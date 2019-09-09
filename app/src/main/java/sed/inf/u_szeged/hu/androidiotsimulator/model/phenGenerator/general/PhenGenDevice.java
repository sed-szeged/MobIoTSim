package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.Device;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.DeviceGroup;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.SensorData;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.SensorDataWrapper;

public class PhenGenDevice extends Device {

    /**
     * Devices with deviceOn = true will be run, when all devices are started,
     * and the others will not.
     */
    protected boolean deviceOn = false;
    protected PhenGenDeviceType pgdeviceType;
    protected String productionSerialNumber;
    protected DeviceGroup deviceGroup;
    /*
     * <sensorData name, measurement>
     */
    protected Map<String, Measurement> attachedMeasurements = new LinkedHashMap<>();

    protected static char[] tokenChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789-_".toCharArray();
    protected static Random rnd = new Random();
    protected static long lastDeviceID = 0;
    protected static long lastSerialNumber = 0;

    private PhenGenDevice(String organizationID, String typeID, String deviceID, String token,
                         String type, double freq, SensorDataWrapper sensors,
                         String traceFileLocation, int numOfDevices, boolean saveTrace) {
        super(organizationID, typeID, deviceID, token, type, freq, sensors, traceFileLocation,
                numOfDevices, saveTrace);
    }

    public static PhenGenDevice getPGDevice(PhenGenDeviceType pgdeviceType) {
        String organizationID = MobIoTApplication.loadData(CloudSettingsActivity.KEY_ORGANIZATION_ID);
        String typeID = pgdeviceType.deviceOuterType;
        String deviceID = generateDeviceID(pgdeviceType, 3);
        String token = generateDeviceToken(pgdeviceType, 10);
        String type = pgdeviceType.deviceInnerType;
        double freq = 1.0;
        ArrayList<SensorData> sensorDataList = new ArrayList<>();
        for (PhenGenDeviceSensorType pgdsensorType : pgdeviceType.sensorTypes) {
            //min and max values are not important here (they won't be used for data gerenation)
            sensorDataList.add(new SensorData(pgdsensorType.getName(), "0", "0"));
        }
        SensorDataWrapper sensors = new SensorDataWrapper(sensorDataList);

        PhenGenDevice newPGDevice = new PhenGenDevice(organizationID, typeID, deviceID, token,
                type, freq, sensors, "random", 1, false);
        newPGDevice.pgdeviceType = pgdeviceType;
        newPGDevice.productionSerialNumber = generateSerialNumber(pgdeviceType,
                5, 20);
        return newPGDevice;
    }



    protected static String generateSerialNumber(PhenGenDeviceType pgdeviceType, int numberLength,
                                                 int serialMaxGrowth) {
        lastSerialNumber = lastSerialNumber + 1 + rnd.nextInt(serialMaxGrowth);
        return pgdeviceType.deviceModel + "-"
                + generateStringFromNumber(lastSerialNumber, numberLength);
    }

    protected static String generateDeviceID(PhenGenDeviceType pgdeviceType, int idNumberLength) {
        return pgdeviceType.deviceInnerType
                + generateStringFromNumber(++lastDeviceID, idNumberLength);
    }

    protected static String generateDeviceToken(PhenGenDeviceType pgdeviceType, int tokenLength) {
        StringBuilder token = new StringBuilder();
        for(int i = 0; i < tokenLength; i++) token.append( tokenChars[rnd.nextInt(tokenChars.length)]);
        return token.toString();
    }

    /*
    Generates a string from the number with leading zeros.
     */
    public static String generateStringFromNumber(long originalNumber, int stringLength) {
        String strOriginalNumber = String.valueOf(originalNumber);
        StringBuilder strFull = new StringBuilder("");
        int zeroCount = Math.max(0, stringLength - strOriginalNumber.length());
        for (int i = 0; i < zeroCount; i++) strFull.append('0');
        strFull.append(strOriginalNumber);
        return strFull.toString();
    }

