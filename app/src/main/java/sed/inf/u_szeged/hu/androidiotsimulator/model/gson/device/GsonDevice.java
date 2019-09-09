package sed.inf.u_szeged.hu.androidiotsimulator.model.gson.device;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GsonDevice {

    @SerializedName("organizationId")
    @Expose
    private String organizationId;
    @SerializedName("typeId")
    @Expose
    private String typeId;
    @SerializedName("deviceId")
    @Expose
    private String deviceId;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("freq")
    @Expose
    private Double freq;
    @SerializedName("sensors")
    @Expose
    private List<Sensor> sensors = new ArrayList<Sensor>();
    @SerializedName("traceFileLocation")
    @Expose
    private String traceFileLocation;
    @SerializedName("numOfDevices")
    @Expose
    private int numOfDevices;
    @SerializedName("saveTrace")
    @Expose
    private boolean saveTrace;

    /**
     * @return The organizationId
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * @param organizationId The organizationId
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * @return The typeId
     */
    public String getTypeId() {
        return typeId;
    }

    /**
     * @param typeId The typeId
     */
    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    /**
     * @return The deviceId
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * @param deviceId The deviceId
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * @return The token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token The token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return The type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return The freq
     */
    public Double getFreq() {
        return freq;
    }

    /**
     * @param freq The freq
     */
    public void setFreq(Double freq) {
        this.freq = freq;
    }

    /**
     * @return The sensors
     */
    public List<Sensor> getSensors() {
        return sensors;
    }

    /**
     * @param sensors The sensors
     */
    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    /**
     * @return The traceFileLocation
     */
    public String getTraceFileLocation() {
        return traceFileLocation;
    }

    /**
     * @param traceFileLocation The traceFileLocation
     */
    public void setTraceFileLocation(String traceFileLocation) {
        this.traceFileLocation = traceFileLocation;
    }

    /**
     * @return The number of devices
     */
    public int getNumOfDevices() {
        return numOfDevices;
    }

    /**
     * @param numOfDevices The numOfDevices
     */
    public void setNumOfDevices(int numOfDevices) {
        this.numOfDevices = numOfDevices;
    }

    public boolean isSaveTrace() {
        return saveTrace;
    }

    public void setSaveTrace(boolean saveTrace) {
        this.saveTrace = saveTrace;
    }
}
