package com.apptronix.alfred.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.apptronix.alfred.R;
import com.apptronix.alfred.User;
import com.apptronix.alfred.adapter.MainPagerAdapter;
import com.apptronix.alfred.data.DBContract;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements MqttCallback,  LoaderManager.LoaderCallbacks<Cursor>,
        ControlGroupFragment.OnFragmentInteractionListener, ControlsScheduleFragment.OnFragmentInteractionListener{

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    TextView text;
    MainPagerAdapter adapter;

    public static final String ARG_GROUP = "group";
    MqttAndroidClient client;
    boolean mqtt_success = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        viewPager = (ViewPager) findViewById(R.id.pager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        getSupportLoaderManager().initLoader(0,null,this);
        //startService(new Intent(this, DataService.class));
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        try {
            connectBroker();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    private void setupViewPager(ViewPager viewPager,Cursor data) {
        adapter = new MainPagerAdapter(getSupportFragmentManager());

        if(data.moveToFirst()){

            for(int i=0;i<data.getCount();i++){
                data.moveToPosition(i);

                String group=data.getString(data.getColumnIndex(DBContract.ControlsEntry.COLUMN_CONTROL_GROUP));
                Timber.i("Group %s",group);
                Bundle bundle = new Bundle();
                bundle.putCharSequence(ARG_GROUP,group);
                ControlGroupFragment controlGroupFragment = new ControlGroupFragment();
                controlGroupFragment.setArguments(bundle);
                adapter.addFragment(controlGroupFragment,group);
            }
        }
        Timber.i("view pager setup");
        adapter.addFragment(new ControlsScheduleFragment(),"Schedule");
        viewPager.setAdapter(adapter);
    }

    private void connectBroker() throws MqttException {


                SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(this);
        String baseUrl = sharedPref.getString(SettingsActivity.KEY_PREF_BASE_URL, "10.50.47.245:1883");
        String clientId = MqttClient.generateClientId();
        Timber.i("mqtt url %s", baseUrl);
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://"+baseUrl,
                clientId);
        IMqttToken token = client.connect();
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                // We are connected
                mqtt_success = true;
                client.setCallback(MainActivity.this);

                try {
                    subscribeTopics();
                } catch (MqttException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                mqtt_success = false;

            }
        });

    }

    private void subscribeTopics() throws MqttException {

        Cursor topicsCursor = getContentResolver().query(DBContract.ControlsEntry.TOPIC_CONTENT_URI,null,null,null,null);
        topicsCursor.moveToFirst();
        int count = topicsCursor.getCount();
        String[] topics  = new String[count];
        int[] qos = new int[count];
        for(int i=0;i<count;i++){
            topicsCursor.moveToPosition(i);
            topics[i]=topicsCursor.getString(topicsCursor.getColumnIndex(DBContract.ControlsEntry.COLUMN_MQTT_TOPIC));
            qos[i]=1;
        }
        IMqttToken subToken = client.subscribe(topics, qos);
        subToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {

            }

            @Override
            public void onFailure(IMqttToken asyncActionToken,
                                  Throwable exception) {
                // The subscription could not be performed, maybe the user was not
                // authorized to subscribe on the specified topic e.g. using wildcards

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle().equals("Settings")){
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        } else if (item.getTitle().equals("SignOut")){
            signOut();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        mqtt_success=false;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        ContentValues values = new ContentValues();
        JSONObject jsonObject = new JSONObject(message.toString());
        int state=jsonObject.getInt("state");
        Timber.i("%s%s%d",topic,message.toString(),state);
        values.put(DBContract.ControlsEntry.COLUMN_CONTROL_STATUS,state);
        getContentResolver().update(DBContract.ControlsEntry.CONTENT_URI,values,null,new String[]{topic});

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                DBContract.ControlsEntry.GROUPS_CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Timber.i("groups %d",data.getCount());
        setupViewPager(viewPager,data);

        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        tabLayout.removeAllTabs();
    }

    @Subscribe(threadMode= ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event){

        Timber.i(event.getMessage());

        switch (event.getMessage()){
            case "ServerUnreachable": {

                Toast.makeText(this,"Server Unreachable",Toast.LENGTH_LONG).show();
                break;

            }
            case "TokenUpdateRefused":{

                Toast.makeText(this, "Please Login Again",Toast.LENGTH_LONG).show();
                signOut();
                break;
            }

        }
    }

    public void addControlItem(View view) {

        Intent intent = new Intent(this,AddControlItemActivity.class);
        startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //control group frag
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null){
            getSupportLoaderManager().restartLoader(0,null,this);
        }
    }

    @Override
    public void messageMqttTopic(String topic, String msg) {



        if(mqtt_success){
            Timber.i("mqtt connected");
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = msg.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                client.publish(topic, message);
                ContentValues values = new ContentValues();
                JSONObject jsonObject = new JSONObject(msg.toString());
                int state=jsonObject.getInt("state");
                Timber.i("%s%s%d",topic,message.toString(),state);
                values.put(DBContract.ControlsEntry.COLUMN_CONTROL_STATUS,state);
                getContentResolver().update(DBContract.ControlsEntry.CONTENT_URI,values,null,new String[]{topic});
            } catch (JSONException |UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }

        } else {
            Timber.i("mqtt not connected");
            Toast.makeText(this,"mqtt not connected",Toast.LENGTH_SHORT).show();
            try {
                connectBroker();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public static class MessageEvent{

        public String message;

        public  MessageEvent(String message){
            this.message=message;
        }

        public String getMessage(){
            return message;
        }

    }

    private void signOut() {

        User.signOutUser(this);
        startActivity(new Intent(this,LoginActivity.class));
        finish();

    }
}
