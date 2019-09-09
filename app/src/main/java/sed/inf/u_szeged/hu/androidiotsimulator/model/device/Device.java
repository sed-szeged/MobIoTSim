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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.StringTokenizer;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DeviceSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.model.cloudsettings.CloudSettingsWrapper;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.device.GsonDevice;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.device.Sensor;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.openweather.OpenweatherTrace;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.randomdata.FinishedTrace;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.randomdata.Parameter;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.trace.randomdata.ParameterWrapper;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenDevice;

public class Device implements Runnable, MqttCallback {

    volatile boolean isRunning = false;
    private Gson gson = new Gson();
    private String type;
    @Expose
    private MqttClient client;
    @Expose
    private
    Random random;
    protected String organizationID;
    protected String typeID;
    protected String deviceID;
    protected String token;
    // TODO: remove this two
    private String commandID;
    private String eventID;
    //data generation
    private String traceFileLocation;
    //String format;
    private boolean warning = false;
    private float[] prevValue;
    private int numOfDevices;
    protected double freq;
    protected SensorDataWrapper sensors;
    private boolean isOn = false;
    private int cnt;
    private int traceCounter;
    private boolean saveTrace;
    private FinishedTrace clog;

    private boolean hasTraceData;
    private OpenweatherTrace openweatherTraceData;

    public Device(Device copyDevice) {
        this.isRunning = copyDevice.isRunning();
        this.gson = copyDevice.getGson();
        this.type = copyDevice.getType();
        this.client = copyDevice.getClient();
        this.random = copyDevice.getRandom();
        this.organizationID = copyDevice.getOrganizationID();
        this.typeID = copyDevice.getTypeID();
        this.deviceID = copyDevice.getDeviceID();
        this.token = copyDevice.getToken();
        this.commandID = copyDevice.getCommandID();
        this.eventID = copyDevice.getEventID();
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
    }

    public Device(String organizationID, String typeID, String deviceID, String token, String type, double freq, SensorDataWrapper sensors, String traceFileLocation, int numOfDevices, boolean saveTrace) {
        this.organizationID = organizationID;
        this.token = token;
        this.typeID = typeID;
        this.type = type;
        this.deviceID = deviceID;
        this.prevValue = new float[sensors.getList().size()];
        this.freq = freq;
        this.sensors = sensors;
        this.traceFileLocation = traceFileLocation;
        this.numOfDevices = numOfDevices;
        this.saveTrace = saveTrace;
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
        double freq = Double.parseDouble(st.nextToken());
        SensorDataWrapper sensorDataWrapper = SensorDataWrapper.sensorDataFromSerial(st.nextToken());
        String traceFileLocation = st.nextToken();
        int numOfDevices = Integer.parseInt(st.nextToken());
        boolean saveTrace = Boolean.parseBoolean(st.nextToken());

        Device d = new Device(organizationID, typeID, deviceID, token, type, freq, sensorDataWrapper, traceFileLocation, numOfDevices, saveTrace);
        return d;
    }

    public static Device fromJson(GsonDevice gsonDevice) {

        String organizationID = gsonDevice.getOrganizationId();
        String typeID = gsonDevice.getTypeId();
        String deviceID = gsonDevice.getDeviceId();
        String token = gsonDevice.getToken();
        String type = gsonDevice.getType();
        double freq = Double.parseDouble(String.valueOf(gsonDevice.getFreq()));
        int numOfDevices = gsonDevice.getNumOfDevices();
        boolean saveTrace = gsonDevice.isSaveTrace();

        SensorDataWrapper sensorDataWrapper = new SensorDataWrapper();
        for (Sensor s : gsonDevice.getSensors()) {
            SensorData sd = new SensorData(s.getName(), String.valueOf(s.getMin()), String.valueOf(s.getMax()));
            sensorDataWrapper.addSensor(sd);
        }

        String traceFileLocation = gsonDevice.getTraceFileLocation();

        Device d = new Device(organizationID, typeID, deviceID, token, type, freq, sensorDataWrapper, traceFileLocation, numOfDevices, saveTrace);
        return d;

    }


    @Override
    public void run() {

        connectToBroker();

        isRunning = true;
        isOn = true;
        client.setCallback(this);

        subscribeToTopic();

        clog = new FinishedTrace();
        while (isRunning) {
            try {
                sendToTopic();
                Thread.currentThread().sleep((long) (freq * 1000));
            } catch (InterruptedException e) {
                //running = false;
            }
        }
    }

