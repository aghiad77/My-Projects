package com.project.nearby.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.project.nearby.models.User;

public class Sharedprefs {

    private static final String MY_PREFS_NAME = "com.dotCode.nearBy";
    private static final String TAG = "Sharedprefs";

    private static SharedPreferences sharedPreferences;

    private String masterKeyAlias;


    public Sharedprefs(Context context) {
        sharedPreferences = context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getKey() {
        return sharedPreferences.getString("key", null);
    }

    public void setKey(String key) {
        sharedPreferences.edit().putString("key", key).apply();
    }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public void putBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }


    public String getStrings(String key) {
        return sharedPreferences.getString(key, null);
    }

    public void putString(String key, String value) {

        Log.d(TAG, "putString: aaaaa  :"+value);
        sharedPreferences.edit().putString(key, value).apply();
    }


    public void putUser(User user) {

        Log.d(TAG, "putUser: asdfasdf : "+user.id);

        Gson gson = new Gson();
        String json = gson.toJson(user);
        sharedPreferences.edit().putString("userKey", json).apply();
    }

    public User getUser() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("userKey", null);
        return gson.fromJson(json, User.class);

    }



    public void clearSpecificKey(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    public void removeVariables(){
        putString("user_name",null);
        putString("uid",null);
        putBoolean("isLogged",false);
    }

}
