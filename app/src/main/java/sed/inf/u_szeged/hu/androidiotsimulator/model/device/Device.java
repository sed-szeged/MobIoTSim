package sed.inf.u_szeged.hu.androidiotsimulator.model.device;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.StringTokenizer;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DeviceSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.Generic;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.JsonDevice;
import sed.inf.u_szeged.hu.androidiotsimulator.model.replay.CompleteReplay;
import sed.inf.u_szeged.hu.androidiotsimulator.model.replay.Value;
import sed.inf.u_szeged.hu.androidiotsimulator.model.replay.ValueWrapper;

public class Device implements Runnable, MqttCallback {

    volatile boolean isRunning = false;
    Gson gson = new Gson();
    private String type;
    @Expose
    private MqttClient client;
    @Expose
    private
    Random random;
    private String organizationID;
    private String typeID;
    private String deviceID;
    private String token;
    // TODO: remove this two
    private String commandID;
    private String eventID;
    //data generation
    private String replayFileLocation;
    //String format;
    private boolean warning = false;
    private int[] prevValue;
    private double freq;
    private SensorDataWrapper sensors;
    private boolean isOn = false;
    private int replayCounter;

    private CompleteReplay clog;

    private Generic replayData;


    public Device(String organizationID, String typeID, String deviceID, String token, String type, double freq, SensorDataWrapper sensors, String replayFileLocation) {
        this.organizationID = organizationID;
        this.token = token;
        this.typeID = typeID;
        this.type = type;
        this.deviceID = deviceID;
        this.prevValue = new int[sensors.getList().size()];
        this.freq = freq;
        this.sensors = sensors;
        this.replayFileLocation = replayFileLocation;

        this.random = new Random();

        for (int j = 0; j < sensors.getList().size(); j++) {
            prevValue[j] = (Integer.parseInt(sensors.getList().get(j).getMaxValue()) + Integer.parseInt(sensors.getList().get(j).getMinValue())) / 2;
        }

        if (!Objects.equals(replayFileLocation, "random")) {
            replayData = getReplayFromJson();
            replayCounter = 0;
        } else {
            replayData = null;
        }

    }

    public static Device fromSerial(String str) {
        StringTokenizer st = new StringTokenizer(str, "|");

        String organizationID = st.nextToken();
        String typeID = st.nextToken();
        String deviceID = st.nextToken();
        String token = st.nextToken();
        String type = st.nextToken();
        double freq = Double.parseDouble(st.nextToken());
        SensorDataWrapper sensorDataWrapper = SensorDataWrapper.sensorDataFromSerial(st.nextToken());
        String replayFileLocation = st.nextToken();

        Device d = new Device(organizationID, typeID, deviceID, token, type, freq, sensorDataWrapper, replayFileLocation);
        return d;
    }

    public static Device fromJson(JsonDevice jsonDevice) {

        String organizationID = jsonDevice.getOrganizationId();
        String typeID = jsonDevice.getTypeId();
        String deviceID = jsonDevice.getDeviceId();
        String token = jsonDevice.getToken();
        String type = jsonDevice.getType();
        double freq = Double.parseDouble(String.valueOf(jsonDevice.getFreq()));

        SensorDataWrapper sensorDataWrapper = new SensorDataWrapper();
        for (sed.inf.u_szeged.hu.androidiotsimulator.model.gson.Sensor s : jsonDevice.getSensors()) {
            SensorData sd = new SensorData(s.getName(), String.valueOf(s.getMin()), String.valueOf(s.getMax()));
            sensorDataWrapper.addSensor(sd);
        }

        String replayFileLocation = jsonDevice.getReplayFileLocation();

        Device d = new Device(organizationID, typeID, deviceID, token, type, freq, sensorDataWrapper, replayFileLocation);
        return d;

    }

    @Override
    public void run() {
        bluemixQuickstartConnect();
        isRunning = true;

        isOn = true;

        client.setCallback(this);


        if (!organizationID.equals("quickstart")) {
            bluemixQuickstartSubscribe();
        }

        clog = new CompleteReplay();

        while (isRunning) {
            try {
                bluemixQuickstartSend();

                Thread.currentThread().sleep((long) (freq * 1000));

            } catch (InterruptedException e) {
                //running = false;
            }
        }
    }

