package com.example.cherrymusicplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
 
public class Splash extends Activity {   
 
    private final int SPLASH_DISPLAY_LENGHT = 1000;  //1000
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        new Handler().postDelayed(new Runnable(){
 
         @Override
         public void run() {
             Intent mainIntent = new Intent(Splash.this,PageControler.class);
             Splash.this.startActivity(mainIntent);
                 Splash.this.finish();
         }
           
        }, SPLASH_DISPLAY_LENGHT);
    }
} 