package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC.HC_Subject_Person;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.packHC.HC_Theme;

public class PhenGenMain {
    Context context;
    private static boolean isLoaded = false;    //has the existing (default or file) content been loaded
    private List<Theme> availableThemes;
    private TreeMap<Long, Simulation> simulations = new TreeMap<>();
    private TreeMap<Long, Population> registeredPopulations = new TreeMap<>();
    private TreeMap<Long, Subject> registeredSubjects = new TreeMap<>();
    private TreeMap<Long, PhenGenDevice> registeredPGDevices = new TreeMap<>();

    public static final String KEY_SIMULATION = "SIMULATION_KEY";
    public static final String KEY_THEME = "THEME_KEY";
    public static final String KEY_POPULATION = "POPULATION_KEY";
    public static final String KEY_SUBJECT = "SUBJECT_KEY";
    public static final String KEY_PGDEVICE = "PGDEVICE_KEY";
    public static final String KEY_PGDEVICE_TYPE = "PGDEVICE_TYPE_KEY";

    public static final Long NEW_ELEMENT = -1L;
    public static final Long ALL_ELEMENT = -2L; //for example: show all subject in all populations

    public boolean isLoaded() {
        return isLoaded;
    }

    // TEST INIT
    public void loadDefaultSimulations() {
        isLoaded = true;
        Simulation simulation1 = HC_Theme.getTheme().getBlankSimulation();
            simulation1.name.setString("TesztSzimuláció1");
            simulations.put(1L, simulation1);

        Simulation simulation2 = HC_Theme.getTheme().getBlankSimulation();
            simulation2.name.setString("TesztSzimuláció 2");
            simulations.put(2L, simulation2);
            Population pop2_1 = simulation2.createPopulation();
                ((Parameter.ParamString) pop2_1.getParameter(Population.PARAM_POPULATION_NAME)).setString("Fremenek");
                ((Parameter.ParamLong) pop2_1.getParameter(Population.PARAM_POPULATION_SIZE)).setValue(12);
                ((Parameter.ParamEnum) pop2_1.getParameter("LIFESTYLE")).setValue("ATHLETE");
            Population pop2_2 = simulation2.createPopulation();
                ((Parameter.ParamString) pop2_2.getParameter(Population.PARAM_POPULATION_NAME)).setString("Númenoriak");
                ((Parameter.ParamLong) pop2_2.getParameter(Population.PARAM_POPULATION_SIZE)).setValue(5);
                HC_Subject_Person sub2_2_1 = (HC_Subject_Person) simulation2.createSubject(pop2_2);
                    ((Parameter.ParamString) sub2_2_1.getParameter(Subject.PARAM_SUBJECT_NAME)).setString("első");
                    ((Parameter.ParamString) sub2_2_1.getParameter(HC_Subject_Person.PARAMETER_FORENAME)).setString("Egy");
                    ((Parameter.ParamString) sub2_2_1.getParameter(HC_Subject_Person.PARAMETER_SURNAME)).setString("Num");
                    ((Parameter.ParamEnum) sub2_2_1.getParameter(HC_Subject_Person.PARAMETER_GENDER)).setValue(HC_Subject_Person.PARAMETER_GENDER_MALE);
                HC_Subject_Person sub2_2_2 = (HC_Subject_Person) simulation2.createSubject(pop2_2);
                    ((Parameter.ParamString) sub2_2_2.getParameter(Subject.PARAM_SUBJECT_NAME)).setString("Ket Num");
                    ((Parameter.ParamString) sub2_2_2.getParameter(HC_Subject_Person.PARAMETER_FORENAME)).setString("Ket");
                    ((Parameter.ParamString) sub2_2_2.getParameter(HC_Subject_Person.PARAMETER_SURNAME)).setString("Num");
                    ((Parameter.ParamEnum) sub2_2_2.getParameter(HC_Subject_Person.PARAMETER_GENDER)).setValue(HC_Subject_Person.PARAMETER_GENDER_FEMALE);
            Population pop2_3 = simulation2.createPopulation();
                ((Parameter.ParamString) pop2_3.getParameter(Population.PARAM_POPULATION_NAME)).setString("Subjects of Ymir");
                ((Parameter.ParamLong) pop2_3.getParameter(Population.PARAM_POPULATION_SIZE)).setValue(1);
                ((Parameter.ParamEnum) pop2_3.getParameter("LIFESTYLE")).setValue("LAZY_DEVELOPER");
                HC_Subject_Person sub2_3_1 = (HC_Subject_Person) simulation2.createSubject(pop2_3);
                    ((Parameter.ParamString) sub2_3_1.getParameter(Subject.PARAM_SUBJECT_NAME)).setString("Hodor?");
                    ((Parameter.ParamString) sub2_3_1.getParameter(HC_Subject_Person.PARAMETER_FORENAME)).setString("Hodor");
                    ((Parameter.ParamString) sub2_3_1.getParameter(HC_Subject_Person.PARAMETER_SURNAME)).setString("");
                    ((Parameter.ParamEnum) sub2_3_1.getParameter(HC_Subject_Person.PARAMETER_GENDER)).setValue(HC_Subject_Person.PARAMETER_GENDER_MALE);
            PhenGenDevice dev2_1 = simulation2.createDevice(HC_Theme.super_smartWatch);
            PhenGenDevice dev2_2 = simulation2.createDevice(HC_Theme.super_smartWatch);
            PhenGenDevice dev2_3 = simulation2.createDevice(HC_Theme.super_smartWatch);
            PhenGenDevice dev2_4 = simulation2.createDevice(HC_Theme.average_smartWatch);
            PhenGenDevice dev2_5 = simulation2.createDevice(HC_Theme.bloodGlucoseMeter_SuperMedicineInc);
            PhenGenDevice dev2_6 = simulation2.createDevice(HC_Theme.unwise_smartWatch);

        Simulation simulation3 = HC_Theme.getTheme().getBlankSimulation();
            simulation3.name.setString("Ne szimulálj!");
            simulations.put(3L, simulation3);
            Population pop3_1 = simulation3.createPopulation();
                ((Parameter.ParamString) pop3_1.getParameter(Population.PARAM_POPULATION_NAME)).setString("Hippik");
                ((Parameter.ParamLong) pop3_1.getParameter(Population.PARAM_POPULATION_SIZE)).setValue(3);
    }



