package sed.inf.u_szeged.hu.androidiotsimulator.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.adapter.ParameterAdapter;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.Device;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.SensorData;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.SensorDataWrapper;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.JsonDevice;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.Sensor;
import sed.inf.u_szeged.hu.androidiotsimulator.views.ExpandedListView;

public class DeviceSettingsActivity extends AppCompatActivity {


    public static final String KEY_ORGANIZATION_ID = "ORGANIZATION_ID";
    public static final String KEY_TYPE_ID = "TYPE_ID";
    public static final String KEY_DEVICE_ID = "KEY_DEVICE_ID";
    public static final String KEY_TOKEN = "TOKEN";

    public static final String KEY_TYPE = "TYPE";
    public static final String KEY_FREQ = "FREQ";

    public static final String KEY_EDIT_IT = "EDIT_IT";

    public static final String KEY_SENSORS = "SENSORS";

    public static final int MSG_W_DELETE_PARAMETER = 39;

    String editId;

    ExpandedListView listView;
    private static ParameterAdapter adapter;
    File myExternalFile;

    Gson gson = new Gson();


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

    private void deleteParamter(int position) {

        System.out.println("DELETE " + adapter.getItem(position));
        //devices.remove(position);
        adapter.remove(adapter.getItem(position));
        adapter.notifyDataSetChanged();
        //saveDevices();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);

        MobIoTApplication.setActivity(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title);
        setSupportActionBar(toolbar);

        listView = (ExpandedListView) findViewById(R.id.list);
        SensorDataWrapper sdw;

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            System.out.println("if");
            setData(bundle);
            editId = bundle.getString(KEY_EDIT_IT);
            sdw = SensorDataWrapper.sensorDataFromSerial(bundle.getString(KEY_SENSORS));

        } else {
            System.out.println("else");
            sdw = SensorDataWrapper.sensorDataFromSerial("empty1+1+5*empty2+-1+30");
            System.out.println("DeviceSettingsActivity bundle null");
            initTypeSpinner(0);

        }


        adapter = new ParameterAdapter(sdw.getList(), getApplicationContext());
        listView.setAdapter(adapter);
        getApplicationContext().setTheme(R.style.AppTheme);


        ((Button) findViewById(R.id.ok_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtras(getData());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        ((Button) findViewById(R.id.cancel_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("result", getData());
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            }
        });


        ((Button) findViewById(R.id.add_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.add(new SensorData("empty", "0", "0"));
            }
        });


