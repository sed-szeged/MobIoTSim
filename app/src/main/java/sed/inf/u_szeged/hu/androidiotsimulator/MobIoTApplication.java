package sed.inf.u_szeged.hu.androidiotsimulator;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Tomi on 2016. 02. 11..
 */
public class MobIoTApplication extends Application {

    static Activity activity;

    static SharedPreferences sharedPreferences;

    public static final String KEY_TYPE = "TYPE";
    public static final String KEY_ORGATNISATION_ID = "ORGATNISATION_ID";
    public static final String KEY_APPLICATION_ID = "APPLICATION_ID";
    public static final String KEY_AUTH_KEY = "AUTH_KEY";
    public static final String KEY_AUTH_TOKEN = "AUTH_TOKEN";
    public static final String KEY_CONNECTION_TYPE = "CONNECTION_TYPE";

    public static final String KEY_CLOUDS = "CLOUDS";
    public static final String KEY_DEVICES = "DEVICES";


    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences("sed.inf.u_szeged.hu.androidiotsimulator", MODE_PRIVATE);
    }

    public static void setActivity(Activity activity){
        MobIoTApplication.activity = activity;
    }

    public static Activity getActivity(){
        return activity;
    }

    public static void saveData(String key, String data){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, new String(data));
        editor.commit();
    }

    public static String loadData(String key){
        String d = sharedPreferences.getString(key, null);
        if(d==null){
            return null;
        }else{
            return new String(d);
        }
    }

}