    //GENERAL
    private static PhenGenMain phenGenMain = new PhenGenMain();
    private PhenGenMain() {
        availableThemes = new ArrayList<>();
        availableThemes.add(HC_Theme.getTheme());
    }
    public static PhenGenMain getPhenGenMain() {
        return phenGenMain;
    }
    public Context getContext() { return context; }
    public void setContext(Context context) {
        //DeviceGroup needs it
        this.context = context;
    }


    //THEMES
    public List<Theme> getAvailableThemes() { return availableThemes;}

    public Theme getThemeByName(String themeName) {
        for (Theme theme: availableThemes) {
            if (theme.name.equals(themeName)) return theme;
        }
        return null;
    }



    //SIMULATIONS
    public List<Simulation> getSimulationList(Theme filterTheme) {
        ArrayList<Simulation> list = new ArrayList<>();
        for (Map.Entry<Long, Simulation> entry : simulations.entrySet()) {
            if (filterTheme == null || entry.getValue().theme == filterTheme) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    public long getKeyBySimulation(Simulation simulation) {
        for (Map.Entry<Long, Simulation> entry : simulations.entrySet()) {
            if (entry.getValue() == simulation) {
                return entry.getKey();
            }
        }
        //key doesn't extist, so registrate simulation to get one
        return registrateSimulation(simulation);
    }

    public Simulation getSimulationByKey(long key) {
        return simulations.get(key);
    }

    public long registrateSimulation(Simulation simulation) {
        return registrateToTreeMap(simulations, simulation);
    }

    public void removeSimulation(Simulation simulation) {
        simulations.remove(getKeyBySimulation(simulation));
    }



    // POPULATIONS
    public List<Population> getPopulationList(long simulationKey) {
        Simulation selectedSimulation = simulations.get(simulationKey);
        if (selectedSimulation == null) return null;
        else return selectedSimulation.getPopulations();
    }

    public long getKeyByPopulation(Population population) {
        for (Map.Entry<Long, Population> entry : registeredPopulations.entrySet()) {
            if (entry.getValue() == population) {
                return entry.getKey();
            }
        }
        //key doesn't extist, so registrate population to get one
        return registratePopulation(population);
    }

    public Population getPopulationByKey(long key) {
        if (key == ALL_ELEMENT) return null;
        else return registeredPopulations.get(key);
    }

    public long registratePopulation(Population population) {
        return registrateToTreeMap(registeredPopulations, population);
    }

    public void removePopulation(Population population) {
        registeredPopulations.remove(getKeyByPopulation(population));
    }



    // SUBJECTS
    public List<Subject> getSubjectListInSimulation(long simulationKey) {
        Simulation selectedSimulation = simulations.get(simulationKey);
        if (selectedSimulation == null) return null;
        else return selectedSimulation.getSubjects();
    }

    public List<Subject> getSubjectListInPopulation(long populationKey) {
        Population selectedPopulation = registeredPopulations.get(populationKey);
        if (selectedPopulation == null) return null;
        else return selectedPopulation.getSubjects();
    }

    public long getKeyBySubject(Subject subject) {
        for (Map.Entry<Long, Subject> entry : registeredSubjects.entrySet()) {
            if (entry.getValue() == subject) {
                return entry.getKey();
            }
        }
        //key doesn't extist, so registrate subject to get one
        return registrateSubject(subject);
    }

    public Subject getSubjectByKey(long key) {
        return registeredSubjects.get(key);
    }

    public long registrateSubject(Subject subject) {
        return registrateToTreeMap(registeredSubjects, subject);
    }

    public void removeSubject(Subject subject) {
        registeredSubjects.remove(getKeyBySubject(subject));
    }



    // PGDevices (Phenomenon Generator Devices)
    public List<PhenGenDevice> getPGDeviceList(long simulationKey) {
        Simulation selectedSimulation = simulations.get(simulationKey);
        if (selectedSimulation == null) return null;
        else return selectedSimulation.getDevices();
    }

    public long getKeyByPGDevice(PhenGenDevice phenGenDevice) {
        for (Map.Entry<Long, PhenGenDevice> entry : registeredPGDevices.entrySet()) {
            if (entry.getValue() == phenGenDevice) {
                return entry.getKey();
            }
        }
        //key doesn't extist, so registrate population to get one
        return registratePGDevice(phenGenDevice);
    }

    public PhenGenDevice getPGDeviceByKey(long key) {
        if (key == ALL_ELEMENT) return null;
        else return registeredPGDevices.get(key);
    }

    public long registratePGDevice(PhenGenDevice phenGenDevice) {
        return registrateToTreeMap(registeredPGDevices, phenGenDevice);
    }

    public void removePGDevice(PhenGenDevice phenGenDevice) {
        registeredPGDevices.remove(getKeyByPGDevice(phenGenDevice));
    }



    //UTILITY
    private <V> long registrateToTreeMap(TreeMap<Long, V> treeMap, V value) {
        long newKey = treeMap.isEmpty() ? 1 : treeMap.lastKey() + 1;
        treeMap.put(newKey, value);
        return newKey;
    }

}
