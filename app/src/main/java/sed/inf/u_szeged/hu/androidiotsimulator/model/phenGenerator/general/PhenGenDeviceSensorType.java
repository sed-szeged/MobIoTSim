package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general;

public class PhenGenDeviceSensorType {
    private String name;
    private String measuredParameterName;
    private Parameter.ParameterType parameterType;
    /**
     * Unit of measurement.
     * (It is possible to be different devices with the same SensorType name (used as key in maps),
     * but having different units of measurement.)
     */
    private String unit;

    public PhenGenDeviceSensorType(String name, Parameter.ParameterType parameterType,
                                   String measuredParameterName) {
        this.name = name;
        this.parameterType = parameterType;
        this.measuredParameterName = measuredParameterName;
    }
    public PhenGenDeviceSensorType(String name, Parameter.ParameterType parameterType,
                                   String measuredParameterName,
                                   String unitOfMeasurement) {
        this(name, parameterType, measuredParameterName);
        unit = unitOfMeasurement;
    }

    public String getName() {
        return name;
    }

    public Parameter.ParameterType getParameterType() {
        return parameterType;
    }

    public String getMeasuredParameterName() { return measuredParameterName; }

    public String getUnit() { return unit; }
}
