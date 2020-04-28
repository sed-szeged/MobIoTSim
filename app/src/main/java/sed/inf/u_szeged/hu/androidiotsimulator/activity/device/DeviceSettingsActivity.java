package sed.inf.u_szeged.hu.androidiotsimulator.activity.device;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.adapter.ParameterAdapter;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.controller.RESTTools;
import sed.inf.u_szeged.hu.androidiotsimulator.model.cloudsettings.CloudSettingsWrapper;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.Device;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.DeviceGroup;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.SensorData;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.SensorDataWrapper;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.device.GsonDevice;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.device.Sensor;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.devicetype.Result;
import sed.inf.u_szeged.hu.androidiotsimulator.views.ExpandedListView;

import static sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment.deviceGroupAdapter;
import static sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment.deviceGroupList;

public class DeviceSettingsActivity extends AppCompatActivity {


    public static final String KEY_ORGANIZATION_ID = "ORGANIZATION_ID";
    public static final String KEY_TYPE_ID = "TYPE_ID";
    public static final String KEY_DEVICE_ID = "KEY_DEVICE_ID";
    public static final String KEY_PASSWORD = "KEY_PASSWORD";
    public static final String KEY_TOPICS = "KEY_TOPICS";
    public static final String KEY_TOKEN = "TOKEN";
    public static final String KEY_TYPE = "TYPE";
    public static final String KEY_FREQ = "FREQ";
    public static final String KEY_EDIT_IT = "EDIT_IT";
    public static final String KEY_SENSORS = "SENSORS";
    public static final String KEY_TRACE_LOCATION = "TRACELOCATION";
    public static final String KEY_NUM_OF_DEVICES = "NUM_OF_DEVICES";
    public static final String KEY_SAVE_TRACE = "SAVE TRACE";
    public static final int MSG_W_DELETE_PARAMETER = 39;
    private static final int IMPORT_TRACE_LOCATION_REQ_CODE = 6544;
    private static ParameterAdapter adapter;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_W_DELETE_PARAMETER:
                    System.out.println("msg warning");
                    deleteParamter(msg.arg1);
                    break;

            }

        }
    };
    String editId;
    ExpandedListView listView;
    File myExternalFile;
    Gson gson = new Gson();
    String traceFileLocation;
    Switch aSwitch;
    Resources res;
    ArrayList<String> deviceTypeIds;
    RESTTools restTools;

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private void deleteParamter(int position) {
        System.out.println("DELETE " + adapter.getItem(position));
        adapter.remove(adapter.getItem(position));
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMPORT_TRACE_LOCATION_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    // Get the URI of the selected file
                    final Uri uri = data.getData();
                    System.out.println("Uri = " + uri.toString());
                    try {
                        // Get the file path from the URI

                        final String path = getRealPathFromURI(uri);
                        Toast.makeText(DeviceSettingsActivity.this,
                                "File imported: " + path, Toast.LENGTH_LONG).show();
                        traceFileLocation = path;
                        System.out.print("traceFileLocation= " + traceFileLocation);
                        ((TextView) findViewById(R.id.trace_import_location)).setText(traceFileLocation);
                    } catch (Exception e) {
                        System.out.println("DeviceSettingsActivity" + " File select error" + e);
                    }
                }
            }
        }
    }


    private String getRealPathFromURI(Uri contentURI) {
        final String docId = DocumentsContract.getDocumentId(contentURI);
        final String[] split = docId.split(":");
        return Environment.getExternalStorageDirectory() + "/" + split[1];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        String auth_key = MobIoTApplication.loadData(CloudSettingsActivity.KEY_AUTH_KEY);
        String auth_token = MobIoTApplication.loadData(CloudSettingsActivity.KEY_AUTH_TOKEN);
        String orgId = MobIoTApplication.loadData(CloudSettingsActivity.KEY_ORGANIZATION_ID);

        restTools = new RESTTools(orgId, auth_key, auth_token, getApplicationContext());

        MobIoTApplication.setActivity(this);

        res = getResources();

        listView = (ExpandedListView) findViewById(R.id.list);
        SensorDataWrapper sdw;

        aSwitch = (Switch) findViewById(R.id.sw_random);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on) {
                if (on) {
                    //Do something when Switch button is on/checked
                    traceFileLocation = "random";
                    ((Switch) findViewById(R.id.sw_save_trace)).setChecked(true);

                    findViewById(R.id.parameter_container).setVisibility(View.VISIBLE);
                    findViewById(R.id.trace_container).setVisibility(View.GONE);

                } else {
                    //Do something when Switch is off/unchecked
                    ((Switch) findViewById(R.id.sw_save_trace)).setChecked(false);
                    findViewById(R.id.parameter_container).setVisibility(View.GONE);
                    findViewById(R.id.trace_container).setVisibility(View.VISIBLE);


                }
            }
        });

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            System.out.println("if");
            setData(bundle);
            editId = bundle.getString(KEY_EDIT_IT);
            sdw = SensorDataWrapper.sensorDataFromSerial(bundle.getString(KEY_SENSORS));
            getSupportActionBar().setTitle(bundle.getString("title"));
            if( getSupportActionBar().getTitle().toString().equals(getString(R.string.title_activity_device_settings_edit))) {
                EditText deviceId_et = findViewById(R.id.numofdevices_et);
                deviceId_et.setEnabled(false);
            }
        } else {
            System.out.println("else");
            String paramName = res.getString(R.string.new_paramter_name);
            String min = String.valueOf(res.getInteger(R.integer.new_parameter_min));
            String max = String.valueOf(res.getInteger(R.integer.new_parameter_max));
            sdw = SensorDataWrapper.sensorDataFromSerial(paramName + "+" + min + "+" + max);
            System.out.println("DeviceSettingsActivity bundle null");
            initTypeSpinner(0);
        }


        adapter = new ParameterAdapter(sdw.getList(), getApplicationContext());
        listView.setAdapter(adapter);
        getApplicationContext().setTheme(R.style.AppTheme);

        ((Button) findViewById(R.id.add_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String paramName = res.getString(R.string.new_paramter_name);
            String min = String.valueOf(res.getInteger(R.integer.new_parameter_min));
            String max = String.valueOf(res.getInteger(R.integer.new_parameter_max));
            adapter.add(new SensorData(paramName, min, max));
            }
        });

        ((Button) findViewById(R.id.trace_import_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            showFileChooser();
            }
        });

        final EditText topics_et = ((EditText) findViewById(R.id.topics_et));

        topics_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus){
                setDefaultTopics(topics_et);
            }
            }
        });

        topics_et.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
             if (topics_et.getText().toString().equals("")) {
                 setDefaultTopics(topics_et);
             }
             }
         }
        );