    private void connectToBroker() {
        CloudSettingsWrapper.CSType cloudType = CloudSettingsWrapper.CSType.valueOf(
                        MobIoTApplication.loadData(CloudSettingsActivity.KEY_TYPE) );
        String broker_url = MobIoTApplication.loadData(CloudSettingsActivity.KEY_BROKER_URL);
        String clientId = "";
        String username = "";
        String password = "";

        switch(cloudType) {
            case BLUEMIX_DEMO:
            case BLUEMIX:
                clientId = "d:" + organizationID + ":" + typeID + ":" + deviceID;
                username = "use-token-auth";
                password = token;
                break;
            case EDGE_GATEWAY:
                clientId = deviceID;    //can be anything else
                username = deviceID;
                password = token;
                break;
            default:
                //TODO: find a generalized solution if it is possible
        }

        try {
            if (client != null) {
                try {
                    client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                client = null;
            }

            client = new MqttClient(broker_url, clientId, null);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());

            System.out.println("Connecting to broker: " + broker_url +
                    "\n   clientID: " + clientId +
                    "\n   username: " + username + ", password: " + token);
            client.connect(connOpts);
            System.out.println("Connected");

        } catch (MqttException me) {
            System.out.println("| reason " + me.getReasonCode());
            System.out.println("| msg " + me.getMessage());
            System.out.println("| loc " + me.getLocalizedMessage());
            System.out.println("| cause " + me.getCause());
            System.out.println("| excep " + me);
            me.printStackTrace();
        }
    }

    private void subscribeToTopic() {
        //TODO
    }

    private void sendToTopic() {
        CloudSettingsWrapper.CSType cloudType = CloudSettingsWrapper.CSType.valueOf(
                MobIoTApplication.loadData(CloudSettingsActivity.KEY_TYPE) );
        String eventID = MobIoTApplication.loadData(CloudSettingsActivity.KEY_EVENT_ID);
        String topic = "";

        switch (cloudType) {
            case BLUEMIX_DEMO:
                break;
            case BLUEMIX:
                topic = "iot-2/evt/" + eventID + "/fmt/json";
                break;
            case EDGE_GATEWAY:
                String device_type = typeID;
                String device_id = deviceID;
                topic = "/device/" + device_type + "/" + device_id;
                break;
            default:
                //TODO: find a generalized solution if it is possible
        }
        System.out.println("Topic: " + topic);

        StringBuilder newContent = new StringBuilder();
        newContent.append("{ \"d\" : { ");

        if (this instanceof PhenGenDevice) {
            String content = ((PhenGenDevice) this).getMessageContent();
            newContent.append( content );
        } else if (traceFileLocation.equals("random")) {
            int pos = 0;
            ParameterWrapper parameterWrapper = new ParameterWrapper();
            for (SensorData s : sensors.getList()) {
                String value = dataGenerator(s);
                if (value == null) continue;    //there was no data available
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

                System.out.println(output.toString());
                newContent.append(output.toString());

                traceCounter++;
            } else {

                int i = traceCounter % openweatherTraceData.getCntcycles();

                String end = deviceID.substring(deviceID.lastIndexOf("_") + 1);
                int deviceNum = Integer.parseInt(end) % openweatherTraceData.getCycles().get(0).getCnt();

                StringBuilder output = new StringBuilder();
                output.append(new Gson().toJson(openweatherTraceData.getCycles().get(i).getList().get(deviceNum), Map.class));
                System.out.println("GSON FIX TEST:" + new Gson().toJson(openweatherTraceData.getCycles().get(i).getList().get(deviceNum), Map.class));

                output.deleteCharAt(0);
                output.deleteCharAt(output.length() - 1);

                newContent.append(output.toString());
                traceCounter++;
            }
        }

        newContent.append(" } }");

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
        if (traceFileLocation.equals("random")) {
            //saveLog(ctx);
        }
    }

