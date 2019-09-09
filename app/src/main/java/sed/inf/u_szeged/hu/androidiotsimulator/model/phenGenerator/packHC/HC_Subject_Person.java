package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC;

import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Parameter;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Phenomenon;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Simulation;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Subject;

import static sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC.HC_Population_Persons.PARAMETER_DISEASE_HASDIABETES_TYPE_1;
import static sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC.HC_Population_Persons.PARAMETER_DISEASE_HAS_HYPERTONIA;
import static sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC.HC_Population_Persons.PARAMETER_CARES_ABOUT_OWN_DIABETES;
import static sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC.HC_Population_Persons.PARAMETER_LIFESTYLE;
import static sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC.HC_Population_Persons.PARAMETER_LIFESTYLE_ATHLETE;
import static sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC.HC_Population_Persons.PARAMETER_LIFESTYLE_GOODWORKER;
import static sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC.HC_Population_Persons.PARAMETER_SCHEDULE;
import static sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC.HC_Population_Persons.PARAMETER_SCHEDULE_NOROUTINE;
import static sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC.HC_Population_Persons.GROUP_LIFESTYLE;
import static sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC.HC_Population_Persons.GROUP_SCHEDULE;


public class HC_Subject_Person extends Subject {

    static long personalIdCounter = 0;

    public static final String PARAMETER_PERSONALID = "PERSONAL ID";
    public static final String PARAMETER_FORENAME = "FORENAME";
    public static final String PARAMETER_SURNAME = "SURNAME";
    public static final String PARAMETER_GENDER = "SEX";
    public static final String PARAMETER_GENDER_MALE = "MALE";
    public static final String PARAMETER_GENDER_FEMALE = "FEMALE";
    public static final Parameter.ParamEnum.ParamEnumGroup GROUP_GENDER = new Parameter.ParamEnum.ParamEnumGroup(
            new String[]{ PARAMETER_GENDER_MALE, PARAMETER_GENDER_FEMALE });
    public static final String PARAMETER_PULSE = "PULSE";
    public static final String PARAMETER_BLOOD_PRESSURE_SYS = "BLOOD PRESSURE - SYSTOLIC";
    public static final String PARAMETER_BLOOD_PRESSURE_DIA = "BLOOD PRESSURE - DIASTOLIC";
    public static final String PARAMETER_BLOOD_GLUCOSE_MMOL_L = "BLOOD GLUCOSE MMOL/L";
    //public static final String PARAMETER_BLOOD_GLUCOSE_MG_DL = "BLOOD GLUCOSE MG/DL";
    public static final String PARAMETER_SENSOR_GLUCOS_MMOL_L = "SENSOR GLUCOSE MMOL/L";
    public static final String PARAMETER_BLOOD_OXYGEN = "BLOOD OXYGEN";

    static final String PARAMETER_PERSONALID_PREFIX = "ID";
    static final int PARAMETER_PERSONALID_SUFFIX_LENGTH = 8;
    static final String DEFAULT_NAME = "person";


    protected Parameter.ParamString forename = new Parameter.ParamString("Annie");
    protected Parameter.ParamString surname = new Parameter.ParamString("Somebody");
    protected Parameter.ParamString personalID = new Parameter.ParamString("");
    protected Parameter.ParamEnum gender = new Parameter.ParamEnum(GROUP_GENDER, PARAMETER_GENDER_FEMALE);
    protected Parameter.ParamBoolean hasDiabetesType1 = new Parameter.ParamBoolean(false);
    protected Parameter.ParamBoolean caresAboutOwnDiabetes = new Parameter.ParamBoolean(false);
    protected Parameter.ParamBoolean hasHypertonia = new Parameter.ParamBoolean(false);
    protected Parameter.ParamEnum lifestyle =
            new Parameter.ParamEnum(GROUP_LIFESTYLE, PARAMETER_LIFESTYLE_GOODWORKER);
    protected Parameter.ParamEnum schedule =
            new Parameter.ParamEnum(GROUP_SCHEDULE, PARAMETER_SCHEDULE_NOROUTINE);

