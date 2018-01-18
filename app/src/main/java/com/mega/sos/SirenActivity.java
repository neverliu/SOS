package com.mega.sos;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mega.sos.service.SafetyHomecomingService;

import java.sql.Connection;

public class SirenActivity extends AppCompatActivity {

    private Button sirenBtn;
    private static final String TAG = "Siren";

    private SoundPool soundPool;
    int hit;
    int mPresentPlayId;
    float mVolume;
    Boolean isPlay = false;
    Boolean isFirst = true;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siren);
        sirenBtn = (Button) findViewById(R.id.siren_btn);

        SharedPreferences sp = getSharedPreferences("com.mega.sos_preferences", MODE_PRIVATE);
//        SharedPreferences sp = getSharedPreferences("com.mega.sos_preferences", MODE_WORLD_READABLE);
        String sound = sp.getString("siren_sound", "0");

        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
        if (sound.equals("0")) {
            hit = soundPool.load(this, R.raw.aldebaran, 0);
        } else {
            hit = soundPool.load(this, R.raw.adara, 0);
        }

        mVolume = ((float) sp.getInt("ring_volume", 50)) / 100;

        sirenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.siren_btn){
                    if(isPlay){
                        sirenBtn.setText(R.string.start);
                        soundPool.pause(mPresentPlayId);
                        isPlay = false;
                    } else {
                        sirenBtn.setText(R.string.alarm);
                        if (isFirst) {
                            mPresentPlayId = soundPool.play(hit, mVolume, mVolume, 0, -1, (float)1);
                            isFirst = false;
                        } else {
                            soundPool.resume(mPresentPlayId);
                        }
                        isPlay = true;
                    }
                }
            }
        });

//        sirenBtn.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(v.getId() == R.id.siren_btn){
//                    if(event.getAction() == MotionEvent.ACTION_UP){
//                        sirenBtn.setText(R.string.start);
//                        soundPool.pause(mPresentPlayId);
//                    }
//                    if(event.getAction() == MotionEvent.ACTION_DOWN){
//                        sirenBtn.setText(R.string.alarm);
//                        if (isFirst) {
//                            mPresentPlayId = soundPool.play(hit, mVolume, mVolume, 0, -1, (float)1);
//                            isFirst = false;
//                        } else {
//                            soundPool.resume(mPresentPlayId);
//                        }
//                    }
//                }
//                return false;
//            }
//        });

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.siren));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        }
        return super.onOptionsItemSelected(item);
    }
}
