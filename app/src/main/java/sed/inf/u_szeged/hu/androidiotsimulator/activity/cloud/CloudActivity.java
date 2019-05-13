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
        findViewById(R.id.new_cloud_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(CloudActivity.this);
                builder.setTitle("Select a Cloud Type");
                final String[] cloudTypes = {"Bluemix Quickstart demo", "Bluemix regular"};
                builder.setItems(cloudTypes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(CloudActivity.this, CloudSettingsActivity.class);
                        Bundle bundle = new Bundle();

                        switch (which) {
                            case 0:
                                bundle.putString(CloudSettingsActivity.KEY_TYPE, CloudSettingsWrapper.CSType.BLUEMIX_DEMO.toString());
                                bundle.putString(CloudSettingsActivity.KEY_ORGANIZATION_ID, "quickstart");
                                break;
                            case 1:
                                bundle.putString(CloudSettingsActivity.KEY_TYPE, CloudSettingsWrapper.CSType.BLUEMIX.toString());
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

        findViewById(R.id.edit_btn).setOnClickListener(new View.OnClickListener() {
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
                }

                bundle.putString(CloudSettingsActivity.KEY_NAME,
                        cloudSettingsWrappers.get(position).getName());

                bundle.putString(CloudSettingsActivity.KEY_EDIT_IT, String.valueOf(position));

                intent.putExtras(bundle);
                startActivityForResult(intent, EDIT_CLOUD_SETTINGS_REQ_CODE);
            }
        });

        findViewById(R.id.delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner spinner = findViewById(R.id.cloud_providers_spinner);
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
            // TODO REMOVE testBluemix
            CloudSettingsWrapper testBluemix = new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX, "testBluemix", "ahwgcy", false, 443, "mobiotsim", "empty", "empty", "cmd", "status");
            wrappers.add(testBluemix);
            CloudSettingsWrapper demoBluemix = new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX_DEMO);
            wrappers.add(demoBluemix);
            CloudSettingsWrapper myBluemix = new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX, "myBluemix", "wg3go6", false, 1883, "mobiotsim", "empty", "empty", "cmd", "status");
            wrappers.add(myBluemix);
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

        String connectionType = MobIoTApplication.loadData(CloudSettingsActivity.KEY_CONNECTION_TYPE);

        setConnectionTypeSpinner(connectionType);
    }


    private void saveActualCloud() {
        Spinner cloudSpinner = findViewById(R.id.cloud_providers_spinner);
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

        } else if (cloudSettingsWrapper.getType() == CloudSettingsWrapper.CSType.BLUEMIX) {
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

            String connectionType = ((Spinner) findViewById(R.id.connection_type_spinner)).getSelectedItem().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_CONNECTION_TYPE, connectionType);
        }
    }


    private void initCloudProvidersSpinner(String overrideSelected) {

        ArrayList<String> providers = createProvidersList();

        Spinner spinner = findViewById(R.id.cloud_providers_spinner);
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

                    String connectionType = "true";

                    setConnectionTypeSpinner(connectionType);
                }

                // TODO something with this thing
                MobIoTApplication.saveData(MobIoTApplication.KEY_DEVICES, "");

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
        Spinner connectionTypeSpinner = findViewById(R.id.connection_type_spinner);
        connectionTypeSpinner.setEnabled(false);
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
        Spinner spinner = findViewById(R.id.connection_type_spinner);
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
