package sed.inf.u_szeged.hu.androidiotsimulator.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;

import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.devicetype.DeviceTypes;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.devicetype.Result;


/**
 * Created by tommy on 2/25/2017. Project name: MobIoTSim-mirrored
 *  
 */
public class RESTTools {

    private String orgId;
    private String username;
    private String password;
    private Context context;

    public RESTTools(String orgId, String username, String password, Context context) {
        this.orgId = orgId;
        this.username = username;
        this.password = password;
        this.context = context;
    }

    public void addDevices(String strJson) {
        String url = "https://" + orgId + ".internetofthings.ibmcloud.com/api/v0002/bulk/devices/add";
        new PostingTask(strJson, url, username, password, "Add devices").execute();
    }

    public void removeDevice(String strJson) {
        String url = "https://" + orgId + ".internetofthings.ibmcloud.com/api/v0002/bulk/devices/remove";
        new PostingTask(strJson, url, username, password, "Remove devices").execute();
    }

    public void addDeviceType(String strJson) {
        String url = "https://" + orgId + ".internetofthings.ibmcloud.com/api/v0002/device/types";
        new PostingTask(strJson, url, username, password, "Add type").execute();
    }

    public List<Result> getDeviceTypes() {
        String result = "";
        String url = "https://" + orgId + ".internetofthings.ibmcloud.com/api/v0002/device/types";
        try {
            result = new GettingTask(url, username, password, "Get Types").execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();

        DeviceTypes deviceTypes = gson.fromJson(result, DeviceTypes.class);
        return deviceTypes.getResults();
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

                System.out.println("AuthEncoded: " + authEncoded);

                HttpURLConnection connection
                        = (HttpURLConnection) new URL(url).openConnection();
                connection.addRequestProperty("Authorization", "Basic " + authEncoded);
                connection.addRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
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
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }


    private class PostingTask extends AsyncTask<String, String, Void> {

        String strJson;
        String url;
        String username;
        String password;
        InputStream content;
        String message;

        PostingTask(String strJson, String url, String username, String password, String message) {
            this.strJson = strJson;
            this.url = url;
            this.username = username;
            this.password = password;
            this.message = message;
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {

                String authStr = username + ":" + password;
                String authEncoded = Base64.encodeToString(authStr.getBytes(), Base64.DEFAULT);

                System.out.println("AuthEncoded: " + authEncoded);
                System.out.println("JSON: " + strJson);

                HttpURLConnection connection
                        = (HttpURLConnection) new URL(url).openConnection();
                connection.setDoOutput(true);
                connection.addRequestProperty("Authorization", "Basic " + authEncoded);
                connection.addRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("POST");
                connection.setRequestProperty("charset", "utf-8");

                OutputStream out = connection.getOutputStream();
                out.write(strJson.getBytes(StandardCharsets.UTF_8));

                content = connection.getErrorStream();

                System.out.println("Response code: " + connection.getResponseCode());
                System.out.println("Response message: " + connection.getResponseMessage());
                message += " response: " +  connection.getResponseCode() + " " +connection.getResponseMessage();

                out.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    public Context getContext() {
        return context;
    }
}
