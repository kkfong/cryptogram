package com.pixplicity.cryptogram.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.cryptogram.utils.CryptogramProvider;
import com.pixplicity.cryptogram.views.CryptogramView;

import butterknife.BindView;

public class CryptogramActivity extends BaseActivity {

    @BindView(R.id.vg_cryptogram)
    protected ViewGroup mVgCryptogram;

    @BindView(R.id.tv_author)
    protected TextView mTvAuthor;

    @BindView(R.id.cryptogram)
    protected CryptogramView mCryptogramView;

    @BindView(R.id.tv_error)
    protected TextView mTvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cryptogram);

        Cryptogram cryptogram = CryptogramProvider.getInstance(this).getCurrent();
        updateCryptogram(cryptogram);
    }

    private void updateCryptogram(Cryptogram cryptogram) {
        if (cryptogram != null) {
            mTvError.setVisibility(View.GONE);
            mVgCryptogram.setVisibility(View.VISIBLE);
            mCryptogramView.setCryptogram(cryptogram);
            mTvAuthor.setText(getString(R.string.quote, cryptogram.getAuthor()));
            mToolbar.setSubtitle(getString(
                    R.string.puzzle_number,
                    cryptogram.getId() + 1,
                    CryptogramProvider.getInstance(this).getCount()));
        } else {
            mTvError.setVisibility(View.VISIBLE);
            mVgCryptogram.setVisibility(View.GONE);
            mToolbar.setSubtitle(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cryptogram, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Cryptogram cryptogram = mCryptogramView.getCryptogram();
        switch (item.getItemId()) {
            case R.id.action_next: {
                if (cryptogram == null || cryptogram.isCompleted()) {
                    nextPuzzle();
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.skip_puzzle)
                            .setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    nextPuzzle();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .show();
                }
            }
            return true;
            case R.id.action_reveal: {
                if (cryptogram == null || !mCryptogramView.hasSelectedCharacter()) {
                    Snackbar.make(mVgContent, "Please select a letter first.", Snackbar.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.reveal_confirmation)
                            .setPositiveButton(R.string.reveal, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mCryptogramView.revealCharacterMapping(
                                            mCryptogramView.getSelectedCharacter())) {
                                        // Answer revealed; clear the selection
                                        mCryptogramView.setSelectedCharacter((char) 0);
                                    }
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .show();
                }
            }
            return true;
            case R.id.action_reset: {
                if (cryptogram != null) {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.reset_puzzle)
                            .setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    cryptogram.reset();
                                    mCryptogramView.invalidate();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .show();
                }
            }
            return true;
            case R.id.action_about: {
                startActivity(AboutActivity.create(this));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void nextPuzzle() {
        Cryptogram cryptogram = CryptogramProvider.getInstance(CryptogramActivity.this).getNext();
        updateCryptogram(cryptogram);
    }

}
