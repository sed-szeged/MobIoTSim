package sed.inf.u_szeged.hu.androidiotsimulator.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.Device;

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title);
        setSupportActionBar(toolbar);

        init();

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobIoTApplication.setActivity(this);
    }

    public void init(){
        cloudSettingsWrappers = loadCloudSettingsWrappers();

        initCloudProvidersSpinner();


        initConnectionTypeSpinner();

        load();

        ((Button)findViewById(R.id.new_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(CloudActivity.this, CloudSettingsActivity.class);
                //startActivityForResult(intent, ADD_CLOUD_SETTINGS_REQ_CODE);

                AlertDialog.Builder builder = new AlertDialog.Builder(CloudActivity.this);
                builder.setTitle("Select a Cloud Type");
                String cloudTypes[] = {"Bluemix Demo", "Bluemix"};
                builder.setSingleChoiceItems(cloudTypes, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

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

        ((Button)findViewById(R.id.edit_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CloudActivity.this, CloudSettingsActivity.class);
                Bundle bundle = new Bundle();

                //bundle.putString(CloudSettingsActivity.KEY_TYPE_ID, deviceAdapter.getItem(i).getTypeID());

                //bundle.putString(DeviceSettingsActivity.KEY_EDIT_IT, String.valueOf( i ));

                intent.putExtras(bundle);
                startActivityForResult(intent, EDIT_CLOUD_SETTINGS_REQ_CODE);
            }
        });
    }

    private ArrayList<CloudSettingsWrapper> loadCloudSettingsWrappers() {
        String clouds = MobIoTApplication.loadData(MobIoTApplication.KEY_CLOUDS);
        System.out.println("clouds: " + clouds);
        ArrayList<CloudSettingsWrapper> wrappers = new ArrayList<CloudSettingsWrapper>();

        if(clouds != null && !clouds.equals("")){
            StringTokenizer st = new StringTokenizer(clouds, "<");
            while(st.hasMoreTokens()) {
                String cloudSerial = st.nextToken();
                wrappers.add(CloudSettingsWrapper.fromSerial(cloudSerial));
            }
        }else{
            CloudSettingsWrapper demoBluemix = new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX_DEMO);
            wrappers.add(demoBluemix);
            CloudSettingsWrapper myBluemix = new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX, "myBluemix", "wg3go6", false, 1883, "mobiotsim", "", "");
            wrappers.add(myBluemix);
            //CloudSettingsWrapper myBluemix2 = new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX, "myBluemix2", "2wg3go62", false, 1883);
            //wrappers.add(myBluemix2);
            //CloudSettingsWrapper myAzure = new CloudSettingsWrapper(CloudSettingsWrapper.CSType.AZURE, "myAzure", "myAzure", false);
            //wrappers.add(myAzure);
        }

        return wrappers;
    }

    private void load() {
        String organizationID = MobIoTApplication.loadData(MobIoTApplication.KEY_ORGATNISATION_ID);
        ((EditText)findViewById(R.id.organizatio_id_et)).setText(organizationID);


        String applicationID = MobIoTApplication.loadData(MobIoTApplication.KEY_APPLICATION_ID);
        ((EditText)findViewById(R.id.app_id_et)).setText(applicationID);


        String authKey = MobIoTApplication.loadData(MobIoTApplication.KEY_AUTH_KEY);
        ((EditText)findViewById(R.id.key_et)).setText(authKey);


        String authToken = MobIoTApplication.loadData(MobIoTApplication.KEY_AUTH_TOKEN);
        ((EditText)findViewById(R.id.token_et)).setText(authToken);


        String connectionType = MobIoTApplication.loadData(MobIoTApplication.KEY_CONNECTION_TYPE);
        if(connectionType == null){
            connectionType = "";
        }
        Spinner connectionTypeSpinner = ((Spinner)findViewById(R.id.connection_type_spinner));
        int count = connectionTypeSpinner.getCount();
        for( int i=0; i<count; i++ ){
            String itemStr = connectionTypeSpinner.getItemAtPosition( i ).toString();
            if(connectionType.equals(itemStr)){
                ((Spinner)findViewById(R.id.connection_type_spinner)).setSelection(i);
                break;
            }
        }
    }

    private void saveActualCloud(){
        Spinner cloudSpinner = (Spinner)findViewById(R.id.cloud_providers_spinner);
        int pos = cloudSpinner.getSelectedItemPosition();
        CloudSettingsWrapper cloudSettingsWrapper = cloudSettingsWrappers.get(pos);

        MobIoTApplication.saveData(MobIoTApplication.KEY_TYPE, cloudSettingsWrapper.getType().toString());

        if(cloudSettingsWrapper.getType() == CloudSettingsWrapper.CSType.BLUEMIX_DEMO){
            String organizationID = ((EditText)findViewById(R.id.organizatio_id_et)).getText().toString();
            MobIoTApplication.saveData(MobIoTApplication.KEY_ORGATNISATION_ID, "quickstart");

            String connectionType = ((Spinner)findViewById(R.id.connection_type_spinner)).getSelectedItem().toString();
            MobIoTApplication.saveData(MobIoTApplication.KEY_CONNECTION_TYPE, Boolean.toString(true));

        }else if(cloudSettingsWrapper.getType() == CloudSettingsWrapper.CSType.BLUEMIX){
            String organizationID = ((EditText)findViewById(R.id.organizatio_id_et)).getText().toString();
            MobIoTApplication.saveData(MobIoTApplication.KEY_ORGATNISATION_ID, organizationID);

            String applicationID = ((EditText)findViewById(R.id.app_id_et)).getText().toString();
            MobIoTApplication.saveData(MobIoTApplication.KEY_APPLICATION_ID, applicationID);

            String authKey = ((EditText)findViewById(R.id.key_et)).getText().toString();
            MobIoTApplication.saveData(MobIoTApplication.KEY_AUTH_KEY, authKey);

            String authToken = ((EditText)findViewById(R.id.token_et)).getText().toString();
            MobIoTApplication.saveData(MobIoTApplication.KEY_AUTH_TOKEN, authToken);

            String connectionType = ((Spinner)findViewById(R.id.connection_type_spinner)).getSelectedItem().toString();
            MobIoTApplication.saveData(MobIoTApplication.KEY_CONNECTION_TYPE, connectionType);
        }
    }

    private void initCloudProvidersSpinner() {

        ArrayList<String> providers = new ArrayList<String>();
        for(CloudSettingsWrapper cloudSettingsWrapper : cloudSettingsWrappers){
            if(cloudSettingsWrapper.getType() == CloudSettingsWrapper.CSType.BLUEMIX_DEMO){
                providers.add("Bluemix Demo");
            }else {
                providers.add(cloudSettingsWrapper.getName());
            }
        }

        Spinner spinner = (Spinner) findViewById(R.id.cloud_providers_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout

        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, providers);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        //TODO disable if devices are running

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                Toast.makeText(CloudActivity.this, "Activated: " + item, Toast.LENGTH_SHORT).show();

                if (item.equals("Bluemix Demo")) {

                    ((EditText) findViewById(R.id.organizatio_id_et)).setText("quickstart");

                    (findViewById(R.id.app_id_container)).setVisibility(View.GONE);

                    (findViewById(R.id.key_container)).setVisibility(View.GONE);

                    (findViewById(R.id.token_container)).setVisibility(View.GONE);

                    Spinner connectionTypeSpinner = ((Spinner) findViewById(R.id.connection_type_spinner));
                    String connectionType = "true";
                    connectionTypeSpinner.setEnabled(false);
                    int count = connectionTypeSpinner.getCount();
                    for (int i = 0; i < count; i++) {
                        String itemStr = connectionTypeSpinner.getItemAtPosition(i).toString();
                        if (connectionType.equals(itemStr)) {
                            ((Spinner) findViewById(R.id.connection_type_spinner)).setSelection(i);
                            break;
                        }
                    }

                } else {
                    CloudSettingsWrapper cloudSettingsWrapper = cloudSettingsWrappers.get(position);

                    String organizationID = cloudSettingsWrapper.getOrganizationID();
                    ((EditText) findViewById(R.id.organizatio_id_et)).setText(organizationID);


                    String applicationID = cloudSettingsWrapper.getApplicationID();
                    (findViewById(R.id.organizatio_id_container)).setVisibility(View.VISIBLE);
                    ((EditText) findViewById(R.id.app_id_et)).setText(applicationID);


                    String authKey = cloudSettingsWrapper.getAuthKey();
                    (findViewById(R.id.key_container)).setVisibility(View.VISIBLE);
                    ((EditText) findViewById(R.id.key_et)).setText(authKey);


                    String authToken = cloudSettingsWrapper.getAuthToken();
                    (findViewById(R.id.token_container)).setVisibility(View.VISIBLE);
                    ((EditText) findViewById(R.id.token_et)).setText(authToken);


                    String connectionType = "true";
                    if (connectionType == null) {
                        connectionType = "";
                    }
                    Spinner connectionTypeSpinner = ((Spinner) findViewById(R.id.connection_type_spinner));
                    int count = connectionTypeSpinner.getCount();
                    for (int i = 0; i < count; i++) {
                        String itemStr = connectionTypeSpinner.getItemAtPosition(i).toString();
                        if (connectionType.equals(itemStr)) {
                            ((Spinner) findViewById(R.id.connection_type_spinner)).setSelection(i);
                            break;
                        }
                    }

                }

                saveActualCloud();

                //TODO recreate devices
                MobIoTApplication.saveData(MobIoTApplication.KEY_DEVICES, "");

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initConnectionTypeSpinner(){
        Spinner spinner = (Spinner) findViewById(R.id.connection_type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.connection_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                //Toast.makeText(CloudActivity.this, "Selected: " + item, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ADD_CLOUD_SETTINGS_REQ_CODE){
            if(resultCode == RESULT_OK){
                //devices.set()
                Bundle bundle = data.getExtras();
                if(bundle != null) {
                    System.out.println("onActivityResult add bundle " + bundle.toString());
                    CloudSettingsWrapper cloudSettingsWrapper;
                    if(bundle.getString(CloudSettingsActivity.KEY_TYPE).equals("BLUEMIX")){
                        cloudSettingsWrapper = new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX,
                                bundle.getString(CloudSettingsActivity.KEY_NAME),
                                bundle.getString(CloudSettingsActivity.KEY_ORGATNISATION_ID),
                                true,
                                1883,
                                bundle.getString(CloudSettingsActivity.KEY_APPLICATION_ID),
                                bundle.getString(CloudSettingsActivity.KEY_AUTH_KEY),
                                bundle.getString(CloudSettingsActivity.KEY_AUTH_TOKEN)
                                );
                    }else{
                        cloudSettingsWrapper = new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX_DEMO) ;
                    }

                    cloudSettingsWrappers.add(cloudSettingsWrapper);
                    adapter.notifyDataSetChanged();
                    saveClouds();
                }else{
                    System.out.println("onActivityResult add bundle NULL");
                }
            }
        }

        if(requestCode == EDIT_CLOUD_SETTINGS_REQ_CODE){
            if(resultCode == RESULT_OK){
                //devices.set()
                Bundle bundle = data.getExtras();
                if(bundle != null) {
                    System.out.println("onActivityResult edit bundle " + bundle.toString());
                    CloudSettingsWrapper cloudSettingsWrapper;
                    if(bundle.getString(CloudSettingsActivity.KEY_TYPE).equals("BLUEMIX")){
                        cloudSettingsWrapper = new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX,
                                bundle.getString(CloudSettingsActivity.KEY_NAME),
                                bundle.getString(CloudSettingsActivity.KEY_ORGATNISATION_ID),
                                true,
                                1883,
                                bundle.getString(CloudSettingsActivity.KEY_APPLICATION_ID),
                                bundle.getString(CloudSettingsActivity.KEY_AUTH_KEY),
                                bundle.getString(CloudSettingsActivity.KEY_AUTH_TOKEN)
                                );
                    }else{
                        cloudSettingsWrapper = new CloudSettingsWrapper(CloudSettingsWrapper.CSType.BLUEMIX_DEMO) ;
                    }

                    Integer position = Integer.parseInt( bundle.getString(DeviceSettingsActivity.KEY_EDIT_IT) );

                    if(cloudSettingsWrappers.get(position).equals(cloudSettingsWrapper)){
                        System.out.println("NOT Edited");
                        return;
                    }

                    System.out.println("Edit cloud from " + position + " : " + cloudSettingsWrappers.get(position));

                    cloudSettingsWrappers.remove(position);
                    adapter.notifyDataSetChanged();
                    System.out.println("Edit cloud to: " + cloudSettingsWrapper);

                    cloudSettingsWrappers.add(position, cloudSettingsWrapper);
                    adapter.notifyDataSetChanged();
                    saveClouds();

                }else{
                    System.out.println("onActivityResult edit bundle NULL");
                }
            }
        }
    }

    private void saveClouds() {

        StringBuilder sb = new StringBuilder();
        //for( Device d : devices ){
        for( int i=0; i<cloudSettingsWrappers.size(); i++) {
            CloudSettingsWrapper cloudSettingsWrapper = cloudSettingsWrappers.get(i);
            sb.append("<");
            sb.append(cloudSettingsWrapper.getSerial());
        }
        MobIoTApplication.saveData(MobIoTApplication.KEY_CLOUDS, sb.toString());

    }


}
