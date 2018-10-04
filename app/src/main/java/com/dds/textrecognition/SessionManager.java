package com.dds.textrecognition;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "TextRecognitionPreference";
    private static final String IS_LOGIN = "isLoggedIn";
    public static final String KEY_LAST_WORD_POSITION = "posLastWord";
    public static final String KEY_USER_NAME = "username";

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;


    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createUserSession(String username){
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_USER_NAME, username);
        editor.putInt(KEY_LAST_WORD_POSITION, 0);
        editor.commit();
    }

    public void setLastWordPosition (int pos) {
        editor.putInt(KEY_LAST_WORD_POSITION, pos);
        editor.commit();
    }


    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public int getLastWordPosition () {
        return pref.getInt(KEY_LAST_WORD_POSITION, 0);
    }

    public String getUserName () {
        return pref.getString(KEY_USER_NAME, "");
    }

}
