package sed.inf.u_szeged.hu.androidiotsimulator.model.json;

import sed.inf.u_szeged.hu.androidiotsimulator.model.device.Device;

/**
 * Created by tommy on 2/25/2017. Project name: MobIoTSim-mirrored
 *  
 */
public class BulkDevice {

    private String typeId;
    private String deviceId;
    private String authToken;

    public BulkDevice(Device device) {
        this.typeId = device.getTypeID();
        this.deviceId = device.getDeviceID();
        this.authToken = device.getToken();
    }

    public String getAddJsonCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\"typeId\" : \"").append(typeId).append("\",\n");
        sb.append("\"deviceId\"  : \"").append(deviceId).append("\",\n");
        sb.append("\"metadata\" : {},\n");
        sb.append("\"authToken\" : \"").append(authToken).append("\"\n");
        sb.append("}");
        return sb.toString();
    }

    public String getRemoveJsonCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\"typeId\" : \"").append(typeId).append("\",\n");
        sb.append("\"deviceId\"  : \"").append(deviceId).append("\" \n");
        sb.append("}");
        return sb.toString();
    }

}
