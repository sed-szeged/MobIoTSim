package sed.inf.u_szeged.hu.androidiotsimulator.model.device;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.StringTokenizer;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudFragment;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DeviceSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.main.IoTSimulatorActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.controller.RESTTools;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.device.GsonDevice;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.device.Sensor;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.openweather.OpenweatherTrace;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.randomdata.FinishedTrace;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.randomdata.Parameter;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.randomdata.ParameterWrapper;

import static sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication.getActivity;
import static sed.inf.u_szeged.hu.androidiotsimulator.activity.chart.ChartActivity.chartMaxEntries;

public class Device implements Runnable, MqttCallback {

    private volatile boolean isRunning = false;
    private Gson gson = new Gson();


    private String type;
    private String password;
    private String deviceID;
    private String topics;

    @Expose
    private MqttClient client;
    @Expose
    private Random random;
    //private String organizationID;
    //private String typeID;

    private String token;
    // TODO: remove this two
    //private String commandID;
    //private String eventID;
    //data generation
    private String traceFileLocation;
    //String format;
    private boolean warning = false;
    private int[] prevValue;
    private int numOfDevices;

    private double freq;
    private SensorDataWrapper sensors;
    private boolean isOn = false;
    private int cnt;
    private int traceCounter;
    private boolean saveTrace;
    private FinishedTrace clog;

    private boolean hasTraceData;
    private OpenweatherTrace openweatherTraceData;

    private ArrayList<String> dataSent = new ArrayList();

    private String userId;

    public Device(Device copyDevice) {
        this.isRunning = copyDevice.isRunning();
        this.gson = copyDevice.getGson();
        this.type = copyDevice.getType();
        this.topics = copyDevice.getTopics();
        this.password = copyDevice.getPassword();
        this.client = copyDevice.getClient();
        this.random = copyDevice.getRandom();
        //this.organizationID = copyDevice.getOrganizationID();
        //this.typeID = copyDevice.getTypeID();
        this.deviceID = copyDevice.getDeviceID();
        this.token = copyDevice.getToken();
        //this.commandID = copyDevice.getCommandID();
        //this.eventID = copyDevice.getEventID();
        this.traceFileLocation = copyDevice.getTraceFileLocation();
        this.warning = copyDevice.getWarning();
        this.prevValue = copyDevice.getPrevValue();
        this.numOfDevices = copyDevice.getNumOfDevices();
        this.freq = copyDevice.getFreq();
        this.sensors = copyDevice.getSensors();
        this.isOn = copyDevice.getisOn();
        this.traceCounter = copyDevice.getTraceCounter();
        this.clog = copyDevice.getClog();
        this.hasTraceData = copyDevice.hasTraceData();
        this.saveTrace = copyDevice.isSaveTrace();
        this.userId = copyDevice.getUserId();
    }

    public Device(String deviceID, String password, String topics, String token, String type, double freq, SensorDataWrapper sensors, String traceFileLocation, int numOfDevices, boolean saveTrace, String userId) {
        //this.organizationID = organizationID;
        this.token = token;
        //this.typeID = typeID;
        this.type = type;
        this.deviceID = deviceID;
        this.password = password;
        this.topics = topics;
        this.prevValue = new int[sensors.getList().size()];
        this.freq = freq;
        this.sensors = sensors;
        this.traceFileLocation = traceFileLocation;
        this.numOfDevices = numOfDevices;
        this.saveTrace = saveTrace;
        this.random = new Random();
        this.userId = userId;

        for (int j = 0; j < sensors.getList().size(); j++) {
            prevValue[j] = (Integer.parseInt(sensors.getList().get(j).getMaxValue()) + Integer.parseInt(sensors.getList().get(j).getMinValue())) / 2;
        }

    }


    public static Device fromSerial(String str) {
        StringTokenizer st = new StringTokenizer(str, "|");

        //String organizationID = st.nextToken();
        //String typeID = st.nextToken();
        String deviceID = st.nextToken();
        String password = st.nextToken();
        String topics = st.nextToken();
        String token = st.nextToken();
        String type = st.nextToken();
        double freq = Double.parseDouble(st.nextToken());
        SensorDataWrapper sensorDataWrapper = SensorDataWrapper.sensorDataFromSerial(st.nextToken());
        String traceFileLocation = st.nextToken();
        int numOfDevices = Integer.parseInt(st.nextToken());
        boolean saveTrace = Boolean.parseBoolean(st.nextToken());
        String userId =  st.nextToken();

        return new Device(deviceID,password,topics, token, type, freq, sensorDataWrapper, traceFileLocation, numOfDevices, saveTrace, userId);
    }