    private void bluemixQuickstartSubscribe() {
        String s = "iot-2/cmd/" + commandID + "/fmt/json";
        try {
            client.subscribe(s);
            System.out.println("subscribed to: " + s);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void stop(Context ctx) {
        isRunning = false;
        if (replayFileLocation.equals("random")) {
            saveLog(ctx);
        }
    }

    private void saveLog(Context ctx) {
        String saveResult = "{";
        saveResult += "\"cnt\" : " + clog.getLenght() + ", \"list\" : " + clog + "}";
        System.out.println("Clog: " + clog);
        System.out.println("SaveResult: " + saveResult);

        SimpleDateFormat s = new SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault());
        String format = s.format(new Date());
        String fileName = deviceID + "_" + format + ".json";

        try {
            File myExternalFile = new File(ctx.getExternalFilesDir("DeviceReplays"), fileName);
            FileOutputStream fos = new FileOutputStream(myExternalFile);

            fos.write(saveResult.getBytes());
            fos.close();
            Toast.makeText(ctx, "Replay saved as: " + fileName, Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void bluemixQuickstartConnect() {
        //String broker       = "tcp://quickstart.messaging.internetofthings.ibmcloud.com:1883";
        //String broker       = "tcp://quickstart.messaging.internetofthings.ibmcloud.com:1883";

        commandID = MobIoTApplication.loadData(CloudSettingsActivity.KEY_COMMAND_ID);
        System.out.println("commandID:" + commandID);

        eventID = MobIoTApplication.loadData(CloudSettingsActivity.KEY_EVENT_ID);
        System.out.println("eventID:" + eventID);

        //TODO SSL
        String broker_prefix = "tcp://";
        String broker_suffix = ".messaging.internetofthings.ibmcloud.com:1883";
        String broker = broker_prefix + organizationID + broker_suffix;


        //String clientId     = "d:quickstart:myDeviceType:myDeviceID";
        //String clientId     = "d:wg3go6:myDeviceType:myDeviceID";
        //String clientId     = "d:quickstart:myDeviceType:myDeviceID";
        // d:org_id:type_id:device_id

        String clientId = "d:" + organizationID + ":" + typeID + ":" + deviceID;

        try {
            if (client != null) {
                try {
                    client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                client = null;
            }

            client = new MqttClient(broker, clientId, null);

            //TODO
            //client.setCallback(this);

            //client.setCallback(IoTSimulatorActivity.this);
            MqttConnectOptions connOpts = new MqttConnectOptions();

            connOpts.setCleanSession(true);
            //connOpts.setKeepAliveInterval(30);


            connOpts.setUserName("use-token-auth");
            connOpts.setPassword(token.toCharArray());


            System.out.println("Connecting to broker: " + broker);
            client.connect(connOpts);
            System.out.println("Connected");

            //client.disconnect();
            //System.out.println("Disconnected");

        } catch (MqttException me) {
            System.out.println("| reason " + me.getReasonCode());
            System.out.println("| msg " + me.getMessage());
            System.out.println("| loc " + me.getLocalizedMessage());
            System.out.println("| cause " + me.getCause());
            System.out.println("| excep " + me);
            me.printStackTrace();
        }
    }

    public void bluemixQuickstartSend() {
        //String topic        = "iot-2/type/iotqs-sensor/id/myDeviceID/evt/status/fmt/json";
        //String topic        = "iot-2/evt/iotsensor/fmt/jon";
        String topic = "iot-2/evt/" + eventID + "/fmt/json";


        // int value = dataGenerator();

        StringBuilder newContent = new StringBuilder();
        newContent.append("{ \"d\" : { ");

        if (replayFileLocation.equals("random")) {
            int pos = 0;
            ValueWrapper valueWrapper = new ValueWrapper();
            for (SensorData s : sensors.getList()) {
                String value = String.valueOf(dataGenerator(pos, Integer.parseInt(s.getMinValue()), Integer.parseInt(s.getMaxValue())));
                newContent.append("\"" + s.getName() + "\" : " + value);
                if (!s.equals(sensors.getList().get(sensors.getList().size() - 1))) {
                    newContent.append(" , ");
                }
                pos++;

                Value logValue = new Value(s.getName(), value);
                valueWrapper.addValue(logValue);

            }
            clog.addLog(valueWrapper);
        } else {

            int i = replayCounter % replayData.getCnt();

            String x = replayData.getList().get(i).toString();
            x = x.substring(1, x.length() - 1);
            x = x.replaceAll("=", "\" : ");
            x = x.replaceAll(", ", ", \"");
            x = "\"" + x;
            System.out.println(x);
            newContent.append(x);


            replayCounter++;
        }

        //newContent.append("\"main\": {\"temp\": 8,\"pressure\": 1020,\"humidity\": 75,\"temp_min\": 8,\"temp_max\": 8\t}");


        newContent.append(" } }");


        //String content = "{ \"d\" : { \"data\" : " + value + " } }";


        try {
            MqttMessage message = new MqttMessage(newContent.toString().getBytes());
            message.setQos(0);

            System.out.println("before publish isConnected " + client.isConnected());
            client.publish(topic, message);
            System.out.println("isConnected " + client.isConnected());

            System.out.println("Message published " + newContent + " TO " + topic);
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


        System.out.println(type);
        if (Objects.equals(type, "Thermostat")) {

            if (prevValue[pos] <= 10) {
                isOn = true;
            }

            if (prevValue[pos] >= 20) {
                isOn = false;
            }

            System.out.println("isOn: " + isOn);

            //TODO: make this not caling it at every call
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    return null;
                }

                @Override
                protected void onPostExecute(Void o) {
                    //Toast.makeText(MobIoTApplication.getActivity(), dID + " command: " + cmd, Toast.LENGTH_LONG).show();
                    if (MobIoTApplication.getActivity() instanceof DevicesActivity) {
                        Message msg = new Message();
                        msg.what = DevicesActivity.MSG_W_SWITCH;
                        Bundle bundle = new Bundle();
                        bundle.putString(DeviceSettingsActivity.KEY_TYPE_ID, typeID);
                        bundle.putString(DeviceSettingsActivity.KEY_DEVICE_ID, deviceID);
                        bundle.putString(DeviceSettingsActivity.KEY_TOKEN, token);

                        bundle.putString(DeviceSettingsActivity.KEY_FREQ, String.valueOf(freq));
                        bundle.putString(DeviceSettingsActivity.KEY_SENSORS, sensors.toString());
                        bundle.putString(DeviceSettingsActivity.KEY_REPLAY_LOCATION, replayFileLocation);
                        msg.setData(bundle);
                        msg.setTarget(((DevicesActivity) MobIoTApplication.getActivity()).handler);
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

    public String getTypeID() {
        return typeID;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean w) {
        this.warning = w;
    }

    public String getOrganizationID() {
        return organizationID;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getReplayFileLocation() {
        return replayFileLocation;
    }

    public double getFreq() {
        return freq;
    }

    public SensorDataWrapper getSensors() {
        return sensors;
    }

    public boolean isOn() {
        return isOn;
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

        if (topic.equals("iot-2/cmd/" + commandID
                + "/fmt/json")) {

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
                    Toast.makeText(MobIoTApplication.getActivity(), dID + " command: " + cmd, Toast.LENGTH_LONG).show();
                    if (MobIoTApplication.getActivity() instanceof DevicesActivity) {
                        Message msg = new Message();
                        msg.what = DevicesActivity.MSG_W_WARNING;
                        Bundle bundle = new Bundle();
                        bundle.putString(DeviceSettingsActivity.KEY_TYPE_ID, typeID);
                        bundle.putString(DeviceSettingsActivity.KEY_DEVICE_ID, deviceID);
                        bundle.putString(DeviceSettingsActivity.KEY_TOKEN, token);

                        bundle.putString(DeviceSettingsActivity.KEY_FREQ, String.valueOf(freq));
                        bundle.putString(DeviceSettingsActivity.KEY_SENSORS, sensors.toString());
                        bundle.putString(DeviceSettingsActivity.KEY_REPLAY_LOCATION, replayFileLocation);
                        msg.setData(bundle);
                        msg.setTarget(((DevicesActivity) MobIoTApplication.getActivity()).handler);
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

    private Generic getReplayFromJson() {

        Generic obj = null;
        try {

            System.out.println("JsonString: " + MobIoTApplication.getStringFromFile(replayFileLocation));

            String jsonStr = MobIoTApplication.getStringFromFile(replayFileLocation);
            obj = gson.fromJson(jsonStr, Generic.class);

        } catch (Exception e) {
            System.out.println("Device" + " File select error" + e);
        }
        return obj;

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("deliveryComplete token:" + token.toString());

    }


    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Device) {
            Device other = (Device) o;
            if (other.getOrganizationID() != null) {
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
            }
            if (!other.getDeviceID().equals(deviceID)) {
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

            if (other.getReplayFileLocation() != replayFileLocation) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    public String getSerial() {
        StringBuilder sb = new StringBuilder();
        sb.append(organizationID);
        sb.append("|");
        sb.append(typeID);
        sb.append("|");
        sb.append(deviceID);
        sb.append("|");
        sb.append(token);
        sb.append("|");
        sb.append(type);
        sb.append("|");
        sb.append(freq);
        sb.append("|");
        sb.append(sensors);
        sb.append("|");
        sb.append(replayFileLocation);
        return sb.toString();
    }


    @Override
    public String toString() {
        return "Device{" +
                "organizationID='" + organizationID + '\'' +
                ", typeID='" + typeID + '\'' +
                ", deviceID='" + deviceID + '\'' +
                ", token='" + token + '\'' +
                ", type='" + type + '\'' +
                ", freq=" + freq + '\'' +
                ", sensors='" + sensors +
                ", replayFileLocation='" + replayFileLocation +
                '}';
    }
}
