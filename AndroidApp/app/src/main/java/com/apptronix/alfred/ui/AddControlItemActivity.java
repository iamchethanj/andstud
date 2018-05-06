package com.apptronix.alfred.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.apptronix.alfred.R;
import com.apptronix.alfred.data.DBContract;

public class AddControlItemActivity extends AppCompatActivity {

    EditText controlName, controlGroup, mqttTopic;
    Button create;
    BottomNavigationView iconSelector;
    String controlType="Power";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_control_item);

        controlName=(EditText)findViewById(R.id.input_name);
        controlGroup=(EditText)findViewById(R.id.input_group);
        mqttTopic=(EditText)findViewById(R.id.input_mqtt_topic);
        iconSelector=(BottomNavigationView)findViewById(R.id.bottom_navigation);
        create = (Button)findViewById(R.id.add_control);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),createActivity.class));
            }
        });

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        iconSelector.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                controlType= (String) item.getTitle();
                return true;
            }
        });
    }

    public void addItem(View view) {


        ContentValues contentValues = new ContentValues();
        contentValues.put(DBContract.ControlsEntry.COLUMN_CONTROL_NAME, String.valueOf(controlName.getText()));
        contentValues.put(DBContract.ControlsEntry.COLUMN_CONTROL_GROUP, String.valueOf(controlGroup.getText()));
        contentValues.put(DBContract.ControlsEntry.COLUMN_CONTROL_TYPE, controlType);
        contentValues.put(DBContract.ControlsEntry.COLUMN_MQTT_TOPIC, String.valueOf(mqttTopic.getText()));
        this.getContentResolver().insert(DBContract.ControlsEntry.CONTENT_URI,contentValues);
        finish();
    }
}