    public static Device fromJson(GsonDevice gsonDevice) {

        //String organizationID = gsonDevice.getOrganizationId();
        //String typeID = gsonDevice.getTypeId();
        String deviceID = gsonDevice.getDeviceId();
        String password = gsonDevice.getPassword();
        String topics = gsonDevice.getTopics();
        String token = gsonDevice.getToken();
        String type = gsonDevice.getType();
        double freq = Double.parseDouble(String.valueOf(gsonDevice.getFreq()));
        int numOfDevices = gsonDevice.getNumOfDevices();
        boolean saveTrace = gsonDevice.isSaveTrace();
        String userId = gsonDevice.getUserId();

        SensorDataWrapper sensorDataWrapper = new SensorDataWrapper();
        for (Sensor s : gsonDevice.getSensors()) {
            SensorData sd = new SensorData(s.getName(), String.valueOf(s.getMin()), String.valueOf(s.getMax()));
            sensorDataWrapper.addSensor(sd);
        }
        String traceFileLocation = gsonDevice.getTraceFileLocation();
        return new Device(deviceID,password, topics, token, type, freq, sensorDataWrapper, traceFileLocation, numOfDevices, saveTrace, userId);
    }


    @Override
    public void run() {
        AWSConnect();
        this.isRunning = true;
        this.isOn = true;
        if(this.client != null){
            this.client.setCallback(this);
        }

        this.clog = new FinishedTrace();
        while (this.isRunning) {
            try {
                AWSSend();
                Thread.currentThread().sleep((long) (freq * 1000));

            } catch (InterruptedException e) {
                //running = false;
            }
        }
    }

    private void tryToStopClient() {
      if (client != null ) {
          try {
              client.disconnect();
          } catch (MqttException e) {
              e.printStackTrace();
          }
          client = null;
      }
    }

    public void stop(Context ctx) {
        this.isRunning = false;
        if (traceFileLocation.equals("random")) {
            //saveLog(ctx);
        }
        tryToStopClient();
    }

    private void AWSConnect() {
        final String brokerUrl = "tcp://" + CloudFragment.gateway_url + ":1883";
        String clientId = "";

        tryToStopClient();

        try {
            client = new MqttClient(brokerUrl, clientId, null);
            //TODO
            //client.setCallback(this);

            MqttConnectOptions connOpts = new MqttConnectOptions();

            connOpts.setCleanSession(true);
            //connOpts.setKeepAliveInterval(30);

            connOpts.setUserName(deviceID);

            connOpts.setPassword(password.toCharArray());

            System.out.println("Connecting to broker: " + brokerUrl);
            client.connect(connOpts);

        } catch (MqttException me) {
            System.out.println("| reason " + me.getReasonCode());
            System.out.println("| msg " + me.getMessage());
            System.out.println("| loc " + me.getLocalizedMessage());
            System.out.println("| cause " + me.getCause());
            System.out.println("| excep " + me);
            me.printStackTrace();
        }
    }
    private String getTypeAndTopic(){
        String outText="";
        if (topics.contains("{  \"public/#\": \"r\",  \"/device/")){
           int startSubstring = topics.indexOf("/device/");
            int endSubstring = topics.indexOf("#\": \"rw\"}");
            outText = topics.substring(startSubstring,endSubstring);
        }
        return outText;
    }

