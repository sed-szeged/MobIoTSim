package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.pgdevice;

import java.util.Map;

import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Measurement;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenDevice;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenDeviceSensorType;

public class PGDeviceSensorInfo {
    public String sensorName;
    public String sensorParameterType;
    public String sensorValue;

    public PGDeviceSensorInfo(PhenGenDevice pgdevice, PhenGenDeviceSensorType sensorType) {
        this.sensorName = sensorType.getName();
        this.sensorParameterType = sensorType.getParameterType().name();

        for (Map.Entry<String, Measurement> measure: pgdevice.getAttachedMeasurements().entrySet()) {
            if (measure.getKey().equals(sensorType.getName())) {
                this.sensorValue = measure.getValue().getData();
                break;
            }
        }//for
    }//constructor
}
