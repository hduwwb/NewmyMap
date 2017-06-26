package com.exemple.newmymap;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by Jasper on 2017/6/23.
 */

public class SplashActivity extends AppCompatActivity {
    private ImageView launchImage;
    private ImageView imageClock;
    private ImageView imageText;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        launchImage = (ImageView)findViewById(R.id.launch_image);
        imageClock = (ImageView)findViewById(R.id.splash_clock);
        imageText = (ImageView)findViewById(R.id.splash_text);
        float curTranslationY = imageClock.getTranslationY();
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageClock, "translationY",-100f, curTranslationY);
        animator.setDuration(2000);
        animator.start();
        animator = ObjectAnimator.ofFloat(imageText, "alpha", 0f, 1f);
        animator.setDuration(2000);
        animator.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        }).start();
    }
}