    public void AWSSend() {

        if(client == null) {return;}

        String publishTopic = getTypeAndTopic();
        StringBuilder newContent = new StringBuilder();

        if (type.equals("Weathergroup")) {

            String txt = DataGenerator.getNextWeatherGroupRandomMsg();
            newContent.append(txt);
        } else {

            newContent.append("{ \"d\" : { ");

            if (traceFileLocation.equals("random")) {
                int pos = 0;
                ParameterWrapper parameterWrapper = new ParameterWrapper();
                for (SensorData s : sensors.getList()) {
                    String value = String.valueOf(dataGenerator(pos, Integer.parseInt(s.getMinValue()), Integer.parseInt(s.getMaxValue())));
                    newContent.append("\"" + s.getName() + "\" : " + value);
                    if (!s.equals(sensors.getList().get(sensors.getList().size() - 1))) {
                        newContent.append(" , ");
                    }
                    pos++;

                    Parameter logValue = new Parameter(s.getName(), value);
                    parameterWrapper.addValue(logValue);

                }
                clog.addLog(parameterWrapper);
            } else {

                if (hasTraceData) {

                    FileStreamHandler fileStreamHandler = FileStreamHandler.getInstance();
                    fileStreamHandler.setFilePath(traceFileLocation);
                    int i = traceCounter % fileStreamHandler.getCnt();


                    StringBuilder output = new StringBuilder();
                    String deviceIdLastThreeChars = deviceID.substring(deviceID.lastIndexOf("_") + 1);

                    List<Parameter> oneCycle = fileStreamHandler.getParameterValuesForDevice(Integer.parseInt(deviceIdLastThreeChars), i);

                    for (Parameter parameter : oneCycle) {
                        output.append("\"" + parameter.getName() + "\" : " + parameter.getValue() + ",");
                    }
                    int charPos = output.lastIndexOf(",");
                    output.setCharAt(charPos, ' ');


                //  System.out.println(output.toString());
                    newContent.append(output.toString());

                    traceCounter++;
                } else {

                    int i = traceCounter % openweatherTraceData.getCntcycles();

                    String end = deviceID.substring(deviceID.lastIndexOf("_") + 1);
                    int deviceNum = Integer.parseInt(end) % openweatherTraceData.getCycles().get(0).getCnt();

                    StringBuilder output = new StringBuilder();
                    output.append(new Gson().toJson(openweatherTraceData.getCycles().get(i).getList().get(deviceNum), Map.class));
                    //stem.out.println("GSON FIX TEST:" + new Gson().toJson(openweatherTraceData.getCycles().get(i).getList().get(deviceNum), Map.class));


                    output.deleteCharAt(0);
                    output.deleteCharAt(output.length() - 1);

                    newContent.append(output.toString());
                    traceCounter++;
                }
            }
            newContent.append(" } }");
        }

        try {
            MqttMessage message = new MqttMessage(newContent.toString().getBytes());
            message.setQos(0);
            if(dataSent.size()<chartMaxEntries){
                dataSent.add(newContent.toString());
            }
           System.out.println("before publish isConnected " + client.isConnected());
            client.publish(publishTopic, message);
            System.out.println("after publish isConnected " + client.isConnected());

            System.out.println("Message published " + newContent + " TO " + publishTopic);
        } catch (MqttException me) {
            System.out.println("| reason " + me.getReasonCode());
          System.out.println("| msg " + me.getMessage());
          System.out.println("| loc " + me.getLocalizedMessage());
           System.out.println("| cause " + me.getCause());
          System.out.println("| excep " + me);
            me.printStackTrace();
        }
    }

    private int dataGenerator(int pos, int min, int max) {
        int diff = random.nextInt(3);

        //System.out.println(type);
        if (Objects.equals(type, "Thermostat")) {

            if (prevValue[pos] <= 10) {
                isOn = true;
            }

            if (prevValue[pos] >= 20) {
                isOn = false;
            }

           // System.out.println("isOn: " + isOn);

            //TODO: make this not caling it at every call
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    return null;
                }

                @Override
                protected void onPostExecute(Void o) {
                    //Toast.makeText(MobIoTApplication.getActivity(), dID + " command: " + cmd, Toast.LENGTH_LONG).show();
                    if (getActivity() instanceof IoTSimulatorActivity) {
                        Message msg = new Message();
                        msg.what = DevicesFragment.MSG_W_SWITCH;
                        Bundle bundle = new Bundle();
                        //bundle.putString(DeviceSettingsActivity.KEY_TYPE_ID, typeID);
                        bundle.putString(DeviceSettingsActivity.KEY_DEVICE_ID, deviceID);
                        bundle.putString(DeviceSettingsActivity.KEY_PASSWORD, password);
                        bundle.putString(DeviceSettingsActivity.KEY_TOPICS, topics);
                        bundle.putString(DeviceSettingsActivity.KEY_TOKEN, token);
                        bundle.putString(DeviceSettingsActivity.KEY_FREQ, String.valueOf(freq));
                        bundle.putString(DeviceSettingsActivity.KEY_SENSORS, sensors.toString());
                        bundle.putString(DeviceSettingsActivity.KEY_TRACE_LOCATION, traceFileLocation);
                        bundle.putString(DeviceSettingsActivity.KEY_SAVE_TRACE, String.valueOf(saveTrace));
                        bundle.putString(DeviceSettingsActivity.KEY_DEVICE_ID, userId);
                        msg.setData(bundle);
                        msg.setTarget(((IoTSimulatorActivity) getActivity()).handler);
                        msg.sendToTarget();
                    }
                }
            }.execute(null, null, null);


