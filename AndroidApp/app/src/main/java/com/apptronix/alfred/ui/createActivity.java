package com.apptronix.alfred.ui;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.apptronix.alfred.R;

public class createActivity extends AppCompatActivity {

    FloatingActionButton schedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        schedule = (FloatingActionButton)findViewById(R.id.addSch);
        schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),scheduleActivity.class));
            }
        });
    }
}
