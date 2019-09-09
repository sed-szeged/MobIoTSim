package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC;

import java.util.Date;

import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Parameter;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenDevice;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenMain;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Population;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Simulation;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Subject;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Theme;

import static sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC.HC_Population_Persons.PARAMETER_LIFESTYLE_ATHLETE;

public class HC_Simulation extends Simulation {

    protected HC_Simulation(Theme theme) {
        super(theme);

        this.rest_handler = new REST_handler("/device/create", "/patient/create",
                PhenGenMain.getPhenGenMain().getContext()) {
            @Override
            public String getSubjectJSON(Subject subject) {
                HC_Subject_Person hc_subject = (HC_Subject_Person) subject;
                String json = "{ \"name\":\"" + hc_subject.getSubjectName() + "\","
                        + "\"personal_id\":\"" + hc_subject.personalID + "\", "
                        + "\"sex\":\"" + hc_subject.gender.toString() + "\" }";
                return json;

            }

            @Override
            public String getDeviceJSON(PhenGenDevice pgdevice) {
                String dev_name = pgdevice.getDeviceID();
                String dev_pw = pgdevice.getToken();
                String outer_type = pgdevice.getPgdeviceType().getDeviceOuterType();
                String inner_type = pgdevice.getPgdeviceType().getDeviceInnerType();
                String inner_type_cat = pgdevice.getPgdeviceType().getDeviceInnerType_category();
                String dev_serial = pgdevice.getProductionSerialNumber();
                String dev_model = pgdevice.getPgdeviceType().getDeviceModel();
                String dev_manufacturer = pgdevice.getPgdeviceType().getManufacturer();

                String json = "{ \"name\":\"" + dev_name + "\", "
                        + "\"type\":\"" + outer_type + "\", "
                        + "\"password\":\"" + dev_pw + "\", "
                        + "\"topics\":"
                            + "\"{ \\\"public/#\\\" : \\\"r\\\", "
                            + "\\\"/device/" + outer_type + "/" + dev_name + "/#\\\" : \\\"rw\\\"}\", "
                        + "\"type_detail\":\"" + inner_type + "\", "
                        + "\"type_category\":\"" + inner_type_cat + "\", "
                        + "\"device_serial\":\"" + dev_serial + "\", "
                        + "\"device_model\":\"" + dev_model + "\", "
                        + "\"manufacturer\":\"" + dev_manufacturer + "\""
                        + "}";
                return json;
            }
        };
    }

    @Override
    public void generatePhenomena() {
        super.generatePhenomena();
        //System.out.println(simulatedDateTime.getValue().toString());
        for (Subject subject : subjects) {
            HC_Subject_Person person = (HC_Subject_Person) subject;
            calc_current_pulse(person);
            calc_current_bloodPressure(person);
            calc_target_pulse(person);
            calc_target_bloodPressure(person);
            if (person.getSubjectName().contains("Ket")) {
                System.out.println("PULSE = " + person.current_pulse.getCurrentValue() + ", BP = " + person.current_bloodPressureSys.getCurrentValue() + "/" + person.current_bloodPressureDia.getCurrentValue());
                //System.out.println("----- TargetTime: Pulse = " + person.current_pulse.target_time.toString());
                //System.out.println("---- Time: START=" + person.current_pulse.start_time + " // TARGET=" + person.current_pulse.target_time);
                //System.out.println("---- Value: START=" + person.current_pulse.start_value + " // TARGET=" + person.current_pulse.target_value);
            }
        }
    }

    @Override
    public Population newPopulation() {
        return new HC_Population_Persons();
    }

    //PHENOMENON GENERATOR METHODS
    //In healthcare simulation, they could be put in HC_Subject_Person, but in other cases, their
    //  best place is here, because phenomena can depend on more than one subjects

    /*
    PULSE
     */

    protected void calc_base_pulse(HC_Subject_Person person) {
        int base_pulse;
        if (person.lifestyle.toString().equals(PARAMETER_LIFESTYLE_ATHLETE)) {
            base_pulse = Parameter.getRandom(50, 80);
        } else { //LAZY_DEVELOPER
            base_pulse = Parameter.getRandom(60, 120);
        }
        person.base_pulse.setValue(base_pulse);
    }

    protected void init_pulse(HC_Subject_Person person) {
        person.current_pulse.setValue(person.base_pulse.getValue());
    }