    /*public void connectEdgeGateway() {
        String broker_prefix = "tcp";
        String broker_address = "192.168.0.13";
        String broker_port = "1883";
        //String device_type = "devtip1";
        String device_id = "probaDev01";
        String device_token = "asdfg";
        //String message_schema = "{\"d\":%s}";
        //String message_content_schema = "{\"value\":%s}";

        String broker = broker_prefix + "://" + broker_address +
                (broker_port.isEmpty() ? "" : ":" + broker_port);

        String clientId = device_id;
        //String clientId = "d:" + organizationID + ":" + typeID + ":" + deviceID;

        try {
            if (client != null) {
                try {
                    client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                client = null;
            }

            client = new MqttClient(broker, "myClient", null);

            MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                connOpts.setUserName(device_id);
                connOpts.setPassword(device_token.toCharArray());

            System.out.println("Connecting to broker: " + broker +
                    "\n   clientID: " + clientId +
                    "\n   username: " + connOpts.getUserName() + ", password: " + token);
            client.connect(connOpts);
            System.out.println("Connected");

        } catch (MqttException me) {
            System.out.println("| reason " + me.getReasonCode());
            System.out.println("| msg " + me.getMessage());
            System.out.println("| loc " + me.getLocalizedMessage());
            System.out.println("| cause " + me.getCause());
            System.out.println("| excep " + me);
            me.printStackTrace();
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
        //https://console.bluemix.net/docs/services/IoT/reference/security/connect_devices_apps_gw.html#client_port_security
        String broker_prefix = "ssl://";        //PL
        String broker_suffix = ".messaging.internetofthings.ibmcloud.com:443";      //PL
        //String broker_prefix = "tcp://";
        //String broker_suffix = ".messaging.internetofthings.ibmcloud.com:8883";
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


            System.out.println("Connecting to broker: " + broker +
                "\n   clientID: " + clientId +
                "\n   username: " + connOpts.getUserName() + ", password: " + token);
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

    public void edgeGatewaySend() {
        String broker_prefix = "ssl";
        String broker_address = "192.168.0.13";
        String broker_port = "1883";
        String device_type = "devtip1";
        String device_id = "probaDev01";
        String device_token = "asdfg";
        String topic = "/device/" + device_type + "/" + device_id;

        StringBuilder newContent = new StringBuilder();

        newContent.append("{ \"d\" : { ");

        if (traceFileLocation.equals("random")) {
            int pos = 0;
            ParameterWrapper parameterWrapper = new ParameterWrapper();
                for (SensorData s : sensors.getList()) {
                    String value = String.valueOf(originalFloatDataGenerator(pos, Float.parseFloat(s.getMinValue()), Float.parseFloat(s.getMaxValue())));
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

                System.out.println(output.toString());
                newContent.append(output.toString());

                traceCounter++;
            } else {

                int i = traceCounter % openweatherTraceData.getCntcycles();

                String end = deviceID.substring(deviceID.lastIndexOf("_") + 1);
                int deviceNum = Integer.parseInt(end) % openweatherTraceData.getCycles().get(0).getCnt();

                StringBuilder output = new StringBuilder();
                output.append(new Gson().toJson(openweatherTraceData.getCycles().get(i).getList().get(deviceNum), Map.class));
                System.out.println("GSON FIX TEST:" + new Gson().toJson(openweatherTraceData.getCycles().get(i).getList().get(deviceNum), Map.class));

                output.deleteCharAt(0);
                output.deleteCharAt(output.length() - 1);

                newContent.append(output.toString());
                traceCounter++;
            }
        }

        newContent.append(" } }");

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

    public void bluemixQuickstartSend() {
        //String topic        = "iot-2/type/iotqs-sensor/id/myDeviceID/evt/status/fmt/json";
        //String topic        = "iot-2/evt/iotsensor/fmt/jon";
        String topic = "iot-2/evt/" + eventID + "/fmt/json";


        // int value = dataGenerator();

        StringBuilder newContent = new StringBuilder();


        if (type.equals("Weathergroup")) {

            String txt = DataGenerator.getNextWeatherGroupRandomMsg();
            System.out.println(txt);
            newContent.append(txt);
        } else {

            newContent.append("{ \"d\" : { ");

            if (traceFileLocation.equals("random")) {
                int pos = 0;
                ParameterWrapper parameterWrapper = new ParameterWrapper();
                for (SensorData s : sensors.getList()) {
                    String value = String.valueOf(originalFloatDataGenerator(s));
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


                    System.out.println(output.toString());
                    newContent.append(output.toString());

                    traceCounter++;
                } else {

                    int i = traceCounter % openweatherTraceData.getCntcycles();

                    String end = deviceID.substring(deviceID.lastIndexOf("_") + 1);
                    int deviceNum = Integer.parseInt(end) % openweatherTraceData.getCycles().get(0).getCnt();

                    StringBuilder output = new StringBuilder();
                    output.append(new Gson().toJson(openweatherTraceData.getCycles().get(i).getList().get(deviceNum), Map.class));
                    System.out.println("GSON FIX TEST:" + new Gson().toJson(openweatherTraceData.getCycles().get(i).getList().get(deviceNum), Map.class));


                    output.deleteCharAt(0);
                    output.deleteCharAt(output.length() - 1);

                    newContent.append(output.toString());
                    traceCounter++;
                }
            }

            newContent.append(" } }");

        }
        //newContent.append("\"main\": {\"temp\": 8,\"pressure\": 1020,\"humidity\": 75,\"temp_min\": 8,\"temp_max\": 8\t}");


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
    }*/

