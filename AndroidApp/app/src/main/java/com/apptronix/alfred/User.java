package com.apptronix.alfred;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Created by DevOpsTrends on 6/10/2017.
 */

public class User {
    private static String userName;
    private static String email;
    private static String picture;
    private static String userID;
    private static String fcmID;
    private static String refreshToken;
    private static String accessToken;

    public User(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        setUserName(prefs.getString("userName",null));
        setEmail(prefs.getString("email",null));
        setPicture(prefs.getString("picture",null));
        setUserID(prefs.getString("userID",null));
        setRefreshToken(prefs.getString("refreshToken",null));
        setAccessToken(prefs.getString("accessToken",null));
        setFcmID(prefs.getString("fcmID",null));
    }

    public static String getFcmID() {
        return fcmID;
    }

    public static void setFcmID(String fcmID) {
        User.fcmID=fcmID;
    }

    public static void setFcmID(String fcmID, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("fcmID",fcmID)
                .apply();
    }

    public static void makeUser(Context context, GoogleSignInAccount account){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("userName", account.getDisplayName())
                .putString("email", account.getEmail())
                .putString("picture", String.valueOf(account.getPhotoUrl()))
                .putString("userID",account.getId())
                .apply();

    }
    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        User.userName = userName;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        User.email = email;
    }

    public static String getPicture() {
        return picture;
    }

    public static void setPicture(String picture) {
        User.picture = picture;
    }

    public static String getUserID() {
        return userID;
    }

    public static void setUserID(String userID) {
        User.userID = userID;
    }

    public static String getRefreshToken() {
        return refreshToken;
    }

    public static void setRefreshToken(String refreshToken) {
        User.refreshToken = refreshToken;
    }

    public static void updateTokens(String refreshToken, @Nullable String accessToken, Context context){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("refreshToken", refreshToken)
                .putString("accessToken", accessToken)
                .apply();
        User.accessToken=accessToken;
        User.refreshToken=refreshToken;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static void setAccessToken(@Nullable String accessToken, Context context) {
        updateTokens(refreshToken,accessToken, context);
    }

    public static void setAccessToken(String accessToken) {
        User.accessToken=accessToken;
    }

    public static void signOutUser(Context context){
        userName=null;
        email=null;
        picture=null;
        userID=null;
        accessToken=null;
        refreshToken=null;
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("userName", null)
                .putString("email", null)
                .putString("picture", null)
                .putString("userID",null)
                .putString("refreshToken", null)
                .putString("accessToken", null)
                .apply();
    }

}