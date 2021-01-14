package com.abdullah34tr.kisirehberim;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;

public class SplashScreenActivity extends AppCompatActivity {

    private CircularImageView splash_img;
    private TextView splash_txt;
    private Animation topAnim, bottomAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        splash_img = findViewById(R.id.splash_img);
        splash_txt = findViewById(R.id.splash_txt);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        splash_img.setAnimation(topAnim);
        splash_txt.setAnimation(bottomAnim);

        Thread timerThread = new Thread() {

            public void run() {

                try {

                    sleep(2000);
                }

                catch(InterruptedException e) {

                    e.printStackTrace();
                }

                finally {

                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        timerThread.start();
    }

    @Override
    protected void onPause() {

        super.onPause();
        finish();
    }
}