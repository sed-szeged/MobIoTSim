package sed.inf.u_szeged.hu.androidiotsimulator.controller;

import android.os.AsyncTask;
import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


/**
 * Created by tommy on 2/25/2017. Project name: MobIoTSim-mirrored
 *  
 */
public class RESTTools {

    private String orgId;
    private String username;
    private String password;

    public RESTTools(String orgId, String username, String password) {
        this.orgId = orgId;
        this.username = username;
        this.password = password;
    }

    public void addDevices(String strJson) {
        String url = "https://" + orgId + ".internetofthings.ibmcloud.com/api/v0002/bulk/devices/add";
        new PostingTask(strJson, url, username, password).execute();
    }


    public void removeDevice(String strJson) {
        String url = "https://" + orgId + ".internetofthings.ibmcloud.com/api/v0002/bulk/devices/remove";
        new PostingTask(strJson, url, username, password).execute();
    }


    private class PostingTask extends AsyncTask<String, String, Void> {

        String strJson;
        String url;
        String username;
        String password;
        InputStream content;

        public PostingTask(String strJson, String url, String username, String password) {
            this.strJson = strJson;
            this.url = url;
            this.username = username;
            this.password = password;
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

                out.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

}
