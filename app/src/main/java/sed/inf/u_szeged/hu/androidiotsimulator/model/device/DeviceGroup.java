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
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.controller.RESTTools;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.openweather.OpenweatherTrace;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.randomdata.FinishedTrace;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.randomdata.TraceGroup;

import static sed.inf.u_szeged.hu.androidiotsimulator.activity.main.IoTSimulatorActivity.deviceKeysBuffer;


public class DeviceGroup {

    private static final String GENERIC_DEVICE_TRACE = "generic_device_with_parameters";
    private ArrayList<Device> devicesList;
    private TraceGroup finishedTraceList;
    private Device baseDevice;
    private Context context;

    public DeviceGroup(Device baseDevice, Context context) {
        this.baseDevice = new Device(baseDevice);
        this.context = context;
        devicesList = new ArrayList<>();

        int numOfDevices = baseDevice.getNumOfDevices();
        String deviceId = baseDevice.getDeviceID();

        for (int i = 0; i < numOfDevices; i++) {
            Device newDevice = new Device(this.baseDevice);
            newDevice.setDeviceID(deviceId + "_" + generateIdNumber(i));

            if (!Objects.equals(baseDevice.getTraceFileLocation(), "random")) {
                if (isGenericTrace()) {
                    newDevice.setHasTraceData(true);
                    newDevice.setTraceCounter(0);
                } else {
                    String jsonStr = getJsonStr();
                    newDevice.setOpenweatherTraceData(getOpenWeatherDatas(jsonStr));
                    newDevice.setHasTraceData(false);
                }
            } else {
                newDevice.setHasTraceData(false);
            }
            devicesList.add(newDevice);
        }
    }


    private String generateIdNumber(int i) {
        if (i<9) {
            return "00"+(i+1);
        }
        if (i<99) {
            return "0"+(i+1);
        }
        return String.valueOf((i+1));
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

        if (baseDevice.getTraceFileLocation().equals("random") && baseDevice.isSaveTrace()) {
            finishedTraceList.setCnt(finishedTraceList.getTraceGroup().get(0).getCycles().size());
            finishedTraceList.setType(GENERIC_DEVICE_TRACE);
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

    public void uploadDeviceGroup(Context context){
        for (Device dev:  devicesList) {
            dev.uploadDevice(context);
        }
    }

    public void updateDeviceGroup(Device editedDeviceData, Context context ){

        for(int i=0;i<devicesList.size();i++){
            Device dev = devicesList.get(i);
            String deviceKey = dev.getDeviceKeyFromSharedPref(dev);
            dev.setDeviceID(editedDeviceData.getDeviceID()+"_"+generateIdNumber(i));
            dev.setType(editedDeviceData.getType());
            dev.setPassword(editedDeviceData.getPassword());
            dev.setTopics(editedDeviceData.getTopics());
            dev.updateDeviceInCloud(context,deviceKey);
        }
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

    public void updateDeviceKeysInSharedPreferences (Device originalDevice,Device editedDevice, Context context){//updateInSharedPreferences
        String devKeysRaw = MobIoTApplication.loadData(MobIoTApplication.KEY_DEVICEKEYS);

        for(int i=0; i<devicesList.size();i++){
            String  generatedId = generateIdNumber(i);
            String oldDeviceGroupId = originalDevice.getDeviceID()+"_"+generatedId;
            String editedDeviceId = editedDevice.getDeviceID()+"_"+generatedId;
            devKeysRaw = MobIoTApplication.updateDevicesKeys(devKeysRaw,oldDeviceGroupId,editedDeviceId);

            devicesList.get(i).setDeviceID(editedDeviceId);
            devicesList.get(i).setType(editedDevice.getType());
            devicesList.get(i).setPassword(editedDevice.getPassword());
            devicesList.get(i).setTopics(editedDevice.getTopics());

        }
        MobIoTApplication.deleteKey(MobIoTApplication.KEY_DEVICEKEYS);
        MobIoTApplication.saveData(MobIoTApplication.KEY_DEVICEKEYS,devKeysRaw);
    }

    public void removeFromCloud() { //collect devicekeys of selected deviceGroup from SP and buffer
        String[] sb = null;

        String devKeysRaw = MobIoTApplication.loadData(MobIoTApplication.KEY_DEVICEKEYS);

        HashSet<String> deviceKeys = new HashSet<>();
        ArrayList<String> deviceIdsFromList = new ArrayList<String>();
        for (int i = 0; i < devicesList.size(); i++) {
            deviceIdsFromList.add(devicesList.get(i).getDeviceID());

        if (devKeysRaw != null && !devKeysRaw.equals("")) {
            StringTokenizer devicesSt = new StringTokenizer(devKeysRaw, "<");

            while (devicesSt.hasMoreTokens()) {
                String device = devicesSt.nextToken();
                System.out.println(device);
                StringTokenizer deviceSt = new StringTokenizer(device, "|");
                boolean deviceFound = false;
                while (deviceSt.hasMoreTokens()) {
                    String deviceInfo = deviceSt.nextToken();

                    if (deviceInfo.equals(deviceIdsFromList.get(i)) && !deviceFound) {
                        deviceFound = true;
                        System.out.println(deviceInfo);
                    }else if(deviceFound){
                        deviceKeys.add(deviceInfo);
                        deviceFound = false;
                    }
                }
            }
        }
        for(Map.Entry<String,String> deviceData: deviceKeysBuffer.entrySet()){
            if(deviceData.getKey().equals(devicesList.get(i).getDeviceID())){
                deviceKeys.add(deviceData.getValue());
            }
        }
    }
        RESTTools restTools = new RESTTools(sb, context);
        restTools.removeDevice(deviceKeys);
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


    private boolean isGenericTrace() {
        final String path = baseDevice.getTraceFileLocation();
        boolean result = false;

        try {
            String jsonStr = MobIoTApplication.getStringFromFile(path);

            if (jsonStr.contains(DeviceGroup.GENERIC_DEVICE_TRACE)) {
                result = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    private String getJsonStr() {
        final String path = baseDevice.getTraceFileLocation();
        String jsonStr = "";
        try {
            jsonStr = MobIoTApplication.getStringFromFile(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonStr;
    }

    private OpenweatherTrace getOpenWeatherDatas(String jsonStr) {
        Gson gson = new Gson();
        jsonStr.replace("\t", "");
        OpenweatherTrace trace = gson.fromJson(jsonStr, OpenweatherTrace.class);
        System.out.println(jsonStr);
        return trace;
    }


    private FinishedTrace getTraceFromGroup(String jsonStr, int index) {
        FinishedTrace obj = null;
        Gson gson = new Gson();
        TraceGroup traceGroup = gson.fromJson(jsonStr, TraceGroup.class);
        int sizeOfTraceGroup = traceGroup.getTraceGroup().size();
        obj = traceGroup.getTraceGroup().get((index) % sizeOfTraceGroup);

        return obj;
    }

    public int getNumOfOnDevices(){
        int result = 0;
        for (Device device : devicesList) {
            if (device.isOn()) {
                result++;
            }
        }
        return result;
    }

    public ArrayList<Device> getDeviceGroup() {
        return devicesList;
    }

    public Device getBaseDevice() {
        return baseDevice;
    }
}
