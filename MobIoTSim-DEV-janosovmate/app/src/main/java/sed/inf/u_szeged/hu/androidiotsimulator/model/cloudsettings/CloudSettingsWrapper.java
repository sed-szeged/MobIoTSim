package sed.inf.u_szeged.hu.androidiotsimulator.model.cloudsettings;

import android.os.Bundle;
import androidx.annotation.NonNull;

import java.util.StringTokenizer;

import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudSettingsActivity;

/**
 * Created by Tomi on 2016. 05. 16..
 */
public class CloudSettingsWrapper {

    private CSType type;

    private String name;
    private String organizationID;
    private String applicationID;
    private String authKey;
    private String authToken;
    private boolean connectionType;
    private int port;
    private String commandID;
    private String eventID;

    public CloudSettingsWrapper(CSType type) {
        this.type = type;
    }

    public CloudSettingsWrapper(CSType type, String name, String organizationID, boolean connectionType, int port, String applicationID, String authKey, String authToken, String commandID, String eventID) {
        this.type = type;
        this.name = name;
        this.organizationID = organizationID;
        this.connectionType = connectionType;
        this.port = port;
        this.applicationID = applicationID;
        this.authKey = authKey;
        this.authToken = authToken;
        this.commandID = commandID;
        this.eventID = eventID;
    }

    public static CloudSettingsWrapper fromSerial(String serial) {
        StringTokenizer st = new StringTokenizer(serial, "|");
        CSType type = CSType.valueOf(st.nextToken());
        String name = st.nextToken();
        String organizationID = st.nextToken();
        Boolean connectionType = Boolean.parseBoolean(st.nextToken());
        int port = Integer.parseInt(st.nextToken());
        String applicationID = st.nextToken();
        String authKey = st.nextToken();
        String authToken = st.nextToken();
        String commandId = st.nextToken();
        String eventId = st.nextToken();

        CloudSettingsWrapper cloud = new CloudSettingsWrapper(type, name, organizationID, connectionType, port, applicationID, authKey, authToken, commandId, eventId);
        return cloud;
    }

    @NonNull
    public static CloudSettingsWrapper getCloudSettingsWrapper(Bundle bundle) {
        CloudSettingsWrapper cloudSettingsWrapper;
        if (bundle.getString(CloudSettingsActivity.KEY_TYPE).equals("BLUEMIX")) {
            cloudSettingsWrapper = new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX,
                    bundle.getString(CloudSettingsActivity.KEY_NAME),
                    bundle.getString(CloudSettingsActivity.KEY_ORGANIZATION_ID),
                    true,
                    1883,
                    bundle.getString(CloudSettingsActivity.KEY_APPLICATION_ID),
                    bundle.getString(CloudSettingsActivity.KEY_AUTH_KEY),
                    bundle.getString(CloudSettingsActivity.KEY_AUTH_TOKEN),
                    bundle.getString(CloudSettingsActivity.KEY_COMMAND_ID),
                    bundle.getString(CloudSettingsActivity.KEY_EVENT_ID)
            );
        }else if (bundle.getString(CloudSettingsActivity.KEY_TYPE).equals("AWS")) {
            cloudSettingsWrapper = new CloudSettingsWrapper(CloudSettingsWrapper.CSType.AWS,
                    bundle.getString(CloudSettingsActivity.KEY_NAME),
                    bundle.getString(CloudSettingsActivity.KEY_ORGANIZATION_ID),
                    true,
                    1883,
                    bundle.getString(CloudSettingsActivity.KEY_APPLICATION_ID),
                    bundle.getString(CloudSettingsActivity.KEY_AUTH_KEY),
                    bundle.getString(CloudSettingsActivity.KEY_AUTH_TOKEN),
                    bundle.getString(CloudSettingsActivity.KEY_COMMAND_ID),
                    bundle.getString(CloudSettingsActivity.KEY_EVENT_ID)
            );
        } else {
            cloudSettingsWrapper = new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX_DEMO);
        }
        return cloudSettingsWrapper;
    }

    public CSType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getSerial() {
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
        sb.append(commandID);
        sb.append("|");
        sb.append(eventID);
        return sb.toString();
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

    public String getCommandID() {
        return commandID;
    }

    public String getEventID() {
        return eventID;
    }

    public enum CSType {BLUEMIX_DEMO, BLUEMIX, AZURE, AWS}
}

