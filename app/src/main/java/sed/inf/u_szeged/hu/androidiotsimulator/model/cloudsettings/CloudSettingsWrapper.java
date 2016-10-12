package sed.inf.u_szeged.hu.androidiotsimulator.model.cloudsettings;

import java.util.StringTokenizer;

/**
 * Created by Tomi on 2016. 05. 16..
 */
public class CloudSettingsWrapper {

    public enum CSType{BLUEMIX_DEMO, BLUEMIX, AZURE};

    CSType type;

    String name;

    String organizationID;

    //TODO
    String applicationID;
    String authKey;
    String authToken;

    boolean connectionType;

    int port;

    public CloudSettingsWrapper(){}

    public CloudSettingsWrapper(CSType type){
        this.type = type;
    }

    public CloudSettingsWrapper(CSType type, String name, String organizationID, boolean connectionType, int port, String applicationID, String authKey, String authToken){
        this.type = type;
        this.name = name;
        this.organizationID = organizationID;
        this.connectionType = connectionType;
        this.port = port;

        this.applicationID = applicationID;
        this.authKey = authKey;
        this.authToken = authToken;
    }

    public CSType getType(){
        return type;
    }

    public String getName(){
        return name;
    }

    public String getSerial(){
        StringBuilder sb = new StringBuilder();

        sb.append(type);
        sb.append("|");
        sb.append(name);
        sb.append("|");
        sb.append(organizationID);
        sb.append("|");
        sb.append(connectionType);
        sb.append("|");
        sb.append(port);
        sb.append("|");

        sb.append(applicationID);
        sb.append("|");
        sb.append(authKey);
        sb.append("|");
        sb.append(authToken);
        sb.append("|");
        return sb.toString();
    }

    public static CloudSettingsWrapper fromSerial(String serial){
        StringTokenizer st = new StringTokenizer(serial, "|");
        CSType type = CSType.valueOf(st.nextToken());
        String name = st.nextToken();
        String organizationID = st.nextToken();
        Boolean connectionType = Boolean.parseBoolean(st.nextToken());
        int port = Integer.parseInt(st.nextToken());

        String applicationID = st.nextToken();
        String authKey = st.nextToken();
        String authToken = st.nextToken();

        CloudSettingsWrapper cloud = new CloudSettingsWrapper(type, name, organizationID, connectionType, port, applicationID, authKey, authToken);
        return cloud;
    }

    public String getOrganizationID() {
        return organizationID;
    }

    public boolean isConnectionType() {
        return connectionType;
    }

    public int getPort() {
        return port;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public String getAuthKey() {
        return authKey;
    }

    public String getAuthToken() {
        return authToken;
    }
}
