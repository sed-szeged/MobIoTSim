package sed.inf.u_szeged.hu.androidiotsimulator.model.cloudsettings;

import android.os.Bundle;
import android.support.annotation.NonNull;

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

    private String brokerUrl;       //usually generated, but it depends on the cloudType
    private String brokerUrlPrefix;
    private String brokerUrlHost;
    //private String brokerUrlPort;

    public CloudSettingsWrapper(CSType type) {
        this.type = type;
    }

    public CloudSettingsWrapper(CSType type, String name, String organizationID, boolean connectionType,
                                int port, String applicationID, String authKey, String authToken,
                                String commandID, String eventID,
                                String brokerUrl, String brokerUrlPrefix, String brokerUrlHost) {
        boolean defaultSecureConnection = isDefaultSecure(type);
        this.type = type;
        this.name = name;
        this.organizationID = organizationID;
        this.connectionType = connectionType;
        this.port = ( port==0
                ? createPortNumber(this.type.toString(), defaultSecureConnection)
                : port );
        this.applicationID = applicationID;
        this.authKey = authKey;
        this.authToken = authToken;
        this.commandID = commandID;
        this.eventID = eventID;
        this.brokerUrlPrefix = ( brokerUrlPrefix.isEmpty()
                ? createPrefix(this.type.toString(), defaultSecureConnection)
                : brokerUrlPrefix );
        this.brokerUrlHost = ( brokerUrlHost.isEmpty()
                ? createHost(this.type.toString())
                : brokerUrlHost );
        this.brokerUrl = brokerUrl.isEmpty()
                ? createURL(this.brokerUrlPrefix, this.type.toString(), this.organizationID,
                    this.brokerUrlHost, this.port)
                : brokerUrl;
    }

    //TODO: make it handle empty strings (because StringTokenizer jumps multiple delimiters)
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
        String brokerUrl = st.nextToken();
        String brokerUrlPrefix = st.nextToken();
        String brokerUrlHost = st.nextToken();

        CloudSettingsWrapper cloud = new CloudSettingsWrapper(type, name, organizationID, connectionType, port,
                applicationID, authKey, authToken, commandId, eventId, brokerUrl, brokerUrlPrefix, brokerUrlHost);

        return cloud;
    }

    @NonNull
    public static CloudSettingsWrapper getCloudSettingsWrapper(Bundle bundle) {
        CloudSettingsWrapper cloudSettingsWrapper;
        if (bundle.getString(CloudSettingsActivity.KEY_TYPE).equals(CSType.BLUEMIX_DEMO.toString())) {
            cloudSettingsWrapper = new CloudSettingsWrapper(CSType.BLUEMIX_DEMO);
        } else {
            cloudSettingsWrapper = new CloudSettingsWrapper(
                    CSType.valueOf(bundle.getString(CloudSettingsActivity.KEY_TYPE)),
                    bundle.getString(CloudSettingsActivity.KEY_NAME),
                    bundle.getString(CloudSettingsActivity.KEY_ORGANIZATION_ID),
                    true,
                    bundle.getInt(CloudSettingsActivity.KEY_PORT),
                    bundle.getString(CloudSettingsActivity.KEY_APPLICATION_ID),
                    bundle.getString(CloudSettingsActivity.KEY_AUTH_KEY),
                    bundle.getString(CloudSettingsActivity.KEY_AUTH_TOKEN),
                    bundle.getString(CloudSettingsActivity.KEY_COMMAND_ID),
                    bundle.getString(CloudSettingsActivity.KEY_EVENT_ID),
                    bundle.getString(CloudSettingsActivity.KEY_BROKER_URL),
                    bundle.getString(CloudSettingsActivity.KEY_BROKER_URL_PREFIX),
                    bundle.getString(CloudSettingsActivity.KEY_BROKER_URL_HOST)
            );
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
        sb.append("|");
        sb.append(brokerUrl);
        sb.append("|");
        sb.append(brokerUrlPrefix);
        sb.append("|");
        sb.append(brokerUrlHost);
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

    public String getBrokerUrl() { return brokerUrl;}

    public String getBrokerUrlPrefix() { return brokerUrlPrefix; }

    public String getBrokerUrlHost() { return brokerUrlHost; }


    public static String createURL(String brokerUrlPrefix, String cloudType, String organizationID,
                                   String brokerUrlHost, int port) {
        String url = brokerUrlPrefix + "://"
                + ( cloudType.equals(CSType.BLUEMIX.toString()) ? organizationID + "." : "")
                + brokerUrlHost + ":" + port;
        return url;
    }

    public static boolean isDefaultSecure(CSType cloudType) {
        switch (cloudType) {
            case BLUEMIX: return true;
            case EDGE_GATEWAY: return false;
            default: return false;
        }
    }

    public static String createPrefix(String cloudType, boolean isSecure) {
        return isSecure ? "ssl" : "tcp";
    }

    public static String createHost(String cloudType) {
        switch (cloudType) {
            case "BLUEMIX": return "messaging.internetofthings.ibmcloud.com";
            default: return "";
        }
    }

    public static int createPortNumber(String cloudType, boolean isSecure) {
        return isSecure ? 443 : 1883;
    }


    public enum CSType {
        BLUEMIX_DEMO, BLUEMIX, AZURE, EDGE_GATEWAY, CUSTOM;
        public boolean usesRESTTools() {
            //TODO: implement adding/removing devices in a general way, then this can be deleted
            switch(this) {
                case BLUEMIX:
                case BLUEMIX_DEMO:
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public String toString() {
            switch(this){
                case BLUEMIX_DEMO:
                    return "BLUEMIX_DEMO";
                case BLUEMIX:
                    return "BLUEMIX";
                case AZURE:
                    return "AZURE";
                case EDGE_GATEWAY:
                    return "EDGE_GATEWAY";
                case CUSTOM:
                default:
                    return "CUSTOM";
            }
        }

    }
}