        ((Button) findViewById(R.id.save_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    myExternalFile = new File(getExternalFilesDir("SavedDevices"), ((EditText) findViewById(R.id.device_id_et)).getText() + ".json");
                    FileOutputStream fos = new FileOutputStream(myExternalFile);
                    //String outputString = getSerial(bundle);

                    JsonDevice jsonDevice = new JsonDevice();
                    jsonDevice.setOrganizationId(bundle.getString(KEY_ORGANIZATION_ID));
                    jsonDevice.setDeviceId(((EditText) findViewById(R.id.device_id_et)).getText().toString());
                    jsonDevice.setTypeId(((EditText) findViewById(R.id.type_id_et)).getText().toString());
                    jsonDevice.setToken(((EditText) findViewById(R.id.token_et)).getText().toString());
                    jsonDevice.setType(String.valueOf(((Spinner) findViewById(R.id.type_spinner)).getSelectedItem()));
                    jsonDevice.setCommand("cmd");
                    jsonDevice.setStatus("status");
                    jsonDevice.setFreq(Double.parseDouble(((EditText) findViewById(R.id.freq_value_et)).getText().toString()));

                    List<Sensor> list = new ArrayList<>();
                    SensorDataWrapper sensorDataWrapper = new SensorDataWrapper(adapter.getResult());
                    for (SensorData sd : sensorDataWrapper.getList()) {
                        Sensor sensor = new Sensor();
                        sensor.setName(sd.getName());
                        sensor.setMin(Integer.valueOf(sd.getMinValue()));
                        sensor.setMax(Integer.valueOf(sd.getMaxValue()));
                        list.add(sensor);
                    }

                    jsonDevice.setSensors(list);

                    String outputString = gson.toJson(jsonDevice);
                    fos.write(outputString.getBytes());
                    fos.close();
                    Toast.makeText(DeviceSettingsActivity.this, "File saved: " + ((EditText) findViewById(R.id.device_id_et)).getText() + ".json", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            ((Button) findViewById(R.id.save_btn)).setEnabled(false);
        }


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
//                EditText minET = (EditText)findViewById(R.id.min_value_et);
//                EditText maxET = (EditText)findViewById(R.id.max_value_et);

                switch (position) {
                    case 0:
                        freqET.setEnabled(true);
//                        minET.setEnabled(true);
//                        maxET.setEnabled(true);
                        break;
                    case 1:
                        freqET.setText("1.0");
                        freqET.setEnabled(false);
//                        minET.setText("-25");
//                        minET.setEnabled(false);
//                        minET.setText("25");
//                        maxET.setEnabled(false);
                        break;
                    case 2:
                        freqET.setText("10.0");
                        freqET.setEnabled(false);
//                        minET.setText("25");
//                        minET.setEnabled(false);
//                        minET.setText("85");
//                        maxET.setEnabled(false);
                        break;
                    case 3:
                        freqET.setText("1.0");
                        freqET.setEnabled(false);
//                        minET.setText("-25");
//                        minET.setEnabled(false);
//                        minET.setText("25");
//                        maxET.setEnabled(false);
                        break;

                }

                String item = (String) parent.getItemAtPosition(position);
                //Toast.makeText(DeviceSettingsActivity.this, "Selected: " + item, Toast.LENGTH_SHORT).show();
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

        String type_id = ((EditText) findViewById(R.id.type_id_et)).getText().toString();
        String device_id = ((EditText) findViewById(R.id.device_id_et)).getText().toString();
        String token = ((EditText) findViewById(R.id.token_et)).getText().toString();
        String type = (String) ((Spinner) findViewById(R.id.type_spinner)).getSelectedItem();
        String freq = ((EditText) findViewById(R.id.freq_value_et)).getText().toString();
        SensorDataWrapper paramResults = new SensorDataWrapper(adapter.getResult());

        System.out.println("getData " +
                "\ntype_id:" + type_id +
                "\ndevice_id:" + device_id +
                "\ntoken:" + token +
                "\ntype:" + type +
                "\nfreq:" + freq +
                "\nsensordata: " + paramResults);

        bundle.putString(KEY_TYPE_ID, type_id);
        bundle.putString(KEY_DEVICE_ID, device_id);
        bundle.putString(KEY_TOKEN, token);
        bundle.putString(KEY_TYPE, type);
        bundle.putString(KEY_FREQ, freq);
        bundle.putString(KEY_SENSORS, paramResults.toString());

        if (editId != null) {
            bundle.putString(KEY_EDIT_IT, editId);
        }

        return bundle;
    }

    private void setData(Bundle bundle) {
        String type_id = bundle.getString(KEY_TYPE_ID);
        if (type_id != null && !type_id.trim().equals("")) {
            ((EditText) findViewById(R.id.type_id_et)).setText(type_id);
        }

        String device_id = bundle.getString(KEY_DEVICE_ID);
        if (device_id != null && !device_id.trim().equals("")) {
            ((EditText) findViewById(R.id.device_id_et)).setText(device_id);
        }

        String token = bundle.getString(KEY_TOKEN);
        if (token != null && !token.trim().equals("")) {
            ((EditText) findViewById(R.id.token_et)).setText(token);
        }

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


        System.out.println("setData " +
                "\ntype_id:" + type_id +
                "\ndevice_id:" + device_id +
                "\ntoken:" + token +
                "\nfreq:" + freq);
    }


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


    public String getSerial(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        sb.append(bundle.getString(KEY_ORGANIZATION_ID));
        sb.append("|");
        sb.append(((EditText) findViewById(R.id.type_id_et)).getText());
        sb.append("|");
        sb.append(((EditText) findViewById(R.id.device_id_et)).getText());
        sb.append("|");
        sb.append(((EditText) findViewById(R.id.token_et)).getText());
        sb.append("|");
        sb.append(((Spinner) findViewById(R.id.type_spinner)).getSelectedItem());
        sb.append("|");
        sb.append("cmd");
        sb.append("|");
        sb.append("status");
        sb.append("|");
        sb.append(((EditText) findViewById(R.id.freq_value_et)).getText());
        sb.append("|");
        sb.append(new SensorDataWrapper(adapter.getResult()));

        return sb.toString();
    }


}
