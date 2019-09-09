package sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.StringTokenizer;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.model.cloudsettings.CloudSettingsWrapper;

public class CloudActivity extends AppCompatActivity {

    public final static int ADD_CLOUD_SETTINGS_REQ_CODE = 876;
    public final static int EDIT_CLOUD_SETTINGS_REQ_CODE = 543;

    ArrayList<CloudSettingsWrapper> cloudSettingsWrappers;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);
        MobIoTApplication.setActivity(this);

        cloudSettingsWrappers = loadCloudSettingsWrappers();
        saveClouds();
        initCloudProvidersSpinner(null);
        initConnectionTypeSpinner();
        loadSettingsFromPreferences();
        initializeButtons();
    }


    @Override
    protected void onResume() {
        super.onResume();
        MobIoTApplication.setActivity(this);
    }

    private void initializeButtons() {
        ((FloatingActionButton) findViewById(R.id.new_cloud_fab)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(CloudActivity.this);
                builder.setTitle("Select a Cloud Type");
                final ArrayList<String> cloudTypeList = new ArrayList<>();
                for (CloudSettingsWrapper.CSType type : CloudSettingsWrapper.CSType.values()) {
                    cloudTypeList.add(type.toString());
                }
                final String[] cloudTypes = cloudTypeList.toArray(new String[0]);
                builder.setItems(cloudTypes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(CloudActivity.this, CloudSettingsActivity.class);
                        Bundle bundle = new Bundle();

                        switch (cloudTypes[which]) {
                            case "BLUEMIX_DEMO":
                                bundle.putString(CloudSettingsActivity.KEY_TYPE, CloudSettingsWrapper.CSType.BLUEMIX_DEMO.toString());
                                bundle.putString(CloudSettingsActivity.KEY_ORGANIZATION_ID, "quickstart");
                                break;
                            default:
                                String cstypeStr = cloudTypes[which];
                                boolean isSecure = cstypeStr.equals(CloudSettingsWrapper.CSType.BLUEMIX.toString())
                                        ? true : false;
                                int defaultPort = CloudSettingsWrapper.createPortNumber(cstypeStr, isSecure);
                                String defaultPrefix = CloudSettingsWrapper.createPrefix(cstypeStr, isSecure);
                                String defaultHost = CloudSettingsWrapper.createHost(cstypeStr);
                                String defaultURL = CloudSettingsWrapper.createURL(defaultPrefix, cstypeStr,
                                        "?", defaultHost, defaultPort);
                                bundle.putString(CloudSettingsActivity.KEY_TYPE, cstypeStr);
                                bundle.putInt(CloudSettingsActivity.KEY_PORT, defaultPort);
                                bundle.putString(CloudSettingsActivity.KEY_BROKER_URL_PREFIX, defaultPrefix);
                                bundle.putString(CloudSettingsActivity.KEY_BROKER_URL_HOST, defaultHost);
                                bundle.putString(CloudSettingsActivity.KEY_BROKER_URL, defaultURL);
                                break;
                        }

                        intent.putExtras(bundle);
                        startActivityForResult(intent, ADD_CLOUD_SETTINGS_REQ_CODE);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        ((Button) findViewById(R.id.edit_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CloudActivity.this, CloudSettingsActivity.class);
                Bundle bundle = new Bundle();

                int position = ((Spinner) findViewById(R.id.cloud_providers_spinner)).getSelectedItemPosition();

                bundle.putString(CloudSettingsActivity.KEY_TYPE,
                        cloudSettingsWrappers.get(position).getType().toString());

                if (cloudSettingsWrappers.get(position).getType() == CloudSettingsWrapper.CSType.BLUEMIX_DEMO) {
                    bundle.putString(CloudSettingsActivity.KEY_ORGANIZATION_ID,
                            "quickstart");

                    bundle.putString(CloudSettingsActivity.KEY_ORGANIZATION_ID,
                            "myBluemix");
                } else {

                    bundle.putString(CloudSettingsActivity.KEY_ORGANIZATION_ID,
                            cloudSettingsWrappers.get(position).getOrganizationID());

                    bundle.putInt(CloudSettingsActivity.KEY_PORT,
                            cloudSettingsWrappers.get(position).getPort());

                    bundle.putString(CloudSettingsActivity.KEY_APPLICATION_ID,
                            cloudSettingsWrappers.get(position).getApplicationID());

                    bundle.putString(CloudSettingsActivity.KEY_AUTH_TOKEN,
                            cloudSettingsWrappers.get(position).getAuthToken());

                    bundle.putString(CloudSettingsActivity.KEY_AUTH_KEY,
                            cloudSettingsWrappers.get(position).getAuthKey());

                    bundle.putString(CloudSettingsActivity.KEY_COMMAND_ID,
                            cloudSettingsWrappers.get(position).getCommandID());

                    bundle.putString(CloudSettingsActivity.KEY_EVENT_ID,
                            cloudSettingsWrappers.get(position).getEventID());

                    bundle.putString(CloudSettingsActivity.KEY_BROKER_URL,
                            cloudSettingsWrappers.get(position).getBrokerUrl());

                    bundle.putString(CloudSettingsActivity.KEY_BROKER_URL_PREFIX,
                            cloudSettingsWrappers.get(position).getBrokerUrlPrefix());

                    bundle.putString(CloudSettingsActivity.KEY_BROKER_URL_HOST,
                            cloudSettingsWrappers.get(position).getBrokerUrlHost());
                }

                bundle.putString(CloudSettingsActivity.KEY_NAME,
                        cloudSettingsWrappers.get(position).getName());

                bundle.putString(CloudSettingsActivity.KEY_EDIT_IT, String.valueOf(position));

                intent.putExtras(bundle);
                startActivityForResult(intent, EDIT_CLOUD_SETTINGS_REQ_CODE);
            }
        });

        ((Button) findViewById(R.id.delete_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner spinner = (Spinner) findViewById(R.id.cloud_providers_spinner);
                String currentlySelected = MobIoTApplication.loadData(CloudSettingsActivity.KEY_NAME);

                ArrayList<String> providers = createProvidersList();
                cloudSettingsWrappers.remove(providers.indexOf(currentlySelected));
                adapter.notifyDataSetChanged();
                saveClouds();
                initCloudProvidersSpinner(null);
                loadSettingsFromPreferences();

            }
        });
    }


    private ArrayList<CloudSettingsWrapper> loadCloudSettingsWrappers() {
        String clouds = MobIoTApplication.loadData(MobIoTApplication.KEY_CLOUDS);
        System.out.println("clouds: " + clouds);
        ArrayList<CloudSettingsWrapper> wrappers = new ArrayList<CloudSettingsWrapper>();

        // Checks does any clouds exist in the shared preferences, if not then creates new clouds
        if (clouds != null && !clouds.equals("")) {
            StringTokenizer st = new StringTokenizer(clouds, "<");
            while (st.hasMoreTokens()) {
                String cloudSerial = st.nextToken();
                wrappers.add(CloudSettingsWrapper.fromSerial(cloudSerial));
            }
        } else {
            CloudSettingsWrapper demoBluemix =
                    new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX_DEMO);
            wrappers.add(demoBluemix);
            CloudSettingsWrapper myBluemix =
                    new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX, "myBluemix",
                            "wg3go6",false, 1883, "mobiotsim",
                            "empty", "empty", "cmd", "status",
                            "", "", "");
            wrappers.add(myBluemix);
            CloudSettingsWrapper teszt02 =
                    new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX, "Teszt02",
                            "6dr2qf", false, 443,
                            "mobiotsimApp","a-6dr2qf-r5suhuzvwc", "M?52f?S0KBy(ghdFyo",
                            "cmd", "status",
                            "ssl://6dr2qf.messaging. Internetofthings.ibmcloud.com:443",
                            "","");
            wrappers.add(teszt02);
            CloudSettingsWrapper teszt03 =
                    new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX, "Teszt03",
                            "6dr2qf", false, 443, "mobiotsimApp",
                            "a-6dr2qf-o9sr2vyns5", "JAn*zY7&Vb-jJTILAT",
                            "cmd", "status",
                            "","","");
            wrappers.add(teszt03);
            CloudSettingsWrapper edgeGW =
                    new CloudSettingsWrapper(CloudSettingsWrapper.CSType.EDGE_GATEWAY, "EdgeGateway",
                            "x", false, 1883, "mobiotsimApp",
                            "x", "x", "x", "x",
                            "","","192.168.0.13");
            wrappers.add(edgeGW);

        }

        return wrappers;
    }


    private void loadSettingsFromPreferences() {
        String organizationID = MobIoTApplication.loadData(CloudSettingsActivity.KEY_ORGANIZATION_ID);
        ((EditText) findViewById(R.id.organization_id_et)).setText(organizationID);

        String applicationID = MobIoTApplication.loadData(CloudSettingsActivity.KEY_APPLICATION_ID);
        ((EditText) findViewById(R.id.app_id_et)).setText(applicationID);

        String authKey = MobIoTApplication.loadData(CloudSettingsActivity.KEY_AUTH_KEY);
        ((EditText) findViewById(R.id.key_et)).setText(authKey);

        String authToken = MobIoTApplication.loadData(CloudSettingsActivity.KEY_AUTH_TOKEN);
        ((EditText) findViewById(R.id.token_et)).setText(authToken);

        String commandId = MobIoTApplication.loadData(CloudSettingsActivity.KEY_COMMAND_ID);
        ((EditText) findViewById(R.id.command_id_et)).setText(commandId);

        String eventId = MobIoTApplication.loadData(CloudSettingsActivity.KEY_EVENT_ID);
        ((EditText) findViewById(R.id.event_id_et)).setText(eventId);

        String brokerURL = MobIoTApplication.loadData(CloudSettingsActivity.KEY_BROKER_URL);
        ((EditText) findViewById(R.id.show_url_et)).setText(brokerURL);

        String brokerURL_port = MobIoTApplication.loadData(CloudSettingsActivity.KEY_PORT);
        ((TextView) findViewById(R.id.show_url_et_port)).setText(brokerURL_port);

        String brokerURL_prefix = MobIoTApplication.loadData(CloudSettingsActivity.KEY_BROKER_URL_PREFIX);
        ((TextView) findViewById(R.id.show_url_et_prefix)).setText(brokerURL_prefix);

        String brokerURL_host = MobIoTApplication.loadData(CloudSettingsActivity.KEY_BROKER_URL_HOST);
        ((TextView) findViewById(R.id.show_url_et_host)).setText(brokerURL_host);

        String connectionType = MobIoTApplication.loadData(CloudSettingsActivity.KEY_CONNECTION_TYPE);

        setConnectionTypeSpinner(connectionType);
    }


    private void saveActualCloud() {
        Spinner cloudSpinner = (Spinner) findViewById(R.id.cloud_providers_spinner);
        int pos = cloudSpinner.getSelectedItemPosition();
        CloudSettingsWrapper cloudSettingsWrapper = cloudSettingsWrappers.get(pos);

        MobIoTApplication.saveData(CloudSettingsActivity.KEY_TYPE, cloudSettingsWrapper.getType().toString());
        try {
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_NAME, cloudSettingsWrapper.getName());
        } catch (Exception ignored) {
            System.out.println(ignored);
        }

        if (cloudSettingsWrapper.getType() == CloudSettingsWrapper.CSType.BLUEMIX_DEMO) {
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_ORGANIZATION_ID, "quickstart");

            MobIoTApplication.saveData(CloudSettingsActivity.KEY_CONNECTION_TYPE, Boolean.toString(true));

        } else {
            String organizationID = ((EditText) findViewById(R.id.organization_id_et)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_ORGANIZATION_ID, organizationID);

            String applicationID = ((EditText) findViewById(R.id.app_id_et)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_APPLICATION_ID, applicationID);

            String authKey = ((EditText) findViewById(R.id.key_et)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_AUTH_KEY, authKey);

            String authToken = ((EditText) findViewById(R.id.token_et)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_AUTH_TOKEN, authToken);

            String commandId = ((EditText) findViewById(R.id.command_id_et)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_COMMAND_ID, commandId);

            String eventId = ((EditText) findViewById(R.id.event_id_et)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_EVENT_ID, eventId);

            String brokerURL = ((EditText) findViewById(R.id.show_url_et)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_BROKER_URL, brokerURL);

            String brokerURL_port = ((TextView) findViewById(R.id.show_url_et_port)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_PORT, brokerURL_port);

            String brokerURL_prefix = ((TextView) findViewById(R.id.show_url_et_prefix)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_BROKER_URL_PREFIX, brokerURL_prefix);

            String brokerURL_host = ((TextView) findViewById(R.id.show_url_et_host)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_BROKER_URL_HOST, brokerURL_host);


            String connectionType = ((Spinner) findViewById(R.id.connection_type_spinner)).getSelectedItem().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_CONNECTION_TYPE, connectionType);
        }
        System.out.println("Cloud has been saved to shared preferences.");
    }


    private void initCloudProvidersSpinner(String overrideSelected) {

        ArrayList<String> providers = createProvidersList();

        Spinner spinner = (Spinner) findViewById(R.id.cloud_providers_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, providers);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        String currentlySelected = MobIoTApplication.loadData(CloudSettingsActivity.KEY_NAME);
        System.out.println("Selectedcloud: " + currentlySelected);

        if (currentlySelected != null) {
            spinner.setSelection(providers.indexOf(currentlySelected));
        }

        if (overrideSelected != null) {
            spinner.setSelection(providers.indexOf(overrideSelected));
        }

        //TODO disable if devices are running
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                Toast.makeText(CloudActivity.this, "Activated: " + item, Toast.LENGTH_SHORT).show();

                if (item.equals("Bluemix Demo")) {

                    ((EditText) findViewById(R.id.organization_id_et)).setText("quickstart");
                    (findViewById(R.id.app_id_container)).setVisibility(View.GONE);
                    (findViewById(R.id.key_container)).setVisibility(View.GONE);
                    (findViewById(R.id.token_container)).setVisibility(View.GONE);
                    (findViewById(R.id.connection_type_container)).setVisibility(View.GONE);

                    String connectionType = "true";

                    setConnectionTypeSpinner(connectionType);

                } else {
                    CloudSettingsWrapper cloudSettingsWrapper = cloudSettingsWrappers.get(position);

                    String organizationID = cloudSettingsWrapper.getOrganizationID();
                    ((EditText) findViewById(R.id.organization_id_et)).setText(organizationID);

                    String applicationID = cloudSettingsWrapper.getApplicationID();
                    (findViewById(R.id.organization_id_container)).setVisibility(View.VISIBLE);
                    ((EditText) findViewById(R.id.app_id_et)).setText(applicationID);

                    String authKey = cloudSettingsWrapper.getAuthKey();
                    (findViewById(R.id.key_container)).setVisibility(View.VISIBLE);
                    ((EditText) findViewById(R.id.key_et)).setText(authKey);

                    String authToken = cloudSettingsWrapper.getAuthToken();
                    (findViewById(R.id.token_container)).setVisibility(View.VISIBLE);
                    ((EditText) findViewById(R.id.token_et)).setText(authToken);

                    String commandID = cloudSettingsWrapper.getCommandID();
                    (findViewById(R.id.command_id_container)).setVisibility(View.VISIBLE);
                    ((EditText) findViewById(R.id.command_id_et)).setText(commandID);

                    String eventId = cloudSettingsWrapper.getEventID();
                    (findViewById(R.id.event_id_container)).setVisibility(View.VISIBLE);
                    ((EditText) findViewById(R.id.event_id_et)).setText(eventId);

                    String brokerUrl = cloudSettingsWrapper.getBrokerUrl();
                    (findViewById(R.id.show_url_et)).setVisibility(View.VISIBLE);
                    ((EditText) findViewById(R.id.show_url_et)).setText(brokerUrl);

                    int brokerUrl_port = cloudSettingsWrapper.getPort();
                    ((TextView) findViewById(R.id.show_url_et_port)).setText("" + brokerUrl_port);

                    String brokerUrl_prefix = cloudSettingsWrapper.getBrokerUrlPrefix();
                    ((TextView) findViewById(R.id.show_url_et_prefix)).setText(brokerUrl_prefix);

                    String brokerUrl_host = cloudSettingsWrapper.getBrokerUrlHost();
                    ((TextView) findViewById(R.id.show_url_et_host)).setText(brokerUrl_host);

                    String connectionType = "true";

                    setConnectionTypeSpinner(connectionType);
                }

                // TODO something with this thing
                //MobIoTApplication.saveData(MobIoTApplication.KEY_DEVICES, "");

                saveActualCloud();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing happens
            }
        });
    }


    private ArrayList<String> createProvidersList() {
        ArrayList<String> providers = new ArrayList<>();
        for (CloudSettingsWrapper cloudSettingsWrapper : cloudSettingsWrappers) {
            if (cloudSettingsWrapper.getType() == CloudSettingsWrapper.CSType.BLUEMIX_DEMO) {
                providers.add("Bluemix Demo");
            } else {
                providers.add(cloudSettingsWrapper.getName());
            }
        }
        return providers;
    }


    private void setConnectionTypeSpinner(String connectionType) {
        if (connectionType == null) {
            connectionType = "";
        }
        Spinner connectionTypeSpinner = ((Spinner) findViewById(R.id.connection_type_spinner));
        connectionTypeSpinner.setEnabled(true);
        int count = connectionTypeSpinner.getCount();
        for (int i = 0; i < count; i++) {
            String itemStr = connectionTypeSpinner.getItemAtPosition(i).toString();
            if (connectionType.equals(itemStr)) {
                ((Spinner) findViewById(R.id.connection_type_spinner)).setSelection(i);
                break;
            }
        }
    }


    private void initConnectionTypeSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.connection_type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.connection_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_CLOUD_SETTINGS_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                System.out.println("onActivityResult add bundle " + bundle.toString());

                CloudSettingsWrapper cloudSettingsWrapper = CloudSettingsWrapper.getCloudSettingsWrapper(bundle);
                cloudSettingsWrappers.add(cloudSettingsWrapper);
                adapter.notifyDataSetChanged();
                saveClouds();
                initCloudProvidersSpinner(cloudSettingsWrapper.getName());
            }
        }

        if (requestCode == EDIT_CLOUD_SETTINGS_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                System.out.println("onActivityResult edit bundle " + bundle.toString());
                CloudSettingsWrapper cloudSettingsWrapper = CloudSettingsWrapper.getCloudSettingsWrapper(bundle);

                Integer position = Integer.parseInt(bundle.getString(CloudSettingsActivity.KEY_EDIT_IT));

                if (cloudSettingsWrappers.get(position).equals(cloudSettingsWrapper)) {
                    System.out.println("NOT Edited");
                    return;
                }

                cloudSettingsWrappers.remove(cloudSettingsWrappers.get(position));
                cloudSettingsWrappers.add(position, cloudSettingsWrapper);

                adapter.notifyDataSetChanged();
                saveClouds();
                initCloudProvidersSpinner(cloudSettingsWrapper.getName());
                loadSettingsFromPreferences();

            }
        }
    }


    private void saveClouds() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cloudSettingsWrappers.size(); i++) {
            CloudSettingsWrapper cloudSettingsWrapper = cloudSettingsWrappers.get(i);
            sb.append("<");
            sb.append(cloudSettingsWrapper.getSerial());
        }
        MobIoTApplication.saveData(MobIoTApplication.KEY_CLOUDS, sb.toString());
    }

}
