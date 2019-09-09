package sed.inf.u_szeged.hu.androidiotsimulator.model.device;

import android.util.JsonReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.randomdata.Parameter;

/**
 * Created by tommy on 8/7/2017. Project name: MobIoTSim-mirrored
 *  
 */

class FileStreamHandler {

    private static FileStreamHandler instance;
    private static String filePath;
    private static File file;
    private FileInputStream fileInputStream;


    public String getFilePath() {
        return filePath;
    }

    void setFilePath(String filePath) {
        FileStreamHandler.filePath = filePath;
        file = new File(filePath);
    }

    static synchronized FileStreamHandler getInstance() {
        if (instance == null) {
            instance = new FileStreamHandler();
        }
        return instance;
    }


    synchronized List<Parameter> getParameterValuesForDevice(int deviceId, int cycleNumber) {
        try {
            fileInputStream = new FileInputStream(file);
            JsonReader reader = new JsonReader(new InputStreamReader(fileInputStream, "UTF-8"));

            reader.beginObject();
            reader.nextName(); // cnt
            int cnt = reader.nextInt();  // cnt num
            reader.nextName(); // tracegroup
            reader.beginArray();

            for (int i = 0; i < (deviceId%cnt) - 1; i++) {
                reader.skipValue();
            }

            reader.beginObject();
            reader.nextName(); // cycles;
            reader.beginArray();

            for (int i = 0; i < cycleNumber; i++) {
                reader.skipValue();
            }

            List<Parameter> parameterList = new ArrayList<>();

            reader.beginObject();
            reader.nextName(); //parameterList
            reader.beginArray();

            while (reader.hasNext()) {
                reader.beginObject();

                reader.nextName(); // name
                String parameterName = reader.nextString();

                reader.nextName(); // value
                String parameterValue = reader.nextString();

                Parameter parameter = new Parameter(parameterName, parameterValue);
                parameterList.add(parameter);
                reader.endObject();
            }

            reader.close();
            fileInputStream.close();

            return parameterList;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    synchronized int getCnt() {
        int cnt = 0;  // cnt num
        try {
            fileInputStream = new FileInputStream(file);

            JsonReader reader = new JsonReader(new InputStreamReader(fileInputStream, "UTF-8"));
            reader.beginObject();
            reader.nextName(); // cnt
            cnt = Integer.parseInt(reader.nextString());
            reader.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cnt;
    }
}
