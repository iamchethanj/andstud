package com.apptronix.alfred.service;

import android.app.IntentService;
import android.content.Intent;

import com.apptronix.alfred.User;
import com.apptronix.alfred.rest.ApiClient;
import com.apptronix.alfred.ui.LoginActivity;
import com.apptronix.alfred.ui.MainActivity;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import timber.log.Timber;


public class AuthService extends IntentService {

    public static final String ACTION_LOGIN = "com.apptronix.alfred.service.action.LOGIN";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public AuthService() {
        super("AuthService");
    }

    private String token, responseString, result, message;

    @Override
    protected void onHandleIntent(Intent intent) {

        if(intent.getAction().equals(ACTION_LOGIN)){
            //fetch refresh token
            token = intent.getStringExtra("idToken");
            handleActionLogin();
        } else {
            //fetch access token
            if(User.getRefreshToken()==null) { // user has logged out, route to login page
                EventBus.getDefault().post(new MainActivity.MessageEvent("NoRefreshToken"));
            }
        }
    }

    private void handleActionLogin() {

        try {
            Timber.i("sending token %s to server",token);

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(30, TimeUnit.SECONDS);
            builder.readTimeout(30, TimeUnit.SECONDS);
            builder.writeTimeout(30, TimeUnit.SECONDS);
            OkHttpClient client = builder.build();

            User user = new User(this);
            JSONObject postJSON = new JSONObject();
            postJSON.put("idToken",token);
            RequestBody body = RequestBody.create(JSON, String.valueOf(postJSON));
            Request request = new Request.Builder()
                    .url(new URL(ApiClient.BASE_URL+"login"))
                    .post(body)
                    .build();

            //for emulator "http://10.0.2.2:5000/login"
            okhttp3.Response response = client.newCall(request).execute();

            if(response.isSuccessful()){

                responseString = response.body().string();
                Timber.i("RefreshToken Responese %s",responseString);

                if(responseString.equals("fail")){

                    message="LoginFailed";

                } else {

                    //registration, received refresh token
                    User.updateTokens(responseString,null,this);
                    message="LoginSuccessful";

                }

            } else {

                message="ServerUnreachable";

            }


        } catch (IOException | JSONException e1) {
            e1.printStackTrace();

            message="ServerUnreachable";
        }

        EventBus.getDefault().post(new LoginActivity.LoginEvent(message));
    }

}
