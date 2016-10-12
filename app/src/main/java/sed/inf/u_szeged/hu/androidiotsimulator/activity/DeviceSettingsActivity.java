package sed.inf.u_szeged.hu.androidiotsimulator.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;

public class DeviceSettingsActivity extends AppCompatActivity {

    public static final String KEY_BUNDLE = "BUNDLE";

    public static final String KEY_TYPE_ID = "TYPE_ID";
    public static final String KEY_DEVICE_ID = "KEY_DEVICE_ID";
    public static final String KEY_TOKEN = "TOKEN";

    public static final String KEY_TYPE = "TYPE";

    public static final String KEY_FREQ = "FREQ";
    public static final String KEY_MIN = "MIN";
    public static final String KEY_MAX = "MAX";

    public static final String KEY_EDIT_IT = "EDIT_IT";

    String editId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);

        MobIoTApplication.setActivity(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            setData(bundle);
            editId = bundle.getString(KEY_EDIT_IT);
        }else{
            initTypeSpinner(0);
            System.out.println("DeviceSettingsActivity bundle null");
        }

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



    }

    private void initTypeSpinner(int selectedPos) {
        Spinner spinner = (Spinner) findViewById(R.id.type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.device_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setSelection(selectedPos);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                EditText freqET = (EditText) findViewById(R.id.freq_value_et);
                EditText minET = (EditText)findViewById(R.id.min_value_et);
                EditText maxET = (EditText)findViewById(R.id.max_value_et);

                switch (position) {
                    case 0:
                        freqET.setEnabled(true);
                        minET.setEnabled(true);
                        maxET.setEnabled(true);
                        break;
                    case 1:
                        freqET.setText("1.0");
                        freqET.setEnabled(false);
                        minET.setText("-25");
                        minET.setEnabled(false);
                        minET.setText("25");
                        maxET.setEnabled(false);
                        break;
                    case 2:
                        freqET.setText("10.0");
                        freqET.setEnabled(false);
                        minET.setText("25");
                        minET.setEnabled(false);
                        minET.setText("85");
                        maxET.setEnabled(false);
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

    private Bundle getData(){
        Bundle bundle = new Bundle();

        String type_id = ((EditText)findViewById(R.id.type_id_et)).getText().toString();
        String device_id = ((EditText)findViewById(R.id.device_id_et)).getText().toString();
        String token = ((EditText)findViewById(R.id.token_et)).getText().toString();

        String type = (String) ((Spinner)findViewById(R.id.type_spinner)).getSelectedItem();

        String freq = ((EditText)findViewById(R.id.freq_value_et)).getText().toString();
        String min = ((EditText)findViewById(R.id.min_value_et)).getText().toString();
        String max = ((EditText)findViewById(R.id.max_value_et)).getText().toString();

        System.out.println("getData " +
                "\ntype_id:"+type_id+
                "\ndevice_id:"+device_id+
                "\ntoken:"+token+
                "\ntype:"+type+
                "\nfreq:"+freq+
                "\nmin:"+min+
                "\nmax:"+max);

        bundle.putString(KEY_TYPE_ID, type_id);
        bundle.putString(KEY_DEVICE_ID, device_id);
        bundle.putString(KEY_TOKEN, token);

        bundle.putString(KEY_TYPE, type);

        bundle.putString(KEY_FREQ, freq);
        bundle.putString(KEY_MIN, min);
        bundle.putString(KEY_MAX, max);

        if(editId != null) {
            bundle.putString(KEY_EDIT_IT, editId);
        }

        return bundle;
    }

    private void setData(Bundle bundle){
        String type_id = bundle.getString(KEY_TYPE_ID);
        if(type_id != null && !type_id.trim().equals("")) {
            ((EditText) findViewById(R.id.type_id_et)).setText(type_id);
        }

        String device_id = bundle.getString(KEY_DEVICE_ID);
        if(device_id != null && !device_id.trim().equals("")) {
            ((EditText) findViewById(R.id.device_id_et)).setText(device_id);
        }

        String token = bundle.getString(KEY_TOKEN);
        if(token != null && !token.trim().equals("")) {
            ((EditText) findViewById(R.id.token_et)).setText(token);
        }

        String type = bundle.getString(KEY_TYPE);
        String[] deviceTypes = getResources().getStringArray(R.array.device_types);
        for(int i=0; i<deviceTypes.length; i++){
            if(type.trim().equals(deviceTypes[i].trim())){
                initTypeSpinner(i);
                break;
            }else{
                if(i==deviceTypes.length-1){
                    initTypeSpinner(0);
                }
            }
        }

        String freq = bundle.getString(KEY_FREQ);
        if(freq != null && !freq.trim().equals("")) {
            ((EditText) findViewById(R.id.freq_value_et)).setText(freq);
        }

        String min = bundle.getString(KEY_MIN);
        if(min != null && !min.trim().equals("")) {
            ((EditText) findViewById(R.id.min_value_et)).setText(min);
        }

        String max = bundle.getString(KEY_MAX);
        if(max != null && !max.trim().equals("")) {
            ((EditText) findViewById(R.id.max_value_et)).setText(max);
        }

        System.out.println("setData " +
                "\ntype_id:"+type_id+
                "\ndevice_id:"+device_id+
                "\ntoken:"+token+
                "\nfreq:"+freq+
                "\nmin:"+min+
                "\nmax:"+max);
    }





}