//        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
//            ((Button) findViewById(R.id.export_btn)).setEnabled(false);
//        }

        listView.setOnTouchListener(new View.OnTouchListener()

                                    {
                                        // Setting on Touch Listener for handling the touch inside ScrollView
                                        @Override
                                        public boolean onTouch(View v, MotionEvent event) {
                                            // Disallow the touch request for parent scroll on touch of child view
                                            v.getParent().requestDisallowInterceptTouchEvent(true);
                                            return false;
                                        }
                                    }
        );
    }

    private void setDefaultTopics(EditText topics_et){
        String out ="{  \"public/#\": \"r\",  \"/device/";
        //et.setText(((EditText) findViewById(R.id.device_id_et)).getText());
        final Spinner sp = (Spinner) findViewById(R.id.type_spinner);
        out+=String.valueOf(sp.getSelectedItem());
        out+="/";
        final EditText edit = ((EditText) findViewById(R.id.device_id_et));
        out+=edit.getText().toString();
        out+="/#\": \"rw\"}";
        topics_et.setText(out);
    };

    private void saveDeviceToJson() {
        try {
            String fileName = "base_" + ((EditText) findViewById(R.id.device_id_et)).getText() + ".json";
            myExternalFile = new File(getExternalFilesDir("SavedDevices"), fileName);
            FileOutputStream fos = new FileOutputStream(myExternalFile);

            GsonDevice gsonDevice = new GsonDevice();
           // gsonDevice.setOrganizationId((String) ((Spinner) findViewById(R.id.orgid_spinner)).getSelectedItem());
            gsonDevice.setDeviceId(((EditText) findViewById(R.id.device_id_et)).getText().toString());
           // gsonDevice.setTypeId(String.valueOf(((Spinner) findViewById(R.id.typeid_spinner)).getSelectedItem()));
            gsonDevice.setToken(((EditText) findViewById(R.id.token_et)).getText().toString());
            gsonDevice.setPassword(((EditText) findViewById(R.id.password_et)).getText().toString());
            gsonDevice.setTopics(((EditText) findViewById(R.id.topics_et)).getText().toString());
            gsonDevice.setType(String.valueOf(((Spinner) findViewById(R.id.type_spinner)).getSelectedItem()));
            gsonDevice.setFreq(Double.parseDouble(((EditText) findViewById(R.id.freq_value_et)).getText().toString()));
            gsonDevice.setNumOfDevices(Integer.parseInt(((EditText) findViewById(R.id.numofdevices_et)).getText().toString()));
            gsonDevice.setSaveTrace(((Switch) findViewById(R.id.sw_save_trace)).isChecked());

            List<Sensor> list = new ArrayList<>();
            SensorDataWrapper sensorDataWrapper = new SensorDataWrapper(adapter.getResult());
            for (SensorData sd : sensorDataWrapper.getList()) {
                Sensor sensor = new Sensor();
                sensor.setName(sd.getName());
                sensor.setMin(Integer.valueOf(sd.getMinValue()));
                sensor.setMax(Integer.valueOf(sd.getMaxValue()));
                list.add(sensor);
            }

            gsonDevice.setSensors(list);
            gsonDevice.setTraceFileLocation(traceFileLocation);

            String outputString = gson.toJson(gsonDevice);
            fos.write(outputString.getBytes());
            fos.close();
            Toast.makeText(DeviceSettingsActivity.this, "File saved: " + fileName, Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> loadOrganizationIds() {
        String clouds = MobIoTApplication.loadData(MobIoTApplication.KEY_CLOUDS);
        System.out.println("clouds: " + clouds);
        ArrayList<String> orgIds = new ArrayList<>();

        if (clouds != null && !clouds.equals("")) {
            StringTokenizer st = new StringTokenizer(clouds, "<");
            while (st.hasMoreTokens()) {
                String cloudSerial = st.nextToken();
                orgIds.add(CloudSettingsWrapper.fromSerial(cloudSerial).getOrganizationID());
            }
        } else {
            // orgIds.add("null");
        }
        return orgIds;
    }

    private void initOrgIdSpinner(String defaultOrgId) {
        Spinner spinner = (Spinner) findViewById(R.id.orgid_spinner);
        ArrayList<String> organizationIds = loadOrganizationIds();
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, organizationIds);
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(spinnerAdapter);
        int selectedPos = organizationIds.indexOf(defaultOrgId);
        spinner.setSelection(selectedPos);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Nothing to do here
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initTypeSpinner(int selectedPos) {
        Spinner spinner = (Spinner) findViewById(R.id.type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.device_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(spinnerAdapter);

        spinner.setSelection(selectedPos);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                EditText freqET = (EditText) findViewById(R.id.freq_value_et);

                switch (position) {
                    case 0:
                        freqET.setEnabled(true);

                        break;
                    case 1:
                        freqET.setText("1.0");
                        freqET.setEnabled(false);

                        break;
                    case 2:
                        freqET.setText("10.0");
                        freqET.setEnabled(false);

                        break;
                    case 3:
                        freqET.setText("1.0");
                        freqET.setEnabled(false);
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobIoTApplication.setActivity(this);
    }

    private Bundle getData() {
        Bundle bundle = new Bundle();

        //String type_id = String.valueOf(((Spinner) findViewById(R.id.typeid_spinner)).getSelectedItem());
        String type_id = "NA";
        String device_id = ((EditText) findViewById(R.id.device_id_et)).getText().toString();
        String token = ((EditText) findViewById(R.id.token_et)).getText().toString();
        String topics = ((EditText) findViewById(R.id.topics_et)).getText().toString();
        String password = ((EditText) findViewById(R.id.password_et)).getText().toString();
        String organization_id = (String) ((Spinner) findViewById(R.id.orgid_spinner)).getSelectedItem();
        String type = (String) ((Spinner) findViewById(R.id.type_spinner)).getSelectedItem();
        String freq = ((EditText) findViewById(R.id.freq_value_et)).getText().toString();
        String num = ((EditText) findViewById(R.id.numofdevices_et)).getText().toString();
        String saveTrace = String.valueOf(((Switch) findViewById(R.id.sw_save_trace)).isChecked());
        SensorDataWrapper paramResults = new SensorDataWrapper(adapter.getResult());


        if( !passwordValid(password, device_id) || !deviceIdValid(device_id) ){ return null; }

        System.out.println("getData " +
                "\ntype_id:" + type_id +
                "\ndevice_id:" + device_id +
                "\ntoken:" + token +
                "\ntoken:" + topics +
                "\npassword:" + password +
                "\norganization_id:" + organization_id +
                "\ntype:" + type +
                "\nnum: " + num +
                "\nfreq:" + freq +
                "\nsensordata: " + paramResults +
                "\ntracelocation:" + traceFileLocation);

        bundle.putString(KEY_TYPE_ID, type_id);
        bundle.putString(KEY_DEVICE_ID, device_id);
        bundle.putString(KEY_PASSWORD, password);
        bundle.putString(KEY_TOPICS, topics);
        bundle.putString(KEY_TOKEN, token);
        bundle.putString(KEY_ORGANIZATION_ID, organization_id);
        bundle.putString(KEY_NUM_OF_DEVICES, num);
        bundle.putString(KEY_TYPE, type);
        bundle.putString(KEY_FREQ, freq);
        bundle.putString(KEY_SENSORS, paramResults.toString());
        bundle.putString(KEY_TRACE_LOCATION, traceFileLocation);
        bundle.putString(KEY_SAVE_TRACE, saveTrace);

        if (editId != null) {
            bundle.putString(KEY_EDIT_IT, editId);
        }

        return bundle;
    }

    private boolean passwordValid(String password, String device_id ){
        if( password.length() < 8 || device_id.length() < 4){
            Toast.makeText(DeviceSettingsActivity.this,
                    "Please use min 4 characters in Device id and 8 characters in Password fields!" , Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean deviceIdValid( String deviceId){
        for (int i = 0; i<deviceGroupList.size(); i++) {
            if( getSupportActionBar().getTitle().toString().equals(getString(R.string.title_activity_device_settings_edit))){
                if (deviceGroupList.get(i).getBaseDevice().getDeviceID().equals(deviceId) && i != Integer.parseInt(editId)){
                    Toast.makeText(DeviceSettingsActivity.this,
                        "Device group exists: " + deviceId + "! Can not edit!", Toast.LENGTH_SHORT).show();
                    return false;
                }

            }else if(deviceGroupList.get(i).getBaseDevice().getDeviceID().equals(deviceId)){
                Toast.makeText(DeviceSettingsActivity.this,
                    "Device group exists: " + deviceId+ "! Can not add!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void setData(Bundle bundle) {

        String type_id = bundle.getString(KEY_TYPE_ID);

        new TypeSpinnerFillingTask(type_id).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        String device_id = bundle.getString(KEY_DEVICE_ID);
        if (device_id != null && !device_id.trim().equals("")) {
            ((EditText) findViewById(R.id.device_id_et)).setText(device_id);
        }

        String token = bundle.getString(KEY_TOKEN);
        if (token != null && !token.trim().equals("")) {
            ((EditText) findViewById(R.id.token_et)).setText(token);
        }

        String topics = bundle.getString(KEY_TOPICS);
        if (topics != null && !topics.trim().equals("")) {
            ((EditText) findViewById(R.id.topics_et)).setText(topics);
        }

        String password = bundle.getString(KEY_PASSWORD);
        if (password != null && !password.trim().equals("")) {
            ((EditText) findViewById(R.id.password_et)).setText(password);
        }
        initOrgIdSpinner(bundle.getString(KEY_ORGANIZATION_ID));


        String type = bundle.getString(KEY_TYPE);
        String[] deviceTypes = getResources().getStringArray(R.array.device_types);
        for (int i = 0; i < deviceTypes.length; i++) {
            if (type.trim().equals(deviceTypes[i].trim())) {
                initTypeSpinner(i);
                break;
            } else {
                if (i == deviceTypes.length - 1) {
                    initTypeSpinner(0);
                }
            }
        }

        String freq = bundle.getString(KEY_FREQ);
        if (freq != null && !freq.trim().equals("")) {
            ((EditText) findViewById(R.id.freq_value_et)).setText(freq);
        }

        String numOfDevices = bundle.getString(KEY_NUM_OF_DEVICES);
        if (numOfDevices != null && !numOfDevices.trim().equals("")) {
            ((EditText) findViewById(R.id.numofdevices_et)).setText(numOfDevices);
        }

        String saveTrace = bundle.getString(KEY_SAVE_TRACE);
        if (saveTrace != null && !saveTrace.trim().equals("")) {
            ((Switch) findViewById(R.id.sw_save_trace)).setChecked(Boolean.parseBoolean(saveTrace));
        }

        if (!Objects.equals(bundle.getString(KEY_TRACE_LOCATION), "random")) {
            aSwitch.setChecked(false);
            ((TextView) findViewById(R.id.trace_import_location)).setText(bundle.getString(KEY_TRACE_LOCATION));

            traceFileLocation = bundle.getString(KEY_TRACE_LOCATION);

        } else {
            traceFileLocation = "random";
        }

        System.out.println("setData " +
                "\ntype_id:" + type_id +
                "\ndevice_id:" + device_id +
                "\ntoken:" + token +
                "\nfreq:" + freq);
    }

    private void initTypeIdSpinner(String type_id) {
        Spinner spinner = (Spinner) findViewById(R.id.typeid_spinner);
        ArrayList<String> typeIds = deviceTypeIds;

        if (typeIds.contains("Create new type...")) {
            typeIds.remove("Create new type...");
        }

        typeIds.add("Create new type...");
        final int positionOfLast = typeIds.size() - 1;
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, typeIds);
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(spinnerAdapter);
        int selectedPos = deviceTypeIds.indexOf(type_id);
        spinner.setSelection(selectedPos);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == positionOfLast) {
                    System.out.println("New selected");
                    showInputDialog();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void showInputDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(DeviceSettingsActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DeviceSettingsActivity.this, R.style.AlertDialogCustom);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id._dialog_input_et);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    String newType = String.valueOf(editText.getText());
                    deviceTypeIds.add(newType);
                    initTypeIdSpinner(newType);
                    restTools.addDeviceType("{ \"id\" : \"" + newType + "\" }");
                    }
                })
                .setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, IMPORT_TRACE_LOCATION_REQ_CODE);
    }


    private class TypeSpinnerFillingTask extends AsyncTask<String, String, Void> {

        String defaultTypeid;
        List<Result> jsonTypeIds;

        TypeSpinnerFillingTask(String defaultTypeId) {
            this.defaultTypeid = defaultTypeId;
        }

        @Override
        protected Void doInBackground(String... strings) {

 //           jsonTypeIds = restTools.getDeviceTypes();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
         /*   deviceTypeIds = new ArrayList<>();
            for (Result result : jsonTypeIds) {
                deviceTypeIds.add(result.getId());
            }
            initTypeIdSpinner(defaultTypeid);*/
        }
    }


    private boolean areFieldsFilled() {
        boolean result = true;
        if (((EditText) findViewById(R.id.device_id_et)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.device_id_et)).setError(getString(R.string.empty_field));
            result = false;
        }

        if (((EditText) findViewById(R.id.token_et)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.token_et)).setError(getString(R.string.empty_field));
            result = false;
        }
        if (((EditText) findViewById(R.id.password_et)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.password_et)).setError(getString(R.string.empty_field));
            result = false;
        }

        if (((EditText) findViewById(R.id.topics_et)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.topics_et)).setError(getString(R.string.empty_field));
            result = false;
        }

        if (((EditText) findViewById(R.id.numofdevices_et)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.numofdevices_et)).setError(getString(R.string.empty_field));
            result = false;
        }

        if (((EditText) findViewById(R.id.freq_value_et)).getText().toString().equals("")) {
            ((EditText) findViewById(R.id.freq_value_et)).setError(getString(R.string.empty_field));
            result = false;
        }
        return result;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_settings, menu);
        return true;
    }
    private boolean checkIfTopicsValid(String topics, String type, String deviceId){
        //String intext = "/" + type + "/" + deviceId + "/";
        String inText = "{  \"public/#\": \"r\",  \"/device/"+ type + "/" + deviceId + "/#\": \"rw\"}";
        boolean valid = false;
        if (topics != null){
            valid = topics.contains(inText);
        }
        return valid;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            if (areFieldsFilled()) {
                Intent intent = new Intent();
                Bundle bundle = getData();

                if(bundle != null){
                    if ( checkIfTopicsValid( bundle.getString(KEY_TOPICS),bundle.getString(KEY_TYPE),bundle.getString(KEY_DEVICE_ID) ) ){
                        intent.putExtras(bundle);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }else{
                        Toast.makeText(DeviceSettingsActivity.this, "Please use {  \"public/#\": \"r\",  \"/device/TYPE/DEVICE ID/#\": \"rw\"} format in Topics", Toast.LENGTH_LONG).show();
                    }

                }
            }
        }

        if (id == R.id.action_export) {
            saveDeviceToJson();
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }
}
