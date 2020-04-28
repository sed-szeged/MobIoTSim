package sed.inf.u_szeged.hu.androidiotsimulator;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class MobIoTApplication extends Application {

    // Required for saving clouds and Devices
    public static final String KEY_CLOUDS = "CLOUDS";
    public static final String KEY_DEVICES = "DEVICES";
    public static final String KEY_DEVICEKEYS = "DEVICEKEYS";
    public static final String KEY_DEFAULTPASSWORD = "default_device_pwd_1234";

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
        editor.putString(key, data);
        editor.apply();
    }

    public static void deleteKey(String key){
        sharedPreferences.edit().remove(key).commit();
    }

    // Loading data from shared preferences
    public static String loadData(String key) {
        String d = sharedPreferences.getString(key, null);
        if (d == null) {
            return null;
        } else {
            return d;
        }
    }

    public static String updateDevicesKeys(String raw, String oldItem, String newItem){
        String updatedString="";
        if (raw != null && !raw.equals("")) {
            updatedString = raw.replace(oldItem,newItem);
        }
        return updatedString;
    }

    public static void deleteDeviceId(String deviceID){
        String loadedDevicesRaw = loadData(KEY_DEVICEKEYS);
        final int devicekeyLength = 24;

        if ( deviceID == null || deviceID.isEmpty() || loadedDevicesRaw == null || loadedDevicesRaw.isEmpty() )  {return;}

        int deviceIndex = loadedDevicesRaw.indexOf(deviceID);
        if ( deviceIndex == -1 )  {return;}
        deviceIndex-=1;

        loadedDevicesRaw = updateDevicesKeys(loadedDevicesRaw,"<"+deviceID,"" );
        String toBeReplaced = loadedDevicesRaw.substring(deviceIndex, deviceIndex + devicekeyLength + 1);
        loadedDevicesRaw = updateDevicesKeys(loadedDevicesRaw,toBeReplaced,"");
        saveData(KEY_DEVICEKEYS, loadedDevicesRaw);

    }

    public static void clearSharedPreferencesWithExceptions( HashMap<String,String> keepData ){
        sharedPreferences.edit().clear().apply();

        for(Map.Entry<String,String> storeData: keepData.entrySet()){
           saveData(storeData.getKey(),storeData.getValue());
        }
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
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

