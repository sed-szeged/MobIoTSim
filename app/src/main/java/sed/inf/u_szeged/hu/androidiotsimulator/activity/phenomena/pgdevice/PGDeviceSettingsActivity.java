package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.pgdevice;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.ActivityUtility;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.simulation.SimulationSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.simulation.SimulationsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.subject.SubjectsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.Device;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Measurement;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Parameter;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenDevice;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenDeviceSensorType;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenDeviceType;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenMain;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Population;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Simulation;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Subject;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Theme;

import static sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.simulation.SimulationsActivity.ADD_SIMULATION_REQ_CODE;

public class PGDeviceSettingsActivity extends AppCompatActivity {

    public static final int REATTACH_REQ_CODE = 1301;
    public static final int DETACH_REQ_CODE = 1308;
    public static final int REMOVE_REQ_CODE = 1801;

    private Simulation currentSimulation;
    private long currentSimulationKey;
    private PhenGenDevice currentPGDevice;
    private long currentPGDeviceKey;
    private PhenGenDeviceType newDeviceType;

    PGDeviceSensorAdapter pgDeviceSensorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pgdevice_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentSimulationKey = getIntent().getExtras().getLong(PhenGenMain.KEY_SIMULATION);
        currentSimulation = PhenGenMain.getPhenGenMain().getSimulationByKey(currentSimulationKey);
        currentPGDeviceKey = getIntent().getExtras().getLong(PhenGenMain.KEY_PGDEVICE);
        if (currentPGDeviceKey == PhenGenMain.NEW_ELEMENT) {
            String newDeviceTypeStr = getIntent().getExtras().getString(PhenGenMain.KEY_PGDEVICE_TYPE);
            for (PhenGenDeviceType type : currentSimulation.theme.getPhenGenDeviceTypes()) {
                if (type.getDeviceInnerType().equals(newDeviceTypeStr)) {
                    newDeviceType = type;
                    break;
                }
            }
            if (newDeviceType==null) {
                //TODO: safer solution would be better (may be a default type, or closing the Activity)
                System.out.println("PhenGenDeviceType not found in theme. Type:" + newDeviceTypeStr);
                return;
            }
            currentPGDevice = currentSimulation.createDevice(newDeviceType);
        } else {
            currentPGDevice = PhenGenMain.getPhenGenMain().getPGDeviceByKey(currentPGDeviceKey);
        }

        TextView simulationName = (TextView) findViewById(R.id.simulation_name);
            if (simulationName != null) simulationName.setText(currentSimulation.getName());
        TextView themeName = (TextView) findViewById(R.id.simulation_theme);
            if (themeName != null) themeName.setText(currentSimulation.theme.getName());
        TextView pgdevice_name = (TextView) findViewById(R.id.pgdevice_name);
            if (pgdevice_name != null) pgdevice_name.setText(currentPGDevice.getDeviceID());
        /*TextView pgdevice_attachedSubject = (TextView) findViewById(R.id.attachedSubject_name);
            Iterator<Measurement> iteratorM = currentPGDevice.getAttachedMeasurements().values().iterator();
            String subjectName = iteratorM.hasNext() ? iteratorM.next().subject.getSubjectName() : "-"; //first element
            if (pgdevice_attachedSubject != null) pgdevice_attachedSubject.setText(currentPGDevice.getDeviceID());
            if (pgdevice_attachedSubject != null) pgdevice_attachedSubject.setText(
                getApplicationContext().getString(R.string.pgdevice_atteched_subject_caption)
                        + subjectName );*/
        refreshAttachedSubject();
        TextView pgdevice_id = (TextView) findViewById(R.id.pgdevice_id_value);
            if (pgdevice_id != null) pgdevice_id.setText(currentPGDevice.getDeviceID());
        TextView pgdevice_token = (TextView) findViewById(R.id.pgdevice_token_value);
            if (pgdevice_token != null) pgdevice_token.setText(currentPGDevice.getToken());
        TextView pgdevice_serialNum = (TextView) findViewById(R.id.pgdevice_serial_value);
            if (pgdevice_serialNum != null) pgdevice_serialNum.setText(currentPGDevice.getProductionSerialNumber());
        TextView pgdevice_freq = (TextView) findViewById(R.id.pgdevice_freq_value);
            if (pgdevice_freq != null) pgdevice_freq.setText(""+currentPGDevice.getFreq());
        Switch pgdevice_onoff = (Switch) findViewById(R.id.pgdevice_onoff_value);
            if (pgdevice_onoff != null) pgdevice_onoff.setChecked(currentPGDevice.isDeviceOn());
        TextView pgdevice_outerType = (TextView) findViewById(R.id.pgdevice_outerType_value);
            if (pgdevice_outerType != null) pgdevice_outerType.setText(currentPGDevice.getPgdeviceType().getDeviceOuterType());
        TextView pgdevice_innerType = (TextView) findViewById(R.id.pgdevice_innerType_value);
            if (pgdevice_innerType != null) pgdevice_innerType.setText(currentPGDevice.getPgdeviceType().getDeviceInnerType());
        TextView pgdevice_innerType_category = (TextView) findViewById(R.id.pgdevice_innerType_category_value);
            if (pgdevice_innerType_category != null) pgdevice_innerType_category.setText(currentPGDevice.getPgdeviceType().getDeviceInnerType_category());
        TextView pgdevice_manufacturer = (TextView) findViewById(R.id.pgdevice_manufacturer_value);
            if (pgdevice_manufacturer != null) pgdevice_manufacturer.setText(currentPGDevice.getPgdeviceType().getManufacturer());
        TextView pgdevice_productModel = (TextView) findViewById(R.id.pgdevice_productModel_value);
            if (pgdevice_productModel != null) pgdevice_productModel.setText(currentPGDevice.getPgdeviceType().getDeviceModel());

