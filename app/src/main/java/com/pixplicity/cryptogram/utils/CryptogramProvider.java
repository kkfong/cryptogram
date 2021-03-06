package com.pixplicity.cryptogram.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.cryptogram.models.CryptogramProgress;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

public class CryptogramProvider {

    private static final String TAG = CryptogramProvider.class.getSimpleName();

    private static final String ASSET_FILENAME = "cryptograms.json";

    private static CryptogramProvider sInstance;

    private int mCurrentIndex = -1;
    private Cryptogram[] mCryptograms;
    private SparseArray<CryptogramProgress> mCryptogramProgress;

    private final Gson mGson = new Gson();
    private final Random mRandom = new Random();

    @NonNull
    public static CryptogramProvider getInstance(Context context) {
        if (sInstance == null) {
            try {
                sInstance = new CryptogramProvider(context);
            } catch (IOException e) {
                Log.e(TAG, "could not read cryptogram file", e);
                Toast.makeText(context, "Could not find any cryptograms", Toast.LENGTH_LONG).show();
            }
        }
        return sInstance;
    }

    private CryptogramProvider(@Nullable Context context) throws IOException {
        if (context != null) {
            InputStream is = context.getAssets().open(ASSET_FILENAME);
            readStream(is);
        } else {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("assets/" + ASSET_FILENAME);
            readStream(is);
            is.close();
        }
    }

    private void readStream(InputStream is) {
        mCryptograms = mGson.fromJson(new InputStreamReader(is), Cryptogram[].class);
        int i = 0;
        for (Cryptogram cryptogram : mCryptograms) {
            if (cryptogram.getId() == 0) {
                cryptogram.setId(i);
                i++;
            }
        }
    }

    public Cryptogram[] getAll() {
        return mCryptograms;
    }

    public int getCount() {
        return getAll().length;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    @Nullable
    public Cryptogram getCurrent() {
        if (mCurrentIndex < 0) {
            mCurrentIndex = PrefsUtils.getCurrentId();
        }
        if (mCurrentIndex < 0) {
            return getNext();
        }
        return get(mCurrentIndex);
    }

    public void setCurrent(int index) {
        mCurrentIndex = index;
        PrefsUtils.setCurrentId(index);
    }

    @Nullable
    public Cryptogram getNext() {
        int oldId = mCurrentIndex;
        int count = getCount();
        if (count == 0) {
            return null;
        }
        if (PrefsUtils.getRandomize()) {
            mCurrentIndex = mRandom.nextInt(count);
        } else {
            mCurrentIndex++;
        }
        if (mCurrentIndex == oldId) {
            // If the puzzle didn't change, simply take the next puzzle
            mCurrentIndex = oldId + 1;
        }
        if (mCurrentIndex >= getCount()) {
            mCurrentIndex = 0;
        }
        setCurrent(mCurrentIndex);
        return get(mCurrentIndex);
    }

    @Nullable
    public Cryptogram get(int index) {
        if (index < 0 || index >= mCryptograms.length) {
            return null;
        }
        return mCryptograms[index];
    }

    @NonNull
    public SparseArray<CryptogramProgress> getProgress() {
        if (mCryptogramProgress == null) {
            int failures = 0;
            mCryptogramProgress = new SparseArray<>();
            Set<String> progressStrSet = PrefsUtils.getProgress();
            if (progressStrSet != null) {
                for (String progressStr : progressStrSet) {
                    try {
                        CryptogramProgress progress = mGson.fromJson(progressStr, CryptogramProgress.class);
                        mCryptogramProgress.put(progress.getId(), progress);
                    } catch (JsonSyntaxException e) {
                        Crashlytics.setString("progressStr", progressStr);
                        Crashlytics.logException(new RuntimeException("Failed reading progress string", e));
                        progressStrSet.remove(progressStr);
                        failures++;
                    }
                }
            }
            if (failures > 0) {
                // Remove any corrupted data
                PrefsUtils.setProgress(progressStrSet);
            }
        }
        return mCryptogramProgress;
    }

    public void setProgress(CryptogramProgress progress) {
        SparseArray<CryptogramProgress> progressList = getProgress();
        progressList.put(progress.getId(), progress);

        // Now store everything
        Set<String> progressStrSet = new LinkedHashSet<>();
        for (int i = 0; i < progressList.size(); i++) {
            progressStrSet.add(mGson.toJson(progressList.valueAt(i)));
        }
        PrefsUtils.setProgress(progressStrSet);
    }

}
