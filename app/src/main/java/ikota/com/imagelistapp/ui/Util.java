package ikota.com.imagelistapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by kota on 2014/10/10.
 * Utility class.
 */
public class Util {
    // mock user information for development
    private static final String MOCK_USER_ID = "618889";//"19";
    private static final String MOCK_USER_NAME = "tester";//"Oka Tomomi";

    private static final String USER_ID_KEY = "user_id";
    private static final String USER_NAME_KEY = "user_name";
    private static final String USER_IMG_URL_KEY = "user_img_url";
    private static final String USER_EMAIL_KEY = "user_email";
    //private static final String USER_PHONE_NUMBER = "user_phone";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String VERIFICATION_STATE_KEY = "verification_state";

    public static void saveUserId(Context context, String id) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(USER_ID_KEY, id);
        editor.apply();
    }

    public static String getUserId(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(USER_ID_KEY, "");
    }

    public static void saveUserName(Context context, String name) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(USER_NAME_KEY, name);
        editor.apply();
    }

    public static String getUserName(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(USER_NAME_KEY, "");
    }

    public static void saveUserImgURL(Context context, String url) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(USER_IMG_URL_KEY, url);
        editor.apply();
    }

    public static String getUserImgURL(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(USER_IMG_URL_KEY, "");
    }

    public static void saveUserEmail(Context context, String email) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(USER_EMAIL_KEY, email);
        editor.apply();
    }

//    public static String getUserEmail(Context context) {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
//        return pref.getString(USER_EMAIL_KEY,"");
//    }

    public static void saveUserPhoneNumber(Context context, String phone_number) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(USER_EMAIL_KEY, phone_number);
        editor.apply();
    }

//    public static String getUserPhoneNumber(Context context) {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
//        return pref.getString(USER_PHONE_NUMBER,"");
//    }

    public static void saveSerialVerificationState(Context context ,boolean state) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(VERIFICATION_STATE_KEY, state);
        editor.apply();
    }

    /** if verification has done , return true */
    public static boolean getSerialVerificationState(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(VERIFICATION_STATE_KEY, false);
    }

    public static void resetData(Context context) {
        saveUserId(context, "");
        saveUserName(context, "");
        saveUserImgURL(context, "");
    }
}
