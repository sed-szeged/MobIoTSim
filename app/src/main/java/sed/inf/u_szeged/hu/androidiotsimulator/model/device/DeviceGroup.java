package sed.inf.u_szeged.hu.androidiotsimulator.model.device;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.controller.RESTTools;
import sed.inf.u_szeged.hu.androidiotsimulator.model.json.BulkDevice;
import sed.inf.u_szeged.hu.androidiotsimulator.model.trace.TraceGroup;

/**
 * Created by tommy on 2/27/2017. Project name: MobIoTSim-mirrored
 *  
 */
public class DeviceGroup {

    private ArrayList<Device> devicesList;
    private TraceGroup finishedTraceList;
    private Device baseDevice;

    public DeviceGroup(Device baseDevice) {
        this.baseDevice = new Device(baseDevice);
        devicesList = new ArrayList<>();

        int numOfDevices = baseDevice.getNumOfDevices();
        String deviceId = baseDevice.getDeviceID();

        for (int i = 0; i < numOfDevices; i++) {
            Device newDevice = new Device(this.baseDevice, deviceId + "_" + (i + 1));
            devicesList.add(newDevice);
        }

        saveToCloud();
    }

    public void startDevices() {
        for (Device d : devicesList) {
            new Thread(d).start();
        }
    }


    public void stopDevices(Context ctx) {
        finishedTraceList = new TraceGroup();
        for (Device d : devicesList) {
            d.stop(ctx);

            finishedTraceList.add(d.getClog());
        }

        if (baseDevice.getTraceFileLocation().equals("random")) {
            finishedTraceList.setCnt(finishedTraceList.getTraceGroup().get(0).getCycles().size());
            finishedTraceList.setType(baseDevice.getType());
            saveTraceToJson(ctx);
        }
    }

    public boolean isRunning() {
        boolean result = false;
        for (Device d : devicesList) {
            if (d.isRunning()) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void saveToCloud() {
        String finalJson = "[";

        for (int i = 0; i < devicesList.size() - 1; i++) {
            BulkDevice bulkDevice = new BulkDevice(devicesList.get(i));
            finalJson += bulkDevice.getAddJsonCode() + ",";
        }
        BulkDevice lastDevice = new BulkDevice(devicesList.get(devicesList.size() - 1));
        finalJson += lastDevice.getAddJsonCode();
        finalJson += "]";

        String auth_key = MobIoTApplication.loadData(CloudSettingsActivity.KEY_AUTH_KEY);
        String auth_token = MobIoTApplication.loadData(CloudSettingsActivity.KEY_AUTH_TOKEN);
        String orgId = MobIoTApplication.loadData(CloudSettingsActivity.KEY_ORGANIZATION_ID);

        RESTTools restTools = new RESTTools(orgId, auth_key, auth_token);
        restTools.addDevices(finalJson);
    }

    private void saveTraceToJson(Context ctx) {
        try {
            SimpleDateFormat s = new SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault());
            String format = s.format(new Date());
            String fileName = "group_" + baseDevice.getDeviceID() + "_" + format + ".json";
            File myExternalFile = new File(ctx.getExternalFilesDir("DeviceTraces"), fileName);
            FileOutputStream fos = new FileOutputStream(myExternalFile);

            Gson gson = new Gson();
            String outputString = gson.toJson(finishedTraceList);
            fos.write(outputString.getBytes());
            fos.close();
            Toast.makeText(ctx, "File saved: " + fileName, Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeFromCloud() {
        String finalJson = "[";

        for (int i = 0; i < devicesList.size() - 1; i++) {
            BulkDevice bulkDevice = new BulkDevice(devicesList.get(i));
            finalJson += bulkDevice.getRemoveJsonCode() + ",";
        }
        BulkDevice lastDevice = new BulkDevice(devicesList.get(devicesList.size() - 1));
        finalJson += lastDevice.getRemoveJsonCode();
        finalJson += "]";

        String auth_key = MobIoTApplication.loadData(CloudSettingsActivity.KEY_AUTH_KEY);
        String auth_token = MobIoTApplication.loadData(CloudSettingsActivity.KEY_AUTH_TOKEN);
        String orgId = MobIoTApplication.loadData(CloudSettingsActivity.KEY_ORGANIZATION_ID);

        RESTTools restTools = new RESTTools(orgId, auth_key, auth_token);
        restTools.removeDevice(finalJson);
    }

    public boolean isWarning() {
        boolean result = false;
        for (Device device : devicesList) {
            if (device.isWarning()) {
                result = true;
                break;
            }
        }
        return result;
    }

    public ArrayList<Device> getDevicesList() {
        return devicesList;
    }

    public Device getBaseDevice() {
        return baseDevice;
    }
}
