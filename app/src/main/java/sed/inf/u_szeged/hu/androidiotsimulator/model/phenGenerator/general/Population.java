package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public abstract class Population {

    static final String UNTITLED_POPULATION = "untitled";
    static final long POPULATION_DEFAULT_SIZE = 1;

    static final String PARAM_POPULATION_NAME = "POPULATION NAME";
    static final String PARAM_POPULATION_SIZE = "POPULATION SIZE";

    protected Parameter.ParamString populationName;
    protected Parameter.ParamLong requiredNumberOfSubjects;
    protected List<Subject> subjects = new ArrayList<>();
    protected Map<String, Parameter> populationSettings = new LinkedHashMap<>();  //publicly available settings

    protected Population() {
        populationName = new Parameter.ParamString(UNTITLED_POPULATION);
        requiredNumberOfSubjects = new Parameter.ParamLong(POPULATION_DEFAULT_SIZE);

        populationSettings.put(PARAM_POPULATION_NAME, populationName);
        populationSettings.put(PARAM_POPULATION_SIZE, requiredNumberOfSubjects);
    }

    protected abstract List<Subject> generateSubjects(Simulation simulation);
    protected abstract Subject createSubject(Simulation simulation);

    protected void removeSubjects(List<Subject> subjects) {
        subjects.removeAll(subjects);
    }

    protected void removeAllSubjects() {
        removeSubjects(subjects);
    }

    public Parameter getParameter(String parameterKey) {
        return populationSettings.get(parameterKey);
    }

    public Map<String, Parameter> getPopulationSettings() {
        return Collections.unmodifiableMap(populationSettings);
    }

    public String getPopulationName() {
        return populationName.toString();
    }

    /*public void setPopulationName(String populationName) {
        this.populationName.setString(populationName);
    }*/

    public long getRequiredNumberOfSubjects() {
        return requiredNumberOfSubjects.getValue();
    }

    /*public void setRequiredNumberOfSubjects(long number) {
        this.requiredNumberOfSubjects.setValue(number);
    }*/

    public long getActualNumberOfSubjects() { return subjects.size(); }

    public List<Subject> getSubjects() {
        return subjects;
    }

    @Override
    public String toString() {
        return getPopulationName();
    }
}
