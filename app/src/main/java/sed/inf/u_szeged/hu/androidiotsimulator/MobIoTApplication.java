package sed.inf.u_szeged.hu.androidiotsimulator;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Tomi on 2016. 02. 11..
 */
public class MobIoTApplication extends Application {

    // Required for saving clouds and Devices
    public static final String KEY_CLOUDS = "CLOUDS";
    public static final String KEY_DEVICES = "DEVICES";

    static Activity activity;
    static SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences("sed.inf.u_szeged.hu.androidiotsimulator", MODE_PRIVATE);
    }

    public static Activity getActivity() {
        return activity;
    }

    public static void setActivity(Activity activity) {
        MobIoTApplication.activity = activity;
    }

    // Method for saving data to the shared preferences
    public static void saveData(String key, String data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, new String(data));
        editor.commit();
    }

    // Loading data from shared preferences
    public static String loadData(String key) {
        String d = sharedPreferences.getString(key, null);
        if (d == null) {
            return null;
        } else {
            return new String(d);
        }
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);

        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        fin.close();
        return ret;

    }


}
