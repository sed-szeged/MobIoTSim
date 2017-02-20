package sed.inf.u_szeged.hu.androidiotsimulator.model.device;

import java.util.StringTokenizer;

/**
 * Created by tommy on 11/13/2016. Project name: MobIoTSim-mirrored
 *  
 */

public class SensorData {

    private String name;
    private String maxValue;
    private String minValue;

    public SensorData(String name, String minValue, String maxValue) {
        this.name = name;
        this.maxValue = maxValue;
        this.minValue = minValue;
    }

    public static SensorData fromString(String str) {
        StringTokenizer innerToken = new StringTokenizer(str, "+");

        String name = innerToken.nextToken();
        String min = innerToken.nextToken();
        String max = innerToken.nextToken();

        return new SensorData(name, min, max);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("+");
        sb.append(minValue);
        sb.append("+");
        sb.append(maxValue);
        return sb.toString();
    }

}
