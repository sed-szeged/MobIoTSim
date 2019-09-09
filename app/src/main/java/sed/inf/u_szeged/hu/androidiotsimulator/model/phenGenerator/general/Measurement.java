package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general;

public class Measurement {
    protected String dataToSend;    //in JSON format
    //protected String measurementTime;   //date and time of measurement
    public final Subject subject;
    public final PhenGenDevice device;
    public final PhenGenDeviceSensorType sensorType;

    protected Measurement(Subject subject, PhenGenDevice device, PhenGenDeviceSensorType sensorType) {
        this.subject = subject;
        this.device = device;
        this.sensorType = sensorType;
    }

    public final synchronized String getData() {
        //return new MeasurementData(dataToSend, measurementTime);
        return dataToSend;
    }

    public final synchronized void refreshData() {
        String paramName = sensorType.getMeasuredParameterName();
        if (paramName != null) {
            Parameter parameter = subject.measurableParameters.get(paramName);
            if (parameter != null) dataToSend = preProcess(parameter);
        }
    }

    public final synchronized void putDataDirectly(Parameter parameter) {
        dataToSend = parameter.toString();
        //this.measurementTime = measurementTime;
    }

    protected String preProcess(Parameter parameter) {
        Parameter preProcessedParameter = device.getMeasuredParameter(parameter);
        return Parameter_JSON.getJsonValueFromParameter(preProcessedParameter);
    }

    /*
    public static class MeasurementData {
        public String dataValueJson;
        public String measurementTime;
        public String[] additionalInfo;
        public MeasurementData(String dataValueJson, String measurementTime, String[] additionalInfo) {
            this.dataValueJson = dataValueJson;
            this.measurementTime = measurementTime;
            this.additionalInfo = additionalInfo;
        }
    }*/
}
