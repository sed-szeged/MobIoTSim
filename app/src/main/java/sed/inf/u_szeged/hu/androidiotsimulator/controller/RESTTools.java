package sed.inf.u_szeged.hu.androidiotsimulator.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudFragment;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudSettingsActivity;

import static sed.inf.u_szeged.hu.androidiotsimulator.activity.main.IoTSimulatorActivity.deviceKeysBuffer;



public class RESTTools {

    private String orgId;
    private String username;
    private String password;
    private Context context;
    private String[] deviceKeys;

    private String BASE_URL = "http://" + CloudFragment.gateway_url + ":3000/api/device/";

    public static ExecutorService rtExecutorService;

    public RESTTools(String orgId, String username, String password, Context context) {
        this.orgId = orgId;
        this.username = username;
        this.password = password;
        this.context = context;
    }

    public RESTTools(String[] deviceKeys, Context context) {
        this.deviceKeys = deviceKeys;
        this.context = context;
    }

    public RESTTools(Context context) {
        this.context = context;
    }

    public void addDevice(JSONObject jsonDevice){
        //Check if login token is expired before uploading device, if it is, we get a new token
        //CloudFragment.refreshIdTokenIfExpired();
        rtExecutorService = Executors.newCachedThreadPool();
        rtExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                CloudFragment.refreshIdTokenIfExpired();
            }
        });
        rtExecutorService.shutdown();

        try{
            Boolean executorFinished = rtExecutorService.awaitTermination(5, TimeUnit.SECONDS);
            if (executorFinished) new CreatingTask(jsonDevice, BASE_URL+"create", username, password, "Add devices").execute();
        }  catch (Exception e){
            e.printStackTrace();
        }

    }

    public void removeDevice(HashSet<String> deviceKeySet) {


        //System.out.println("We are deleting");
        String url = BASE_URL;
        String nextkey="";
        Iterator keys = deviceKeySet.iterator();
        while (keys.hasNext()) {
            url = BASE_URL;
            nextkey= keys.next().toString();
            url+=nextkey;

            rtExecutorService = Executors.newCachedThreadPool();
            rtExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    CloudFragment.refreshIdTokenIfExpired();
                }
            });
            rtExecutorService.shutdown();

            try{
                Boolean executorFinished = rtExecutorService.awaitTermination(5, TimeUnit.SECONDS);
                if (executorFinished) new DeletingTask(url,nextkey, "Remove devices").execute();
            }  catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public void addDeviceType(String strJson) {
        //String url = "https://" + orgId + ".internetofthings.ibmcloud.com/api/v0002/device/types";
        // new CreatingTask(strJson, url, username, password, "Add type").execute();
    }

    public void updateDevice(JSONObject jsonDevice, String deviceKey){
        //Check if login token is expired before updating device, if it is, we get a new token
        //CloudFragment.refreshIdTokenIfExpired();
        //CloudFragment.refreshIdTokenIfExpired();

        String url = BASE_URL + deviceKey.toString() + "/update";

        rtExecutorService = Executors.newCachedThreadPool();
        rtExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                CloudFragment.refreshIdTokenIfExpired();
            }
        });
        rtExecutorService.shutdown();

        try{
            Boolean executorFinished = rtExecutorService.awaitTermination(5, TimeUnit.SECONDS);
            if (executorFinished) new ModifyingTask(jsonDevice,deviceKey, url, username, password, "Modify device").execute();
        }  catch (Exception e){
            e.printStackTrace();
        }

    }

    private class GettingTask extends AsyncTask<String, String, String> {

        String url;
        String username;
        String password;
        String result;
        String message;

        GettingTask(String url, String username, String password, String message) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.result = "";
            this.message=message;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String authStr = username + ":" + password;
                String authEncoded = Base64.encodeToString(authStr.getBytes(), Base64.DEFAULT);

                //System.out.println("AuthEncoded: " + authEncoded);

                HttpURLConnection connection
                        = (HttpURLConnection) new URL(url).openConnection();
                connection.addRequestProperty("Authorization", "Basic " + authEncoded);
                connection.addRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader( new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                message += " response: " +  connection.getResponseCode() + " " +connection.getResponseMessage();
                result = response.toString();

                System.out.println("Result: " + result);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    private class ModifyingTask extends AsyncTask<String, String, Void> {

        JSONObject jsonObject;

        String url;
        String username;
        String password;
        InputStream content;
        String message;


        ModifyingTask(JSONObject jsonObject,String deviceKey, String url, String username, String password, String message) {
            this.jsonObject = jsonObject;
            this.url = url;
            this.username = username;
            this.password = password;
            this.message = message;

        }

        @Override
        protected Void doInBackground(String... strings) {
            try {

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setReadTimeout(15000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                connection.setRequestProperty("Cookie","session-token="+ MobIoTApplication.loadData(CloudSettingsActivity.KEY_GOOGLE_TOKEN));
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                writer.write(jsonDeviceToString(jsonObject));
                writer.flush();
                writer.close();
                os.close();
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    System.out.println("Response code: " + responseCode);
                    System.out.println("Response message: " + connection.getResponseMessage());
                    message += " response: " + responseCode + " " + connection.getResponseMessage();
                } else {
                    System.out.println("false : " + responseCode);
                }
                connection.disconnect();
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private class DeletingTask extends AsyncTask<String, String, Void> {
        String url="";
        String message="";
        String deviceKey="";

        DeletingTask(String url,String deviceKey, String message){
            this.url = url;
            this.message = message;
            this.deviceKey = deviceKey;
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                //System.out.println("DELETE, THIS IS THE URL" + url);
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setReadTimeout(15000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                connection.setRequestMethod("DELETE");
                connection.setRequestProperty("Cookie","session-token="+ MobIoTApplication.loadData(CloudSettingsActivity.KEY_GOOGLE_TOKEN));


                connection.setDoInput(true);
                connection.setDoOutput(true);




                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                StringBuilder result = new StringBuilder();

                result.append(URLEncoder.encode("device_id", "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(deviceKey, "UTF-8"));

                writer.write(result.toString());
                writer.flush();
                writer.close();
                os.close();
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    System.out.println("Response code: " + responseCode);
                    System.out.println("Response message: " + connection.getResponseMessage());
                    message += " response: " + responseCode + " " + connection.getResponseMessage();
                } else {
                    System.out.println("false : " + responseCode);
                }
                connection.disconnect();
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private class CreatingTask extends AsyncTask<String, String, Void> {

        JSONObject jsonObject;
        String url;
        String username;
        String password;
        String message;

        CreatingTask(JSONObject jsonObject, String url, String username, String password, String message) {
            this.jsonObject = jsonObject;
            this.url = url;
            this.username = username;
            this.password = password;
            this.message = message;

        }

        @Override
        protected Void doInBackground(String... strings) {
            try {

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setReadTimeout(15000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Cookie","session-token="+ MobIoTApplication.loadData(CloudSettingsActivity.KEY_GOOGLE_TOKEN));
                connection.setDoInput(true);
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                writer.write(jsonDeviceToString(jsonObject));

                writer.flush();
                writer.close();
                os.close();
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    System.out.println("sb :" + sb);

                    String[] devNameWithId = getDeviceNameWithId(sb.toString());

                    deviceKeysBuffer.put(devNameWithId[0], devNameWithId[1]);

                    in.close();
                    System.out.println("Response code: " + responseCode);
                    System.out.println("Response message: " + connection.getResponseMessage());
                    message += " response: " + responseCode + " " + connection.getResponseMessage();
                } else {
                    System.out.println("false : " + responseCode);
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    String error = "";
                    String piece;
                    while ((piece = br.readLine()) != null){
                        error += piece;
                    }
                    System.out.println("error: " + error);
                }
                connection.disconnect();
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private String[] getDeviceNameWithId(String response) {
        String[] deviceNameWithId = new String[2];
        deviceNameWithId = response.replaceAll("\"","").split("/");

        return deviceNameWithId;
    }

    public String jsonDeviceToString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){
            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }

    public Context getContext() {
        return context;
    }
}

