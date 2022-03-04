package com.example.chatapp_2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

public class SplashScreen extends AppCompatActivity {

    private static int splash_timer=3000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //3000ms=3s bu activity gosterilip sonrasinda diger activity'ye gecis yapacak;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
              Intent intent=new Intent(SplashScreen.this,MainActivity.class);
              startActivity(intent);
              finish(); //finish the activity(geri tusuna vs basilirsa tekrar acilmasini engelliyor)
            }
        },splash_timer);
    }
}