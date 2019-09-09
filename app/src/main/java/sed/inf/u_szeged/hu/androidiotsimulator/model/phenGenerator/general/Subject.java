package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Subject {

    static final String UNTITLED_SUBJECT = "subject";
    static final String DEFAULT_QUICKINFO = "";

    public static final String PARAM_SUBJECT_NAME = "SUBJECT NAME";
    public static final String PARAM_SUBJECT_QUICKINFO = "SUBJECT QUICKINFO";

    protected Simulation parentSimulation;
    protected Population parentPopulation;

    /**
     A name to easily identify the subject.
     */
    protected Parameter.ParamString subjectName;
    /**
    Used to show concise information about the subject, maybe an identifier other than its name.
     */
    protected Parameter.ParamString subjectQuickInfo;
    /**
     Devices that can currently perform meauserements on the subject.
     */
    protected List<PhenGenDevice> attachedDevices = new ArrayList<>();
    /**
    Modifiable parameters.
    Usually they are set at the beginning, and afterwards they are permanent.
     */
    protected Map<String, Parameter> individualSettings = new LinkedHashMap<>();
    /**
    Parameters used at a comprehensive information board.
    Usually they show information about the actually generated state of the subject.
    (There can be an overlap between settings and information parameters.)
     */
    protected Map<String, Parameter> individualInformation = new LinkedHashMap<>();
    /**
     * Such parameters can be used by devices to be measured and send data from it
     */
    protected Map<String, Parameter> measurableParameters = new LinkedHashMap<>();

    protected Subject(Simulation simulation) {
        subjectName = new Parameter.ParamString(UNTITLED_SUBJECT);
        subjectQuickInfo = new Parameter.ParamString(DEFAULT_QUICKINFO);
        individualSettings.put(PARAM_SUBJECT_NAME, subjectName);
        individualSettings.put(PARAM_SUBJECT_QUICKINFO, subjectQuickInfo);
    }

    public Parameter getParameter(String parameterKey) {
        return individualSettings.get(parameterKey);
    }

    public Map<String, Parameter> getIndividualSettings() {
        return Collections.unmodifiableMap(individualSettings);
    }

    public Map<String, Parameter> getIndividualInformation() {
        return Collections.unmodifiableMap(individualInformation);
    }

    public String getSubjectName() {
        return subjectName.toString();
    }
    public String getQuickInfo() { return subjectQuickInfo.toString(); }
    public long getNumberOfAttechedDevices() { return attachedDevices.size(); }
    public Simulation getParentSimulation() { return parentSimulation; }
    public Population getParentPopulation() { return parentPopulation; }

    //Should be overwritten if it will be used
    public String getSubjectId() { return "0";}

    @Override
    public String toString() {
        return subjectName.toString() + " (" + subjectQuickInfo.toString() + ")";
    }
}
