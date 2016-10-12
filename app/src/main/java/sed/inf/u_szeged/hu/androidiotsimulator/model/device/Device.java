package sed.inf.u_szeged.hu.androidiotsimulator.model.device;

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

import java.util.Random;
import java.util.StringTokenizer;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.DeviceSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.DevicesActivity;

/**
 * Created by Tomi on 2016. 01. 21..
 */
public class Device implements Runnable, MqttCallback {

    String type;

    @Expose
    private MqttClient client;

    @Expose
    Random random;

    String organizationID;

    String url = "tcp://quickstart.messaging.internetofthings.ibmcloud.com:1883";

    String typeID;
    String deviceID;

    String token;

    String clientID = "d:quickstart:iotqs-sensor:myDeviceID";

    String commandID;
    String eventID;
    //String format;
    String topic = "iot-2/evt/status/fmt/jon";

    boolean warning = false;

    int qos = 0;

    //data generation
    int min;
    int max;
    double freq;
    String json_schema = "{ \"d\" : { <content> } }";
    String josn_content = "\"data\" : <value>";

    volatile boolean isRunning = false;

    public Device(String organizationID, String typeID, String deviceID, String token, String type, String commandID, String eventID, int min, int max, double freq){
        this.organizationID = organizationID;
        this.token = token;
        this.commandID = commandID;
        this.eventID = eventID;
        this.typeID = typeID;
        this.type = type;
        this.deviceID = deviceID;
        this.min = min;
        this.max = max;
        this.freq = freq;
        this.random = new Random();
    }

    public static Device fromSerial(String str){
        StringTokenizer st = new StringTokenizer(str, "|");

        String organizationID = st.nextToken();
        String typeID = st.nextToken();
        String deviceID = st.nextToken();
        String token = st.nextToken();
        String type = st.nextToken();
        String commandID = st.nextToken();
        String eventID = st.nextToken();
        int min = Integer.parseInt(st.nextToken());
        int max = Integer.parseInt(st.nextToken());
        double freq = Double.parseDouble(st.nextToken());

        Device d = new Device(organizationID, typeID, deviceID, token, type, commandID, eventID, min, max, freq);
        return d;
    }

    @Override
    public void run() {
        bluemixQuickstartConnect();
        isRunning = true;

        client.setCallback(this);

        if(!organizationID.equals("quickstart")) {
            bluemixQuickstartSubscribe();
        }

        while (isRunning) {
            try {
                bluemixQuickstartSend();

                Thread.currentThread().sleep((long)(freq * 1000));

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

    public void stop(){
        isRunning = false;
    }

    public void bluemixQuickstartConnect(){
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

        String clientId     = "d:" + organizationID + ":" + typeID + ":" + deviceID;

        try {
            if(client != null){
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

        } catch(MqttException me) {
            System.out.println("| reason "+me.getReasonCode());
            System.out.println("| msg "+me.getMessage());
            System.out.println("| loc "+me.getLocalizedMessage());
            System.out.println("| cause "+me.getCause());
            System.out.println("| excep "+me);
            me.printStackTrace();
        }
    }

    public void bluemixQuickstartSend(){
        //String topic        = "iot-2/type/iotqs-sensor/id/myDeviceID/evt/status/fmt/json";
        //String topic        = "iot-2/evt/iotsensor/fmt/jon";
        String topic        = "iot-2/evt/" + eventID + "/fmt/json";

        int value = random.nextInt(max-min) + min;
        String content      = "{ \"d\" : { \"data\" : " + value + " } }";

        try {
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(0);

            System.out.println("before publish isConnected " + client.isConnected());
            client.publish(topic, message);
            System.out.println("isConnected " + client.isConnected());

            System.out.println("Message published " + content + " TO " + topic);
        } catch(MqttException me) {
            System.out.println("| reason "+me.getReasonCode());
            System.out.println("| msg "+me.getMessage());
            System.out.println("| loc "+me.getLocalizedMessage());
            System.out.println("| cause "+me.getCause());
            System.out.println("| excep "+me);
            me.printStackTrace();
        }
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getTypeID() {
        return typeID;
    }

    public boolean isRunning(){
        return isRunning;
    }

    public boolean isWarning(){
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

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public double getFreq() {
        return freq;
    }

    public void setWarning(boolean w){
        this.warning = w;
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

            new AsyncTask<Void,Void,Void>(){

                @Override
                protected Void doInBackground(Void... params) {
                    return null;
                }

                @Override
                protected void onPostExecute(Void o) {
                    Toast.makeText(MobIoTApplication.getActivity(), dID + " command: " + cmd, Toast.LENGTH_LONG).show();
                    if(MobIoTApplication.getActivity() instanceof DevicesActivity){
                        Message msg = new Message();
                        msg.what = DevicesActivity.MSG_W_WARNING;
                        Bundle bundle = new Bundle();
                        bundle.putString(DeviceSettingsActivity.KEY_TYPE_ID, typeID);
                        bundle.putString(DeviceSettingsActivity.KEY_DEVICE_ID, deviceID);
                        bundle.putString(DeviceSettingsActivity.KEY_TOKEN, token);

                        bundle.putString(DeviceSettingsActivity.KEY_FREQ, String.valueOf(freq));
                        bundle.putString(DeviceSettingsActivity.KEY_MIN, String.valueOf(min));
                        bundle.putString(DeviceSettingsActivity.KEY_MAX, String.valueOf(max));
                        msg.setData(bundle);
                        msg.setTarget(((DevicesActivity)MobIoTApplication.getActivity()).handler);
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
        if(o==null){
            return false;
        }
        if(o instanceof Device) {
            Device other = (Device) o;
            if (other.getOrganizationID()!=null){
                if (!other.getOrganizationID().equals(organizationID)) {
                    return false;
                }
            }else{
                //TODO !!!
                if(organizationID!=null && !organizationID.equals("null")){
                    return false;
                }

            }
            if(!other.getTypeID().equals(typeID)){
                return false;
            }
            if(!other.getDeviceID().equals(deviceID)){
                return false;
            }
            if(!other.getToken().equals(token)){
                return false;
            }
            if(!other.getType().equals(type)){
                return false;
            }
            if(other.getFreq() != freq){
                return false;
            }
            if(other.getMax() != max){
                return false;
            }
            if(other.getMin() != min){
                return false;
            }
            return true;
        }else{
            return false;
        }
    }

    public String getSerial(){
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
        sb.append(min);
        sb.append("|");
        sb.append(max);
        sb.append("|");
        sb.append(freq);
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
                ", min=" + min +
                ", max=" + max +
                ", freq=" + freq +
                '}';
    }
}
