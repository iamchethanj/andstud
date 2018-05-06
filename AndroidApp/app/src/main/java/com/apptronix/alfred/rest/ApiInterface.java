package com.apptronix.alfred.rest;


import com.apptronix.alfred.model.ControlsResponse;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by DevOpsTrends on 5/26/2017.
 */

public interface ApiInterface {

    @POST("getAccessToken")
    Call<String> getAccessToken(@Body JSONObject object);

    @GET("getControls")
    Call<ControlsResponse> getControlsList(@Header("Authorization") String authorization);
}