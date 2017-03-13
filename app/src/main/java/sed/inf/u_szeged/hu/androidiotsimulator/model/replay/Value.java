package sed.inf.u_szeged.hu.androidiotsimulator.model.replay;

/**
 * Created by tommy on 1/29/2017. Project name: MobIoTSim-mirrored
 *  
 */

public class Value {

    private String name;
    private String value;

    public Value(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