            if (random.nextInt(3) == 0) {
                if (isOn) {
                    prevValue[pos] += diff;
                } else {
                    prevValue[pos] -= diff;
                }
            }


        } else {

            if (random.nextInt(3) == 0) {
                if (random.nextBoolean()) {
                    if (prevValue[pos] + diff <= 100) { // TODO: fix theese
                        prevValue[pos] += diff;
                    }
                } else {
                    if (prevValue[pos] - diff >= 1) {
                        prevValue[pos] -= diff;
                    }
                }
            }
        }
        return prevValue[pos];
    }

    public String getDeviceID() {
        return deviceID;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean w) {
        this.warning = w;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public void setTraceFileLocation(String traceFileLocation) {
        this.traceFileLocation = traceFileLocation;
    }

    public String getTraceFileLocation() {
        return traceFileLocation;
    }

    public double getFreq() {
        return freq;
    }

    public void setFreq(double freq) {
        this.freq = freq;
    }

    public void setSensors(SensorDataWrapper sensors) {
        this.sensors = sensors;
    }

    public SensorDataWrapper getSensors() {
        return sensors;
    }


    public boolean isOn() {
        return isOn;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public String getUserId(){
        return userId;
    }

    @Override
    public void connectionLost(Throwable cause) {
        if (cause != null) {
            cause.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        System.out.println("messageArrived topic:" + topic +
                "\nmessage:" + payload);

        /*if (topic.equals("iot-2/cmd/" + commandID
                + "/fmt/json")) {
*/
        if (topic.equals("iot-2/cmd/fmt/json")) {
            final String dID = deviceID;

            JSONObject jsonObject = new JSONObject(payload);
            final String cmd = jsonObject.getString("cmd");

            this.warning = true;

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    return null;
                }

                @Override
                protected void onPostExecute(Void o) {
                    //Toast.makeText(MobIoTApplication.getActivity(), dID + " command: " + cmd, Toast.LENGTH_LONG).show();
                    if (getActivity() instanceof IoTSimulatorActivity) {
                        Message msg = new Message();
                        msg.what = DevicesFragment.MSG_W_WARNING;
                        Bundle bundle = new Bundle();
                        //bundle.putString(DeviceSettingsActivity.KEY_TYPE_ID, typeID);
                        bundle.putString(DeviceSettingsActivity.KEY_DEVICE_ID, deviceID);
                        bundle.putString(DeviceSettingsActivity.KEY_TOKEN, token);

                        bundle.putString(DeviceSettingsActivity.KEY_FREQ, String.valueOf(freq));
                        bundle.putString(DeviceSettingsActivity.KEY_SENSORS, sensors.toString());
                        bundle.putString(DeviceSettingsActivity.KEY_TRACE_LOCATION, traceFileLocation);
                        bundle.putString(DeviceSettingsActivity.KEY_SAVE_TRACE, String.valueOf(saveTrace));
                        msg.setData(bundle);
                        msg.setTarget(((IoTSimulatorActivity) getActivity()).handler);
                        msg.sendToTarget();
                    }
                }
            }.execute(null, null, null);
            //Toast.makeText(MobIoTApplication.getContext(), "Command: " + cmd, Toast.LENGTH_LONG).show();

            //Reset the count
            /*
            if (cmd != null && cmd.equals("reset")) {
                int resetcount = jsonObject.getInt("count");
                count = resetcount;
                System.out.println("Count is reset to " + resetcount);
            }
            */
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            System.out.println("deliveryComplete token:" + token.getMessage().toString());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Device) {
            Device other = (Device) o;
            /*if (other.getOrganizationID() != null) {
                if (!other.getOrganizationID().equals(organizationID)) {
                    return false;
                }
            } else {
                //TODO !!!
                if (organizationID != null && !organizationID.equals("null")) {
                    return false;
                }

            }
            if (!other.getTypeID().equals(typeID)) {
                return false;
            }*/
            if (!other.getDeviceID().equals(deviceID)) {
                return false;
            }
            if (!other.getPassword().equals(password)) {
                return false;
            }
            if (!other.getTopics().equals(topics)) {
                return false;
            }
            if (!other.getToken().equals(token)) {
                return false;
            }
            if (!other.getType().equals(type)) {
                return false;
            }
            if (other.getFreq() != freq) {
                return false;
            }

            if (other.getSensors() != sensors) {
                return false;
            }

            if (other.getTraceFileLocation() != traceFileLocation) {
                return false;
            }

            if (other.isSaveTrace() != saveTrace) {
                return false;
            }

            if (other.getUserId() != userId){
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    public String getSerial() {
        StringBuilder sb = new StringBuilder();
        sb.append(deviceID);
        sb.append("|");
        sb.append(password);
        sb.append("|");
        sb.append(topics);
        sb.append("|");
        sb.append(token);
        sb.append("|");
        sb.append(type);
        sb.append("|");
        sb.append(freq);
        sb.append("|");
        sb.append(sensors);
        sb.append("|");
        sb.append(traceFileLocation);
        sb.append("|");
        sb.append(numOfDevices);
        sb.append("|");
        sb.append(saveTrace);
        sb.append("|");
        sb.append(userId);
        return sb.toString();
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }


    public Gson getGson() { return gson;  }

    public String getPassword() { return password;  }

    public void setPassword(String password) { this.password = password; }

    public MqttClient getClient() {
        return client;
    }

    public Random getRandom() {
        return random;
    }

    public int[] getPrevValue() {
        return prevValue;
    }

    public int getTraceCounter() {
        return traceCounter;
    }

    public FinishedTrace getClog() {
        return clog;
    }

    public boolean hasTraceData() {
        return hasTraceData;
    }

    public void setTraceCounter(int traceCounter) {
        this.traceCounter = traceCounter;
    }

    public void setHasTraceData(boolean hasTraceData) {
        this.hasTraceData = hasTraceData;
    }

    public void setOpenweatherTraceData(OpenweatherTrace openweatherTraceData) {
        this.openweatherTraceData = openweatherTraceData;
    }

    public boolean getWarning() {
        return warning;
    }

    public boolean getisOn() {
        return isOn;
    }

    public boolean getisRunning() {
        return isRunning;
    }

    public String getTopics() { return topics; }

    public void setTopics(String topics) { this.topics = topics; }

    public int getNumOfDevices() {
        return numOfDevices;
    }

    public void setNumOfDevices(int numOfDevices) {
        this.numOfDevices = numOfDevices;
    }

    public boolean isSaveTrace() {
        return saveTrace;
    }

    public void setSaveTrace(boolean saveTrace) {
        this.saveTrace = saveTrace;
    }

    @Override
    public String toString() {
        return "Device{" +
                "isRunning=" + isRunning +
                ", gson=" + gson +
                ", type='" + type + '\'' +
                ", client=" + client +
                ", random=" + random +
                ", deviceID='" + deviceID + '\'' +
                ", password='" + password + '\'' +
                ", topics='" + topics + '\'' +
                ", token='" + token + '\'' +
                ", traceFileLocation='" + traceFileLocation + '\'' +
                ", warning=" + warning +
                ", prevValue=" + Arrays.toString(prevValue) +
                ", numOfDevices=" + numOfDevices +
                ", freq=" + freq +
                ", sensors=" + sensors +
                ", isOn=" + isOn +
                ", traceCounter=" + traceCounter +
                ", clog=" + clog +
                ", hasTraceData=" + hasTraceData +
                ", userId=" + userId +
                '}';
    }

    public void uploadDevice(Context context){

        RESTTools restTools = new RESTTools(context);

        JSONObject jsonDevice= new JSONObject();
        try {
            jsonDevice.put("name", this.deviceID);
            jsonDevice.put("password", this.password);
            jsonDevice.put("type", this.type);
            jsonDevice.put("topics", this.topics);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        restTools.addDevice(jsonDevice);
    }

    public void updateDeviceInCloud(Context context,  String deviceKey ){

        RESTTools restTools = new RESTTools(context);

        JSONObject jsonDevice= new JSONObject();
        try {
            jsonDevice.put("name", this.deviceID);
            jsonDevice.put("password", this.password);
            jsonDevice.put("type", this.type);
            jsonDevice.put("topics", this.topics);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        restTools.updateDevice(jsonDevice, deviceKey);
    }

    public String getDeviceKeyFromSharedPref(Device oldDevice){
        String devKeysRaw = MobIoTApplication.loadData(MobIoTApplication.KEY_DEVICEKEYS);

        String deviceKey = "";
        String oldDeviceId = oldDevice.getDeviceID();


        if (devKeysRaw != null && !devKeysRaw.equals("")) {
            StringTokenizer devicesSt = new StringTokenizer(devKeysRaw, "<");

            while (devicesSt.hasMoreTokens()) {
                String device = devicesSt.nextToken();
                System.out.println(device);
                StringTokenizer deviceSt = new StringTokenizer(device, "|");
                boolean deviceFound = false;
                while (deviceSt.hasMoreTokens()) {
                    String deviceInfo = deviceSt.nextToken();

                    if (deviceInfo.equals(oldDeviceId) && !deviceFound) {
                        deviceFound = true;
                        System.out.println(deviceInfo);
                    } else if (deviceFound) {
                        deviceKey = deviceInfo;
                        deviceFound = false;
                    }
                }
            }
        }
        return deviceKey;
    }
    public ArrayList<String> getDataSent (){
        return this.dataSent;
    }
}

