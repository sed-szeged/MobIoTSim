package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC;

import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Parameter;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenDeviceSensorType;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenDeviceType;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Simulation;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Theme;



public class HC_Theme extends Theme {
    public static final String themeName = "HealthCare";
    public static final String deviceMainType = "HCDevice";

    //Sensor types
    public static final PhenGenDeviceSensorType blood_glucose =
            new PhenGenDeviceSensorType("blood_glucose", Parameter.ParameterType.DECIMAL,
                    HC_Subject_Person.PARAMETER_BLOOD_GLUCOSE_MMOL_L);
    public static final PhenGenDeviceSensorType sensor_glucose =
            new PhenGenDeviceSensorType("sensor_glucose", Parameter.ParameterType.DECIMAL,
                    HC_Subject_Person.PARAMETER_SENSOR_GLUCOS_MMOL_L);
    public static final PhenGenDeviceSensorType carbohydrate =
            new PhenGenDeviceSensorType("carbohydrate", Parameter.ParameterType.DECIMAL,
                    "");
    public static final PhenGenDeviceSensorType insulin =
            new PhenGenDeviceSensorType("insulin", Parameter.ParameterType.DECIMAL,
                    "");
    public static final PhenGenDeviceSensorType pulse =
            new PhenGenDeviceSensorType("current_pulse", Parameter.ParameterType.INTEGER,
                    HC_Subject_Person.PARAMETER_PULSE);
    public static final PhenGenDeviceSensorType bloodPressure_systolic =
            new PhenGenDeviceSensorType("blood_pressure_systolic", Parameter.ParameterType.INTEGER,
                    HC_Subject_Person.PARAMETER_BLOOD_PRESSURE_SYS);
    public static final PhenGenDeviceSensorType bloodPressure_diastolic =
            new PhenGenDeviceSensorType("blood_pressure_diastolic", Parameter.ParameterType.INTEGER,
                    HC_Subject_Person.PARAMETER_BLOOD_PRESSURE_DIA);
    public static final PhenGenDeviceSensorType blood_oxygen =
            new PhenGenDeviceSensorType("blood_oxygen", Parameter.ParameterType.INTEGER,
                    HC_Subject_Person.PARAMETER_BLOOD_OXYGEN);

    //DeviceTypes
    public static final PhenGenDeviceType bloodGlucoseMeter_SuperMedicineInc =
            new PhenGenDeviceType(HC_Theme.deviceMainType, "BloodyGlucoseMary",
                    "blood glucose meter","Super Medicine",
                    "RCVD5000", new PhenGenDeviceSensorType[]{blood_glucose});
    public static final PhenGenDeviceType average_smartWatch =
            new PhenGenDeviceType(HC_Theme.deviceMainType, "Average smartwatch",
                    "smart watch","Super Medicine", "SMW1000",
                    new PhenGenDeviceSensorType[]{pulse, bloodPressure_diastolic, bloodPressure_systolic});
    public static final PhenGenDeviceType unwise_smartWatch =
            new PhenGenDeviceType(HC_Theme.deviceMainType, "Bad smartwatch",
                    "smart watch","Cheap health", "XR2/4",
                    new PhenGenDeviceSensorType[]{pulse, bloodPressure_diastolic, bloodPressure_systolic});
    public static final PhenGenDeviceType super_smartWatch =
            new PhenGenDeviceType(HC_Theme.deviceMainType, "Super smartwatch",
                    "smart watch","Super Medicine", "SMW3000",
                    new PhenGenDeviceSensorType[]{pulse, bloodPressure_diastolic, bloodPressure_systolic});
    /*public static final PhenGenDeviceType _manualData =
            new PhenGenDeviceType(HC_Theme.themeName, "(manual)",
                    "", "", new PhenGenDeviceSensorType[]{
                blood_glucose, sensor_glucose, carbohydrate, insulin, current_pulse, bloodPressure_systolic,
                    bloodPressure_diastolic, blood_oxygen
            });*/

    private static HC_Theme theme = new HC_Theme();

    private HC_Theme() {
        super(themeName, deviceMainType);
        themeDescription = "Simulates phsyological phenomena of different populations of people in lifestyle or occupation.\n" +
                "They can be healthy or suffering from one or more diseases.\n\n"+
                "Different devices (smart watch, glucose sensor) are available that provide measured data about their actual health properties.";
        //Adding PhenGenDeviceTypes
        phenGenDeviceTypes.add(average_smartWatch);
        phenGenDeviceTypes.add(unwise_smartWatch);
        phenGenDeviceTypes.add(super_smartWatch);
        phenGenDeviceTypes.add(bloodGlucoseMeter_SuperMedicineInc);
    }
    public static HC_Theme getTheme() {
        return theme;
    }

    @Override
    public Simulation makeNewSimulation() {
        return new HC_Simulation(this);
    }

    @Override
    public Simulation getBlankSimulation() {
        return new HC_Simulation(this);
    }

}