    public static final String PARAMETER_ACTIVITY = "ACTIVITY";
    public static final String PARAMETER_ACTIVITY_EXERCISE = "EXERCISE";
    public static final String PARAMETER_ACTIVITY_PASSIVE = "PASSIVE";
    public static final Parameter.ParamEnum.ParamEnumGroup ACTIVITY_GROUP = new Parameter.ParamEnum.ParamEnumGroup(
            new String[]{PARAMETER_ACTIVITY_EXERCISE, PARAMETER_ACTIVITY_PASSIVE}
    );
    protected Parameter.ParamDateTime exercise_last_start;
    protected Parameter.ParamDateTime exercise_last_end;
    protected Parameter.ParamDateTime exercise_next_start;
    protected Parameter.ParamDateTime exercise_next_end;
    protected Parameter.ParamBoolean  exhausted;
    protected Parameter.ParamEnum currentActivity =
            new Parameter.ParamEnum(ACTIVITY_GROUP, PARAMETER_ACTIVITY_PASSIVE);
    //protected Parameter.ParamInteger base_pulse = new Parameter.ParamInteger(0);
    protected Parameter.ParamInteger base_pulse = new Parameter.ParamInteger(0);
    protected Parameter.ParamInteger base_bloodPressureSys = new Parameter.ParamInteger(0);
    protected Parameter.ParamInteger base_bloodPressureDia = new Parameter.ParamInteger(0);
    protected Phenomenon.PhenomTargetInt current_pulse = new Phenomenon.PhenomTargetInt(0);
    protected Phenomenon.PhenomTargetInt current_bloodPressureSys = new Phenomenon.PhenomTargetInt(0);
    protected Phenomenon.PhenomTargetInt current_bloodPressureDia = new Phenomenon.PhenomTargetInt(0);


    public HC_Subject_Person(HC_Simulation simulation) {
        super(simulation);
        subjectName.setString(DEFAULT_NAME);
        //HC_Subject_Person
        personalID.setString(generateNextPersonalID());
        subjectQuickInfo.setString(personalID.toString());  //ID for quickInfo too
        //Settings
        individualSettings.put(PARAMETER_PERSONALID, personalID);
        individualSettings.put(PARAMETER_FORENAME, forename);
        individualSettings.put(PARAMETER_SURNAME, surname);
        individualSettings.put(PARAMETER_GENDER, gender);
        individualSettings.put(PARAMETER_DISEASE_HASDIABETES_TYPE_1, hasDiabetesType1);
        individualSettings.put(PARAMETER_CARES_ABOUT_OWN_DIABETES, caresAboutOwnDiabetes);
        individualSettings.put(PARAMETER_DISEASE_HAS_HYPERTONIA, hasHypertonia);
        individualSettings.put(PARAMETER_LIFESTYLE, lifestyle);
        individualSettings.put(PARAMETER_SCHEDULE, schedule);
        //Information
        individualInformation.put(PARAMETER_PULSE, current_pulse);
        individualInformation.put(PARAMETER_BLOOD_PRESSURE_SYS, current_bloodPressureSys);
        individualInformation.put(PARAMETER_BLOOD_PRESSURE_DIA, current_bloodPressureDia);
        individualInformation.put(PARAMETER_GENDER, gender);
        individualInformation.put(PARAMETER_DISEASE_HASDIABETES_TYPE_1, hasDiabetesType1);
        individualInformation.put(PARAMETER_DISEASE_HAS_HYPERTONIA, hasHypertonia);
        individualInformation.put(PARAMETER_LIFESTYLE, lifestyle);
        individualInformation.put(PARAMETER_SCHEDULE, schedule);
        individualInformation.put(PARAMETER_ACTIVITY, currentActivity);
        //Measurable parameters
        measurableParameters.put(PARAMETER_PULSE, current_pulse);
        measurableParameters.put(PARAMETER_BLOOD_PRESSURE_SYS, current_bloodPressureSys);
        measurableParameters.put(PARAMETER_BLOOD_PRESSURE_DIA, current_bloodPressureDia);
        // Set base and initialized current values
        simulation.calc_base_pulse(this);
        simulation.init_pulse(this);
        simulation.calc_target_pulse(this);
        simulation.calc_base_bloodPressure(this);
        simulation.init_bloodPressure(this);
        simulation.calc_target_bloodPressure(this);
    }

    private static String generateNextPersonalID() {
        StringBuilder strId = new StringBuilder(PARAMETER_PERSONALID_PREFIX);
        String strValue = String.valueOf(++personalIdCounter);
        int len = PARAMETER_PERSONALID_SUFFIX_LENGTH - strValue.length();
        for (int i = 0; i < len; i++) {
            strId.append('0');
        }
        strId.append(strValue);
        return strId.toString();
    }

    Parameter.ParamString getSubjectNameParameter() { return subjectName; }
    Parameter.ParamString getSubjectQuickInfoParameter() { return subjectQuickInfo; }

    @Override
    public String getSubjectId() {
        return personalID.toString();
    }
}

