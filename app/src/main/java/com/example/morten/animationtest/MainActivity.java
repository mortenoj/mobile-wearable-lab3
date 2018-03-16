package com.example.morten.animationtest;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends Activity {

    MainActivity_Layout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layout = new MainActivity_Layout(this);
        setContentView(layout);


    }

    @Override
    public void onResume() {
        super.onResume();
        layout.resume();
    }



    @Override
    public void onPause() {
        super.onPause();
        layout.pause();
    }
}
