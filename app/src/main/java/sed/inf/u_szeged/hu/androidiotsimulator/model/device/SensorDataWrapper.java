package sed.inf.u_szeged.hu.androidiotsimulator.model.device;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by tommy on 11/13/2016. Project name: MobIoTSim-mirrored
 *  
 */

public class SensorDataWrapper {


    List<SensorData> sensorDataList;

    public SensorDataWrapper() {
        sensorDataList = new ArrayList<>();
    }

    public SensorDataWrapper(List<SensorData> sensorDataList) {
        this.sensorDataList = sensorDataList;
    }

    public static SensorDataWrapper sensorDataFromSerial(String s) {
        SensorDataWrapper sensorDataWrapper = new SensorDataWrapper();
        StringTokenizer st = new StringTokenizer(s, "*");
        while (st.hasMoreTokens()) {

            String nextToken = st.nextToken();
            StringTokenizer innerToken = new StringTokenizer(nextToken, "+");

            String name = innerToken.nextToken();
            String min = innerToken.nextToken();
            String max = innerToken.nextToken();

            SensorData sensorData = new SensorData(name, min, max);
            sensorDataWrapper.addSensor(sensorData);
        }
        return sensorDataWrapper;
    }

    public void addSensor(SensorData sd) {
        sensorDataList.add(sd);
    }

    public List<SensorData> getList() {
        return sensorDataList;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (SensorData sd : sensorDataList) {
            sb.append(sd);
            if (sd != sensorDataList.get(sensorDataList.size() - 1)) {
                sb.append("*");
            }
        }

        return sb.toString();
    }


}