    protected String dataGenerator(SensorData sensordata) {
        return String.valueOf(originalFloatDataGenerator(sensordata));
    }

    private float originalFloatDataGenerator(SensorData sensorData) {
        int pos = sensors.sensorDataList.indexOf(sensorData);
        int diff = random.nextInt(3);

        System.out.println(type);
        if (Objects.equals(type, "SimpleCGM")) {
            float NORMAL_MIN=4.0F, NORMAL_MAX=6.0F;
            float ABSOLUTE_MIN=2.0F, ABSOLUTE_MAX=20.0F;
            //isOn: active intervention in glucose level
            if (prevValue[pos]<NORMAL_MIN) {    //low level glucose
                isOn = isOn || (random.nextFloat()<0.2F);   //quickly recognizable
                if (isOn) { //handling low level glucose
                    prevValue[pos] += Math.round((random.nextFloat()*2) * 10.0F) / 10.0;
                } else { //unhandled falling glucose level
                    prevValue[pos] -= Math.round((random.nextFloat()*2) * 10.0F) / 10.0;
                }
                prevValue[pos] = Math.max(prevValue[pos], ABSOLUTE_MIN);
            } else if (prevValue[pos]>NORMAL_MAX) { //high level glucose
                isOn = isOn || (random.nextFloat()<0.1F);  //may be recognized later
                if (isOn) { //handling high level glucose
                    prevValue[pos] -= Math.round((random.nextFloat()*2) * 10.0F) / 10.0;
                } else { //unhandled falling glucose level
                    prevValue[pos] += Math.round((random.nextFloat()*4) * 10.0F) / 10.0;
                }
                prevValue[pos] = Math.min(prevValue[pos], ABSOLUTE_MAX);
            } else {    //normal level glucose
                isOn = false;
                if (random.nextFloat()>0.10F) { //stable glucose
                    prevValue[pos] += Math.round((random.nextFloat() - 0.5F) * 10.0F) / 10.0;
                } else {    //unsteady glucose
                    prevValue[pos] += Math.round((random.nextFloat() * 4 - 2) * 10.0F) / 10.0;
                }
            }

        } else if (Objects.equals(type, "Thermostat")) {

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
                        bundle.putString(DeviceSettingsActivity.KEY_TRACE_LOCATION, traceFileLocation);
                        bundle.putString(DeviceSettingsActivity.KEY_SAVE_TRACE, String.valueOf(saveTrace));
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

    public String getTraceFileLocation() {
        return traceFileLocation;
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

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
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
                        bundle.putString(DeviceSettingsActivity.KEY_TRACE_LOCATION, traceFileLocation);
                        bundle.putString(DeviceSettingsActivity.KEY_SAVE_TRACE, String.valueOf(saveTrace));
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

            if (other.getTraceFileLocation() != traceFileLocation) {
                return false;
            }

            if (other.isSaveTrace() != saveTrace) {
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
        sb.append(traceFileLocation);
        sb.append("|");
        sb.append(numOfDevices);
        sb.append("|");
        sb.append(saveTrace);
        return sb.toString();
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }


    public Gson getGson() {
        return gson;
    }

    public MqttClient getClient() {
        return client;
    }

    public Random getRandom() {
        return random;
    }

    public String getCommandID() {
        return commandID;
    }

    public String getEventID() {
        return eventID;
    }

    public float[] getPrevValue() {
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
                ", organizationID='" + organizationID + '\'' +
                ", typeID='" + typeID + '\'' +
                ", deviceID='" + deviceID + '\'' +
                ", token='" + token + '\'' +
                ", commandID='" + commandID + '\'' +
                ", eventID='" + eventID + '\'' +
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
                '}';
    }

}