        ListView listView = (ListView) findViewById(R.id.sensors_lv);
            ArrayList<PGDeviceSensorInfo> sensors = new ArrayList<>();
            for (PhenGenDeviceSensorType pgdsType : currentPGDevice.getPgdeviceType().getSensorTypes()) {
                sensors.add(new PGDeviceSensorInfo(currentPGDevice, pgdsType));
            }
            pgDeviceSensorAdapter = new PGDeviceSensorAdapter(this,
                R.layout.listitem_pgdevice_sensor, sensors);
            listView.setAdapter(pgDeviceSensorAdapter);
            //Solution for listview in scrollview problems:
            // https://medium.com/@skidanolegs/listview-inside-scrollview-solve-the-problem-a06fdff2a4e0
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            int listViewTotalHeight = 0;
            View view = null;
            for (int i = 0; i < pgDeviceSensorAdapter.getCount(); i++) {
                view = pgDeviceSensorAdapter.getView(i, view, listView);
                if (i == 0) view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
                view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                listViewTotalHeight += view.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = listViewTotalHeight + (listView.getDividerHeight() * (pgDeviceSensorAdapter.getCount()-1));
            listView.setLayoutParams(params);
            listView.requestLayout();

        TextView pgdevice_deviceGroup = (TextView) findViewById(R.id.pgdevice_group_value);
        if (pgdevice_deviceGroup != null) pgdevice_deviceGroup.setText(
                currentPGDevice.getDeviceGroup() == null ? "-"
                        : currentPGDevice.getDeviceGroup().getBaseDevice().getDeviceID());

        TextView pgdevice_organizationID = (TextView) findViewById(R.id.pgdevice_organizationID_value);
            if (pgdevice_organizationID != null) pgdevice_organizationID.setText(currentPGDevice.getOrganizationID());
        TextView pgdevice_typeID = (TextView) findViewById(R.id.pgdevice_typeID_value);
            if (pgdevice_typeID != null) pgdevice_typeID.setText(currentPGDevice.getTypeID());
        TextView pgdevice_type = (TextView) findViewById(R.id.pgdevice_type_value);
            if (pgdevice_type != null) pgdevice_type.setText(currentPGDevice.getType());


        ((Button) findViewById(R.id.pgdevice_attach_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAction(REATTACH_REQ_CODE);
            }
        });
        ((Button) findViewById(R.id.pgdevice_unattach_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAction(DETACH_REQ_CODE);
            }
        });
        ((Button) findViewById(R.id.pgdevice_remove_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAction(REMOVE_REQ_CODE);
            }
        });


    }//onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pgdevice_settings, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                boolean success = saveChanges();
                if (! success) return false;

                //on creation of new population
                if (currentPGDeviceKey == PhenGenMain.NEW_ELEMENT) {
                    if (newDeviceType == null) {
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                        break;
                    }
                    PhenGenMain.getPhenGenMain().registratePGDevice(currentPGDevice);
                }

                setResult(Activity.RESULT_OK);
                finish();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    /**
     *
     * @return Indicates whether saving was successful or not.
     */
    private boolean saveChanges() {
        EditText deviceId_view = (EditText) findViewById(R.id.pgdevice_id_value);
        EditText password_view = (EditText) findViewById(R.id.pgdevice_token_value);
        EditText serialValue_view = (EditText) findViewById(R.id.pgdevice_serial_value);
        EditText freqStr_view = (EditText) findViewById(R.id.pgdevice_freq_value);

        String deviceId = deviceId_view.getText().toString();
        String password = password_view.getText().toString();
        String serialValue = serialValue_view.getText().toString();
        String freqStr = freqStr_view.getText().toString();

        if (TextUtils.isEmpty(deviceId)) {
            deviceId_view.setError(getString(R.string.pgdevice_settings_must_be_filled));
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            password_view.setError(getString(R.string.pgdevice_settings_must_be_filled));
            return false;
        }
        if (TextUtils.isEmpty(serialValue)) {
            serialValue_view.setError(getString(R.string.pgdevice_settings_must_be_filled));
            return false;
        }
        if (TextUtils.isEmpty(freqStr)) {
            freqStr_view.setError(getString(R.string.pgdevice_settings_must_be_filled));
            return false;
        }

        currentPGDevice.setDeviceID(deviceId);
        currentPGDevice.setPassword(password);
        currentPGDevice.setProductSerialValue(serialValue);
        double freq = Double.parseDouble(freqStr);
        currentPGDevice.setFreq(freq);

        return true;
    }

    private void handleAction(int request_code) {
        switch (request_code) {
            case REATTACH_REQ_CODE:
                saveChanges();
                attachDevice();
                break;
            case DETACH_REQ_CODE:
                saveChanges();
                detachDevice();
                break;
            case REMOVE_REQ_CODE:
                currentSimulation.deleteDevice(currentPGDevice);
                finish();
                break;
        }
    } //handleAction

    private void attachDevice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pgdevices_attach_device_choose_subject_title);

        ArrayAdapter<Subject> adp = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, currentSimulation.getSubjects());

        final Spinner sp = new Spinner(this);
        sp.setAdapter(adp);
        builder.setView(sp);
        builder.setPositiveButton(R.string.ok_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (sp.getSelectedItem() != null &&
                        currentSimulation.attachDevice(currentPGDevice, (Subject) sp.getSelectedItem()) ) {
                    refreshAttachedSubject();
                    Toast.makeText(getApplicationContext(),
                            "1 " + getString(R.string.pgdevices_attach_success_at_devices),
                            Toast.LENGTH_SHORT)
                            .show();

                }
            }
        });
        builder.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void detachDevice() {
        if ( !currentPGDevice.getAttachedMeasurements().isEmpty()
            && currentSimulation.detachDevice(currentPGDevice)) {
            refreshAttachedSubject();
            Toast.makeText(getApplicationContext(),
                    "1 " + getString(R.string.pgdevices_detach_success_at_devices),
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void refreshAttachedSubject() {
        TextView pgdevice_attachedSubject = (TextView) findViewById(R.id.attachedSubject_name);
        Iterator<Measurement> iteratorM = currentPGDevice.getAttachedMeasurements().values().iterator();
        String subjectName = iteratorM.hasNext() ? iteratorM.next().subject.getSubjectName() : "-"; //first element
        if (pgdevice_attachedSubject != null) pgdevice_attachedSubject.setText(currentPGDevice.getDeviceID());
        if (pgdevice_attachedSubject != null) pgdevice_attachedSubject.setText(
                getApplicationContext().getString(R.string.pgdevice_atteched_subject_caption)
                        + subjectName );
    }
}
