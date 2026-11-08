package com.example.fitnessapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "FitnessSession";
    private static final String KEY_ACCESS = "access_token";
    private static final String KEY_REFRESH = "refresh_token";

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveTokens(String access, String refresh) {
        editor.putString(KEY_ACCESS, access);
        editor.putString(KEY_REFRESH, refresh);
        editor.apply();
    }

    public String getAccessToken() { return prefs.getString(KEY_ACCESS, null); }
    public String getRefreshToken() { return prefs.getString(KEY_REFRESH, null); }

    public boolean isLoggedIn() {
        return getRefreshToken() != null;
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}