    public String getMessageContent() {
        StringBuilder content = new StringBuilder("");
        if (! deviceOn) return null;

        //Check
        String subjectId = getSubjectId();
        String deviceId = getDeviceId();
        if (subjectId != null || deviceId != null) {
            content.append("\"check\" : {");
            if (subjectId != null) content.append("\"subject_id\" : \"" + subjectId + "\"");
            if (subjectId != null && deviceId != null) content.append(',');
            if (deviceId != null) content.append("\"device_serial\" : \"" + deviceId + "\"");
            content.append("}\n");
        }
        //Sensors
        if (isCurrentlySendingData()) {
            if (content.length() > 0) content.append(",\n");
            content.append("\"sensors\" : [");
            for (SensorData s : sensors.getList()) {
                content.append("{");
                String dataValue = dataGenerator(s);    //It should be a correct JSON formed value
                content.append("\"value\" : " + dataValue);
                String dataType = getSensorTypeToSend(s.getName());
                if (dataType != null) content.append(", \"type\" : \"" + dataType + "\"");
                String dataUnit = getSensorUnitToSend(s.getName());
                if (dataUnit != null) content.append(", \"unit\" : \"" + dataUnit + "\"");
                String dataInfo = getSensorInfoToSend(s.getName());
                if (dataInfo != null) content.append(", \"info\" : \"" + dataInfo + "\"");

                content.append("}");
                if (!s.equals(sensors.getList().get(sensors.getList().size() - 1))) {
                    content.append(", ");
                }
            }
            content.append("]\n");
        }
        //Meta
        String measure_time = getMeasurementTime();
        String general_info = getGeneralInfo();
        if (measure_time != null || general_info != null) {
            if (content.length() > 0) content.append(",\n");
            content.append("\"meta\" : {");
            if (measure_time != null) content.append("\"measure_time\" : \"" + measure_time + "\"");
            if (measure_time != null && general_info != null) content.append(',');
            if (general_info != null) content.append("\"info\" : \"" + general_info + "\"");
            content.append("}\n");
        }
        //Device_status
        String[][] status_info = getDeviceStatusInfo();
        if (status_info != null) {
            if (content.length() > 0) content.append(",\n");
            content.append(content.append("\"device_status\" : {"));
            for (int i=0; i<status_info.length; i++) {
                if (i > 0) content.append(',');
                content.append("\"info\":{");
                content.append("\"type\" : \"" + status_info[i][0] + "\", ");
                content.append("\"description\" : \"" + status_info[i][1] + "\"");
                content.append("}");
            }
            content.append("}\n");
        }
        return content.toString();
    }

    protected String getSubjectId() {
        for (Measurement measurement : getAttachedMeasurements().values()) {
            return measurement.subject.getSubjectId();
        }
        return null;    //when empty
    }
    protected String getDeviceId() {
        return productionSerialNumber;
    }
    protected boolean isCurrentlySendingData() {
        return attachedMeasurements.isEmpty() ? false : true;
    }
    protected String getSensorTypeToSend(String pgdSensorType) {
        return pgdSensorType;   //it's the same by default
                                //when null, then type won't be sent
    }
    protected String getSensorUnitToSend(String pgdSensorType) {
        return attachedMeasurements.get(pgdSensorType).sensorType.getUnit();
    }
    protected String getSensorInfoToSend(String pgdSensorType) {
        return null;
    }
    protected String getMeasurementTime() {
        return null;    //TODO: delete this method, and use firstMeasurementData's measure_time in getMessageContent
    }
    protected String getGeneralInfo() {
        return null;
    }
    protected String[][] getDeviceStatusInfo() {
        return null;
    }


    @Override
    protected String dataGenerator(SensorData sensordata) {
        Measurement measurement = attachedMeasurements.get(sensordata.getName());
        if (measurement != null) {
            return measurement.getData();
        } else {
            return null;
        }
    }

    public PhenGenDeviceType getPgdeviceType() {
        return pgdeviceType;
    }
    public String getProductionSerialNumber() {
        return productionSerialNumber;
    }
    public DeviceGroup getDeviceGroup() { return deviceGroup; }
    public Map<String, Measurement> getAttachedMeasurements() {
        return attachedMeasurements;
    }
    public boolean isDeviceOn() { return deviceOn; }

    public void setPassword(String password) {
        this.token = password;
    }
    public void setProductSerialValue(String serialValue) {
        this.productionSerialNumber = serialValue;
    }
    public void setFreq(double freq) {
        this.freq = freq;
    }

    public void setDeviceGroup(DeviceGroup group) {
        this.deviceGroup = group;
    }

    //Should create a new Parameter, if it changes any value
    // (otherwise the Subject's real values would be modified)
    protected Parameter getMeasuredParameter(Parameter originalParameter) {
        return originalParameter;
    }

}
