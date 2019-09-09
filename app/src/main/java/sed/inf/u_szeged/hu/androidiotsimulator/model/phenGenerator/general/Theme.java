package sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general;

import java.util.ArrayList;
import java.util.List;

public abstract class Theme {
    protected String name;
    protected String themeDescription = "";
    protected String deviceMainType;
    protected List<PhenGenDeviceType> phenGenDeviceTypes = new ArrayList<>();

    public Theme(String name, String deviceMainType) {
        this.name = name;
        this.deviceMainType = deviceMainType;
    }

    public String getName() {
        return name;
    }

    public String getDeviceMainType() {
        return deviceMainType;
    }

    public String getThemeDescription() {return themeDescription;}

    public List<PhenGenDeviceType> getPhenGenDeviceTypes() {
        return phenGenDeviceTypes;
    }

    public abstract Simulation makeNewSimulation();

    public abstract Simulation getBlankSimulation();

    @Override
    public String toString() {
        return name;
    }
}