    protected void calc_target_pulse(HC_Subject_Person person) {

        //in this case there is no need to calc target, because the previously calculated one has not been reached yet
        if (person.current_pulse.target_time != null
                && person.current_pulse.target_time.getTime() > simulatedDateTime.getValue().getTime()) return;

        int target_pulse;
        int target_time_min;
        if (person.currentActivity.toString().equals(person.PARAMETER_ACTIVITY_EXERCISE)) {
            target_pulse = Parameter.getRandom(80, 180);
            target_time_min = Parameter.getRandom(0, 10);
        } else {    //"normal" activity (PASSIVE)
            target_pulse = person.base_pulse.getValue()
                    * Parameter.getRandom(95, 105) / 100;
            target_time_min = Parameter.getRandom(0, 60);
        }

        Date target_time = new Date(simulatedDateTime.getValue().getTime()
                + target_time_min * 60 * 1000);
        person.current_pulse.setTargetValue(target_pulse, target_time, simulatedDateTime.getValue());
    }//calc_pulse

    protected void calc_current_pulse(HC_Subject_Person person) {
        person.current_pulse.calc_current_value(simulatedDateTime.getValue());
    }

    /*
    BLOOD PRESSURE
     */

    protected void calc_base_bloodPressure(HC_Subject_Person person) {
        int base_bp_sys;
        int base_bp_dia;
        if (person.hasHypertonia.isTrue()) {
            base_bp_sys = Parameter.getRandom(140, 180);
            base_bp_dia = base_bp_sys *
                    Parameter.getRandom(60, 70) / 100; //60-70%
        } else { //no hypertonia
            base_bp_sys =  Parameter.getRandom(90, 120) ;
            base_bp_dia = base_bp_sys *
                    Parameter.getRandom(60, 70) / 100;
        }
        person.base_bloodPressureSys.setValue(base_bp_sys);
        person.base_bloodPressureDia.setValue(base_bp_dia);
    }

    protected void init_bloodPressure(HC_Subject_Person person) {
        person.current_bloodPressureSys.setValue(person.base_bloodPressureSys.getValue());
        person.current_bloodPressureDia.setValue(person.base_bloodPressureDia.getValue());
    }

    protected void calc_target_bloodPressure(HC_Subject_Person person) {

        //in this case there is no need to calc target, because the previously calculated one has not been reached yet
        //sys and dia target_times are always the same in this simulation
        if (person.current_bloodPressureSys.target_time != null
                && person.current_bloodPressureSys.target_time.getTime() > simulatedDateTime.getValue().getTime()) return;

        int target_bp_sys;
        int target_bp_dia;
        int target_time_min;
        if (person.currentActivity.toString().equals(person.PARAMETER_ACTIVITY_PASSIVE)) {
            target_bp_sys = person.base_bloodPressureSys.getValue() *
                    Parameter.getRandom(95, 105) / 100 ;   //      +/-5%
            target_bp_dia = person.base_bloodPressureDia.getValue() *
                    Parameter.getRandom(95, 105) / 100 ;   //      +/-5%
            target_time_min = Parameter.getRandom(0, 60);
        } else {    //doing exercises
            if (person.hasHypertonia.isTrue()) {
                target_bp_sys = Parameter.getRandom(200, 250) ;
                target_bp_dia = (person.base_bloodPressureDia.getValue() +
                        Parameter.getRandom(0, 20)) *           //can move up significantly
                        Parameter.getRandom(95, 105) / 100;   //      +/-5%
            } else { //no hypertonia
                target_bp_sys = person.base_bloodPressureSys.getValue() +
                        Parameter.getRandom(70, 100);
                target_bp_dia = person.base_bloodPressureDia.getValue() *
                        Parameter.getRandom(95, 105) / 100;   //      +/-5%);
            }
            target_time_min = Parameter.getRandom(0, 10);
        }
        Date target_time = new Date(simulatedDateTime.getValue().getTime()
                + target_time_min * 60 * 1000);
        person.current_bloodPressureSys.setTargetValue(target_bp_sys, target_time, simulatedDateTime.getValue());
        person.current_bloodPressureDia.setTargetValue(target_bp_dia, target_time, simulatedDateTime.getValue());

    }

    protected void calc_current_bloodPressure(HC_Subject_Person person) {
        person.current_bloodPressureSys.calc_current_value(simulatedDateTime.getValue());
        person.current_bloodPressureDia.calc_current_value(simulatedDateTime.getValue());
    }
}
