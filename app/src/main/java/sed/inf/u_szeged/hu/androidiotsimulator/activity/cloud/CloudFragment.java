package sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.model.cloudsettings.CloudSettingsWrapper;

import static android.app.Activity.RESULT_OK;

public class CloudFragment extends Fragment {

    public final static int ADD_CLOUD_SETTINGS_REQ_CODE = 876;
    public final static int EDIT_CLOUD_SETTINGS_REQ_CODE = 543;

    ArrayList<CloudSettingsWrapper> cloudSettingsWrappers;
    ArrayAdapter<String> adapter;

    View viewFragment;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewFragment = inflater.inflate(R.layout.activity_cloud, container, false);
        return viewFragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MobIoTApplication.setActivity(getActivity());
        cloudSettingsWrappers = loadCloudSettingsWrappers();
        saveClouds();
        initCloudProvidersSpinner(null);
        initConnectionTypeSpinner();
        loadSettingsFromPreferences();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_toolbar_cloud, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(getActivity(), CloudSettingsActivity.class);
        Bundle bundle = new Bundle();

        switch (item.getItemId()) {
            case R.id.ic_edit_cloud:
                editCurrentCloud();
                break;
            case R.id.ic_delete_cloud:
                deleteCurrentCloud();
                break;
            case R.id.ic_cloud_type_bluemix_demo:
                /*bundle.putString(CloudSettingsActivity.KEY_TYPE, CloudSettingsWrapper.CSType.BLUEMIX_DEMO.toString());
                bundle.putString(CloudSettingsActivity.KEY_ORGANIZATION_ID, "quickstart");
                intent.putExtras(bundle);
                startActivityForResult(intent, ADD_CLOUD_SETTINGS_REQ_CODE);*/
                Toast.makeText(getActivity(), "Removed feature", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ic_cloud_type_bluemix_regular:
                /*bundle.putString(CloudSettingsActivity.KEY_TYPE, CloudSettingsWrapper.CSType.BLUEMIX.toString());
                intent.putExtras(bundle);
                intent.putExtra("title", getString(R.string.title_activity_cloud_settings_add));
                startActivityForResult(intent, ADD_CLOUD_SETTINGS_REQ_CODE);*/
                Toast.makeText(getActivity(), "Removed feature", Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    private void deleteCurrentCloud(){
        Spinner spinner = (Spinner) viewFragment.findViewById(R.id.cloud_providers_spinner);
        final String currentlySelected = MobIoTApplication.loadData(CloudSettingsActivity.KEY_NAME);
        final ArrayList<String> providers = createProvidersList();

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        if (cloudSettingsWrappers.size() > 1) {
                            cloudSettingsWrappers.remove(providers.indexOf(currentlySelected));
                            adapter.notifyDataSetChanged();
                            saveClouds();
                            selectDefaultCloud();
                            initCloudProvidersSpinner(null);
                            loadSettingsFromPreferences();
                        }else{
                            Toast.makeText(getActivity(), "Can not delete last cloud!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
        builder.setMessage(R.string.delete_this_cloud_dialog_msg).setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void editCurrentCloud(){
        Intent intent = new Intent(getActivity(), CloudSettingsActivity.class);
        Bundle bundle = new Bundle();

        int position = ((Spinner) viewFragment.findViewById(R.id.cloud_providers_spinner)).getSelectedItemPosition();

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
        bundle.putString("title",getString(R.string.title_activity_cloud_settings));
        intent.putExtras(bundle);
        startActivityForResult(intent, EDIT_CLOUD_SETTINGS_REQ_CODE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void selectDefaultCloud(){
        ArrayList<CloudSettingsWrapper> listCloud = loadCloudSettingsWrappers();
        CloudSettingsWrapper newSelectedCloud = listCloud.get(0);
        HashMap<String,String> keepDataMap = new HashMap<>();

        keepDataMap.put(MobIoTApplication.KEY_CLOUDS,MobIoTApplication.loadData(MobIoTApplication.KEY_CLOUDS) );
        keepDataMap.put(MobIoTApplication.KEY_DEVICES,MobIoTApplication.loadData(MobIoTApplication.KEY_DEVICES) );
        keepDataMap.put(MobIoTApplication.KEY_DEVICEKEYS,MobIoTApplication.loadData(MobIoTApplication.KEY_DEVICEKEYS) );

        MobIoTApplication.clearSharedPreferencesWithExceptions(keepDataMap);

        MobIoTApplication.saveData(CloudSettingsActivity.KEY_TYPE, newSelectedCloud.getType().toString());
        MobIoTApplication.saveData(CloudSettingsActivity.KEY_NAME, newSelectedCloud.getName());
        MobIoTApplication.saveData(CloudSettingsActivity.KEY_ORGANIZATION_ID, newSelectedCloud.getOrganizationID());
        MobIoTApplication.saveData(CloudSettingsActivity.KEY_CONNECTION_TYPE, "true");
    }

    private void loadSettingsFromPreferences() {
        String organizationID = MobIoTApplication.loadData(CloudSettingsActivity.KEY_ORGANIZATION_ID);
        ((EditText) viewFragment.findViewById(R.id.organization_id_et)).setText(organizationID);

        String applicationID = MobIoTApplication.loadData(CloudSettingsActivity.KEY_APPLICATION_ID);
        ((EditText) viewFragment.findViewById(R.id.app_id_et)).setText(applicationID);

        String authKey = MobIoTApplication.loadData(CloudSettingsActivity.KEY_AUTH_KEY);
        ((EditText) viewFragment.findViewById(R.id.key_et)).setText(authKey);

        String authToken = MobIoTApplication.loadData(CloudSettingsActivity.KEY_AUTH_TOKEN);
        ((EditText) viewFragment.findViewById(R.id.token_et)).setText(authToken);

        String commandId = MobIoTApplication.loadData(CloudSettingsActivity.KEY_COMMAND_ID);
        ((EditText) viewFragment.findViewById(R.id.command_id_et)).setText(commandId);

        String eventId = MobIoTApplication.loadData(CloudSettingsActivity.KEY_EVENT_ID);
        ((EditText) viewFragment.findViewById(R.id.event_id_et)).setText(eventId);

        String connectionType = MobIoTApplication.loadData(CloudSettingsActivity.KEY_CONNECTION_TYPE);

        setConnectionTypeSpinner(connectionType);
    }

    private void saveActualCloud() {
        Spinner cloudSpinner = (Spinner) viewFragment.findViewById(R.id.cloud_providers_spinner);
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
            String organizationID = ((EditText) viewFragment.findViewById(R.id.organization_id_et)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_ORGANIZATION_ID, organizationID);

            String applicationID = ((EditText) viewFragment.findViewById(R.id.app_id_et)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_APPLICATION_ID, applicationID);

            String authKey = ((EditText) viewFragment.findViewById(R.id.key_et)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_AUTH_KEY, authKey);

            String authToken = ((EditText) viewFragment.findViewById(R.id.token_et)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_AUTH_TOKEN, authToken);

            String commandId = ((EditText) viewFragment.findViewById(R.id.command_id_et)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_COMMAND_ID, commandId);

            String eventId = ((EditText) viewFragment.findViewById(R.id.event_id_et)).getText().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_EVENT_ID, eventId);

            String connectionType = ((Spinner) viewFragment.findViewById(R.id.connection_type_spinner)).getSelectedItem().toString();
            MobIoTApplication.saveData(CloudSettingsActivity.KEY_CONNECTION_TYPE, connectionType);
        }
    }

    private void initCloudProvidersSpinner(String overrideSelected) {

        ArrayList<String> providers = createProvidersList();

        Spinner spinner = (Spinner) viewFragment.findViewById(R.id.cloud_providers_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, providers); //this volt eredetileg
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        String currentlySelected = MobIoTApplication.loadData(CloudSettingsActivity.KEY_NAME);
        System.out.println("Selected cloud: " + currentlySelected);  // currentlySelected = rhh

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
                Toast.makeText(getActivity(), "Activated: " + item, Toast.LENGTH_SHORT).show();

                if (item.equals("Bluemix Demo")) {

                    ((EditText) viewFragment.findViewById(R.id.organization_id_et)).setText("quickstart");
                    (viewFragment.findViewById(R.id.app_id_container)).setVisibility(View.GONE);
                    (viewFragment.findViewById(R.id.key_container)).setVisibility(View.GONE);
                    (viewFragment.findViewById(R.id.token_container)).setVisibility(View.GONE);
                    (viewFragment.findViewById(R.id.connection_type_container)).setVisibility(View.GONE);

                    String connectionType = "true";

                    setConnectionTypeSpinner(connectionType);

                } else {
                    CloudSettingsWrapper cloudSettingsWrapper = cloudSettingsWrappers.get(position);

                    String organizationID = cloudSettingsWrapper.getOrganizationID();
                    ((EditText) viewFragment.findViewById(R.id.organization_id_et)).setText(organizationID);

                    String applicationID = cloudSettingsWrapper.getApplicationID();
                    (viewFragment.findViewById(R.id.organization_id_container)).setVisibility(View.VISIBLE);
                    ((EditText) viewFragment.findViewById(R.id.app_id_et)).setText(applicationID);

                    String authKey = cloudSettingsWrapper.getAuthKey();
                    (viewFragment.findViewById(R.id.key_container)).setVisibility(View.VISIBLE);
                    ((EditText) viewFragment.findViewById(R.id.key_et)).setText(authKey);

                    String authToken = cloudSettingsWrapper.getAuthToken();
                    (viewFragment.findViewById(R.id.token_container)).setVisibility(View.VISIBLE);
                    ((EditText) viewFragment.findViewById(R.id.token_et)).setText(authToken);

                    String commandID = cloudSettingsWrapper.getCommandID();
                    (viewFragment.findViewById(R.id.command_id_container)).setVisibility(View.VISIBLE);
                    ((EditText) viewFragment.findViewById(R.id.command_id_et)).setText(commandID);

                    String eventId = cloudSettingsWrapper.getEventID();
                    (viewFragment.findViewById(R.id.event_id_container)).setVisibility(View.VISIBLE);
                    ((EditText) viewFragment.findViewById(R.id.event_id_et)).setText(eventId);

                    String connectionType = "true";

                    setConnectionTypeSpinner(connectionType);
                }
                saveActualCloud();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing happens
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
            CloudSettingsWrapper aws = new CloudSettingsWrapper(CloudSettingsWrapper.CSType.AWS, "myAWS", "empty", false, 1883, "mobiotsim", "empty", "empty", "cmd", "status");
            wrappers.add(aws);
        }

        return wrappers;
    }

    private void setConnectionTypeSpinner(String connectionType) {
        if (connectionType == null) {
            connectionType = "";
        }
        Spinner connectionTypeSpinner = ((Spinner) viewFragment.findViewById(R.id.connection_type_spinner));
        connectionTypeSpinner.setEnabled(false);
        int count = connectionTypeSpinner.getCount();
        for (int i = 0; i < count; i++) {
            String itemStr = connectionTypeSpinner.getItemAtPosition(i).toString();
            if (connectionType.equals(itemStr)) {
                ((Spinner) viewFragment.findViewById(R.id.connection_type_spinner)).setSelection(i);
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    private void initConnectionTypeSpinner() {
        Spinner spinner = (Spinner) viewFragment.findViewById(R.id.connection_type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.connection_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

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