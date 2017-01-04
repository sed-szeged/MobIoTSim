package sed.inf.u_szeged.hu.androidiotsimulator.model.device;

import android.hardware.Sensor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

import com.google.gson.annotations.Expose;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.StringTokenizer;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.DeviceSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.DevicesActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.JsonDevice;

public class Device implements Runnable, MqttCallback {

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

    private String commandID;
    private String eventID;

    //String format;
    private boolean warning = false;
    //data generation

    private int[] prevValue;
    private double freq;

    private SensorDataWrapper sensors;

    volatile boolean isRunning = false;

    private boolean isOn = false;


    public Device(String organizationID, String typeID, String deviceID, String token, String type, String commandID, String eventID, double freq, SensorDataWrapper sensors) {
        this.organizationID = organizationID;
        this.token = token;
        this.commandID = commandID;
        this.eventID = eventID;
        this.typeID = typeID;
        this.type = type;
        this.deviceID = deviceID;
        this.prevValue = new int[sensors.getList().size()];
        this.freq = freq;
        this.sensors = sensors;

        this.random = new Random();

        for (int j = 0; j < sensors.getList().size(); j++) {
            prevValue[j] = (Integer.parseInt(sensors.getList().get(j).getMaxValue()) + Integer.parseInt(sensors.getList().get(j).getMinValue())) / 2;
        }


    }

    public static Device fromSerial(String str) {
        StringTokenizer st = new StringTokenizer(str, "|");

        String organizationID = st.nextToken();
        String typeID = st.nextToken();
        String deviceID = st.nextToken();
        String token = st.nextToken();
        String type = st.nextToken();
        String commandID = st.nextToken();
        String eventID = st.nextToken();
        double freq = Double.parseDouble(st.nextToken());
        SensorDataWrapper sensorDataWrapper = SensorDataWrapper.sensorDataFromSerial(st.nextToken());


        Device d = new Device(organizationID, typeID, deviceID, token, type, commandID, eventID, freq, sensorDataWrapper);
        return d;
    }

    public static Device fromJson(JsonDevice jsonDevice) {

        String organizationID = jsonDevice.getOrganizationId();
        String typeID = jsonDevice.getTypeId();
        String deviceID = jsonDevice.getDeviceId();
        String token = jsonDevice.getToken();
        String type = jsonDevice.getType();
        String commandID = jsonDevice.getCommand();
        String eventID = jsonDevice.getStatus();
        double freq = Double.parseDouble(String.valueOf(jsonDevice.getFreq()));

        SensorDataWrapper sensorDataWrapper = new SensorDataWrapper();
        for ( sed.inf.u_szeged.hu.androidiotsimulator.model.gson.Sensor s : jsonDevice.getSensors()) {
            SensorData sd = new SensorData(s.getName(), String.valueOf(s.getMin()), String.valueOf(s.getMax()));
            sensorDataWrapper.addSensor(sd);
        }


        Device d = new Device(organizationID, typeID, deviceID, token, type, commandID, eventID, freq, sensorDataWrapper);
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

    public void stop() {
        isRunning = false;
    }

    public void bluemixQuickstartConnect() {
        //String broker       = "tcp://quickstart.messaging.internetofthings.ibmcloud.com:1883";
        //String broker       = "tcp://quickstart.messaging.internetofthings.ibmcloud.com:1883";

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

        int pos = 0;
        for (SensorData s : sensors.getList()) {
            newContent.append("\"" + s.getName() + "\" : " + dataGenerator(pos, Integer.parseInt(s.getMinValue()), Integer.parseInt(s.getMaxValue())));
            if (!s.equals(sensors.getList().get(sensors.getList().size() - 1))) {
                newContent.append(" , ");
            }
            pos++;

        }


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

    public String getOrganizationID() {
        return organizationID;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getCommandID() {
        return commandID;
    }

    public String getEventID() {
        return eventID;
    }

    public double getFreq() {
        return freq;
    }

    public SensorDataWrapper getSensors() {
        return sensors;
    }

    public void setWarning(boolean w) {
        this.warning = w;
    }

    public boolean isOn(){
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
        sb.append(commandID);
        sb.append("|");
        sb.append(eventID);
        sb.append("|");
        sb.append(freq);
        sb.append("|");
        sb.append(sensors);
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
                ", commandID='" + commandID + '\'' +
                ", eventID='" + eventID + '\'' +
                ", freq=" + freq + '\'' +
                ", freq=" + sensors +
                '}';
    }
}
