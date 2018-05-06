package com.apptronix.alfred.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

import com.apptronix.alfred.User;
import com.apptronix.alfred.data.DBContract;
import com.apptronix.alfred.model.Control;
import com.apptronix.alfred.model.ControlsResponse;
import com.apptronix.alfred.rest.ApiClient;
import com.apptronix.alfred.rest.ApiInterface;
import com.apptronix.alfred.ui.MainActivity;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.Call;
import timber.log.Timber;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class DataService extends IntentService {

    public DataService() {
        super("DataService");
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    @Override
    protected void onHandleIntent(Intent intent) {

        Timber.i("DataService started");

        if (intent != null) {
            final String action = intent.getAction();

            fetchAccessToken();
            handleActionGetControls();
        }
    }

    private void handleActionGetControls() {

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<ControlsResponse> callTT = apiService.getControlsList(User.getAccessToken());

        Timber.i("get controls");
        try {
            Response<ControlsResponse> response = callTT.execute();
            if(response.isSuccessful()){
                if(response.code()==200){
                    List<Control> attdList = response.body().getResults();
                    insertControlsList(attdList);
                } else if( response.code() == 401) { //bad auth
                    User.setAccessToken(null, this); //reset access token
                    fetchAccessToken();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            Timber.i(e.getMessage());
        }

    }

    private void insertControlsList(List<Control> controlsList) {


        Timber.i("insert %d", controlsList.size());
        Vector<ContentValues> cVVector = new Vector<ContentValues>(controlsList.size());

        for(Control control: controlsList){
            ContentValues controlCV = new ContentValues();
            controlCV.put(DBContract.ControlsEntry.COLUMN_CONTROL_NAME,control.getName());
            controlCV.put(DBContract.ControlsEntry.COLUMN_CONTROL_STATUS,control.getStatus());
            cVVector.add(controlCV);
        }

        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            int inserts=this.getContentResolver().bulkInsert(DBContract.ControlsEntry.CONTENT_URI, cvArray);
            Timber.i("%d control bulk insert success", inserts);
        }
    }


    private void fetchAccessToken() {

        User user = new User(this);
        String refreshToken = User.getRefreshToken();
        Timber.i("sending refresh token %s to server",refreshToken);

        try {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(30, TimeUnit.SECONDS);
            builder.readTimeout(30, TimeUnit.SECONDS);
            builder.writeTimeout(30, TimeUnit.SECONDS);
            OkHttpClient client = builder.build();

            JSONObject postJSON = new JSONObject();
            postJSON.put("refreshToken",refreshToken);
            RequestBody body = RequestBody.create(JSON, String.valueOf(postJSON));
            Request request = new Request.Builder()
                    .url(new URL(ApiClient.BASE_URL+"getAccessToken"))
                    .post(body)
                    .build();


            okhttp3.Response response = client.newCall(request).execute();

            if(response.isSuccessful()){

                String responseString = response.body().string();
                Timber.i("AccessToken Response is %s",responseString);

                if(responseString.equals("fail")){

                    EventBus.getDefault().post(new MainActivity.MessageEvent("TokenUpdateRefused"));

                } else {

                    //registration, received refresh token
                    User.updateTokens(User.getRefreshToken(),responseString,this);

                }

            } else {

                EventBus.getDefault().post(new MainActivity.MessageEvent("ServerUnreachable"));

            }

        } catch (IOException | JSONException e1) {
            e1.printStackTrace();
            EventBus.getDefault().post(new MainActivity.MessageEvent("ServerUnreachable"));
        }
    }
}
