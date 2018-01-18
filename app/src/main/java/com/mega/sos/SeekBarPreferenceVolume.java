package com.mega.sos;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.view.View;
import android.app.Activity;
import android.view.KeyEvent;
import android.media.AudioManager;
import android.view.View.OnKeyListener;

// 添加调节音量的 SeekBar 
public class SeekBarPreferenceVolume extends Preference implements OnSeekBarChangeListener {
    private TextView value;
    private int mProgress;
    private int mMax = 100;
    private boolean mTrackingTouch;
    private OnSeekBarPrefsChangeListener mListener = null;
    private int max = 100;
    private int current;
    private Context mContext;
    private SeekBar seekBar;
    private int nowVolume;

    public SeekBarPreferenceVolume(Context context, AttributeSet attrs,
                                   int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setLayoutResource(R.layout.ring_volume_prefs);
    }

    public SeekBarPreferenceVolume(Context context, AttributeSet attrs) {
        super(context, attrs,0);
        mContext = context;
    }

    public SeekBarPreferenceVolume(Context context) {
        super(context,null);
        mContext = context;
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (mListener != null) {
            mListener.onProgressChanged(getKey(),seekBar, progress, fromUser);
        }
        if (seekBar.getProgress() != mProgress) {
            syncProgress(seekBar);
        }
        if (fromUser && !mTrackingTouch) {
            syncProgress(seekBar);
        }

    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mListener != null) {
            mListener.onStartTrackingTouch(getKey(),seekBar);
        }
        mTrackingTouch = true;
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mListener != null) {
            mListener.onStopTrackingTouch(getKey(),seekBar);
        }
        mTrackingTouch = false;
        if (seekBar.getProgress() != mProgress) {
            syncProgress(seekBar);
        }
        notifyHierarchyChanged();

    }

    public void setMax(int max) {
        if (max != mMax) {
            mMax = max;
            notifyChanged();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.progress = mProgress;
        myState.maxs = mMax;
        return myState;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        seekBar = (SeekBar) view.findViewById(R.id.volume_seekbar);
        seekBar.setMax(max);
        seekBar.setProgress(current);
        seekBar.setEnabled(isEnabled());
        seekBar.setOnSeekBarChangeListener(this);
        value = (TextView)view.findViewById(R.id.volume_text);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mProgress = myState.progress;

        mMax = myState.maxs;
        notifyChanged();
    }

    public void setDefaultProgressValue(int defaultValue) {
        if(getPersistedInt(-1) == -1) {

            setProgress(defaultValue);
        }
    }

    public void setProgress(int progress) {
        setProgress(progress, true);
    }

    private void setProgress(int progress, boolean notifyChanged) {
        if (progress > mMax) {
            progress = mMax;
        }
        if (progress < 0) {
            progress = 0;
        }
        if (progress != mProgress) {
            mProgress = progress;
            persistInt(progress);
            if (notifyChanged) {
                notifyChanged();
            }
            current = mProgress;
        }
    }

    public int getProgress() {
        return mProgress;
    }

    public void setOnSeekBarPrefsChangeListener(OnSeekBarPrefsChangeListener listener) {
        mListener = listener;
    }

    public interface OnSeekBarPrefsChangeListener {
        public void onStopTrackingTouch(String key ,SeekBar seekBar) ;
        public void onStartTrackingTouch(String key ,SeekBar seekBar);
        public void onProgressChanged(String key ,SeekBar seekBar, int progress,boolean fromUser);
    }

    void syncProgress(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (progress != mProgress) {
            if (callChangeListener(progress)) {
                setProgress(progress, false);
            } else {
                seekBar.setProgress(mProgress);
            }
            current = mProgress;
        }
    }

    private static class SavedState extends BaseSavedState {

        int progress;
        int maxs;

        public SavedState(Parcel source) {
            super(source);
            progress = source.readInt();
            maxs = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(progress);
            dest.writeInt(maxs);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }
}