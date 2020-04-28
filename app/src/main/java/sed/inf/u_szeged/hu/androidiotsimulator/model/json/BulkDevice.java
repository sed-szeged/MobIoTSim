package sed.inf.u_szeged.hu.androidiotsimulator.model.json;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import sed.inf.u_szeged.hu.androidiotsimulator.model.device.Device;

/**
 * Created by tommy on 2/25/2017. Project name: MobIoTSim-mirrored
 *  
 */
public class BulkDevice {

    private String typeId;



    private String deviceId;
    private String authToken;



    private String name;
    private String password;
    private String type;
    private String topics;

    public BulkDevice(Device device) {
        //this.typeId = device.getTypeID();
        this.deviceId = device.getDeviceID();
        this.authToken = device.getToken();
        this.password = device.getPassword();
        this.type = device.getType();
        this.topics = device.getTopics();
    }

    public String getDeviceId() { return deviceId; }

    public void setDeviceId(String deviceId) {this.deviceId = deviceId;}

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getTopics() { return topics; }

    public void setTopics(String topics) { this.topics = topics; }
}
