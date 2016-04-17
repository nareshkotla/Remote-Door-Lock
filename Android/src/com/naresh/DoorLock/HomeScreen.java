package com.naresh.DoorLock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeScreen extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home_screen);
        Button normal_user_button = (Button)findViewById(R.id.normal_user);

        normal_user_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent viewIntent = new Intent(getApplicationContext(), LoginScreen.class);
                startActivityForResult(viewIntent, 0);
            }
        });
        Button one_time_user_button = (Button)findViewById(R.id.one_time_user);

        one_time_user_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent viewIntent = new Intent(getApplicationContext(), OneTimeLoginScreen.class);
                startActivityForResult(viewIntent, 0);
            }
        });
    }
}