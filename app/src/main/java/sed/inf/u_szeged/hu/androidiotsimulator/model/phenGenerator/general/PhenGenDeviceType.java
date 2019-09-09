package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PhenGenDeviceType {
    /*
     That's what a gateway sees.
    */
    protected String deviceOuterType;
    /*
     This is the name of the deviceType.
     */
    protected String deviceInnerType;
    /*
     Groups the main behaviour of the devices.
     */
    protected String deviceInnerType_category;
    /*
     Manufacturer of the device
     */
    protected String manufacturer;
    /*
     Device model
     */
    protected String deviceModel;
    /*
     Sensor name_keys and types.
     */
    protected List<PhenGenDeviceSensorType> sensorTypes = new ArrayList<>();

    public PhenGenDeviceType(String deviceOuterType, String deviceInnerType, String deviceInnerType_category,
                             String manufacturer, String deviceModel, PhenGenDeviceSensorType[] sensorTypes) {
        this.deviceOuterType = deviceOuterType;
        this.deviceInnerType = deviceInnerType;
        this.deviceInnerType_category = deviceInnerType_category;
        this.manufacturer = manufacturer;
        this.deviceModel = deviceModel;
        this.sensorTypes.addAll(Arrays.asList(sensorTypes));
    }

    public String getDeviceOuterType() { return deviceOuterType; }
    public String getDeviceInnerType() { return deviceInnerType; }
    public String getDeviceInnerType_category() { return deviceInnerType_category; }
    public String getManufacturer() { return manufacturer; }
    public String getDeviceModel() { return deviceModel; }

    public List<PhenGenDeviceSensorType> getSensorTypes() { return sensorTypes;}
}
