package sed.inf.u_szeged.hu.androidiotsimulator.activity.device;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.adapter.DeviceGroupAdapter;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.Device;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.DeviceGroup;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.SensorDataWrapper;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.GsonDevice;

public class DevicesActivity extends AppCompatActivity {


    public static final int ADD_DEVICE_SETTINGS_REQ_CODE = 987;
    public static final int EDIT_DEVICE_SETTINGS_REQ_CODE = 654;
    public static final int MSG_W_WARNING = 21;
    public static final int MSG_W_EDIT = 32;
    public static final int MSG_W_DELETE = 42;
    public static final int MSG_W_SWITCH = 52;
    private static final int IMPORT_DEVICE_REQ_CODE = 6384;

    List<DeviceGroup> deviceGroupList;
    DeviceGroupAdapter deviceGroupAdapter;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_W_WARNING:
                    System.out.println("msg warning");
                    warning(msg.getData());
                    break;
                case MSG_W_EDIT:
                    System.out.println("msg edit");
                    edit(msg.arg1);
                    break;
                case MSG_W_DELETE:
                    System.out.println("msg delete");
                    delete(msg.arg1);
                    break;
                case MSG_W_SWITCH:
                    System.out.println("msg switch");
                    switchIt();
                    break;
            }
        }
    };
    ListView devicesLV;
    Gson gson = new Gson();

    private void warning(Bundle data) {
        int count = deviceGroupAdapter.getCount();
        for (int i = 0; i < count; i++) {
            DeviceGroup deviceGroup = deviceGroupAdapter.getItem(i);
            for (Device d : deviceGroup.getDevicesList()) {
                if (d.getDeviceID().equals(data.get(DeviceSettingsActivity.KEY_DEVICE_ID))) {
                    d.setWarning(true);
                    break;
                }
            }
        }
        deviceGroupAdapter.notifyDataSetInvalidated();
    }

    private void switchIt() {
        deviceGroupAdapter.notifyDataSetInvalidated();
    }


    private void delete(int position) {
        System.out.println("DELETE " + deviceGroupAdapter.getItem(position));
        DeviceGroup deviceGroup = deviceGroupList.get(position);

        deviceGroup.removeFromCloud();

        deviceGroupList.remove(position);
        deviceGroupAdapter.notifyDataSetChanged();
        saveDevicesToPrefs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobIoTApplication.setActivity(this);
    }

    public void edit(int i) {
        Intent intent = new Intent(this, DeviceSettingsActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString(DeviceSettingsActivity.KEY_ORGANIZATION_ID, deviceGroupAdapter.getItem(i).getBaseDevice().getOrganizationID());
        bundle.putString(DeviceSettingsActivity.KEY_TYPE_ID, deviceGroupAdapter.getItem(i).getBaseDevice().getTypeID());
        bundle.putString(DeviceSettingsActivity.KEY_DEVICE_ID, deviceGroupAdapter.getItem(i).getBaseDevice().getDeviceID());
        bundle.putString(DeviceSettingsActivity.KEY_TOKEN, deviceGroupAdapter.getItem(i).getBaseDevice().getToken());
        bundle.putString(DeviceSettingsActivity.KEY_TYPE, deviceGroupAdapter.getItem(i).getBaseDevice().getType());
        bundle.putString(DeviceSettingsActivity.KEY_FREQ, String.valueOf(deviceGroupAdapter.getItem(i).getBaseDevice().getFreq()));
        bundle.putString(DeviceSettingsActivity.KEY_SENSORS, String.valueOf(deviceGroupAdapter.getItem(i).getBaseDevice().getSensors()));
        bundle.putString(DeviceSettingsActivity.KEY_EDIT_IT, String.valueOf(i));
        bundle.putString(DeviceSettingsActivity.KEY_TRACE_LOCATION, deviceGroupAdapter.getItem(i).getBaseDevice().getTraceFileLocation());
        bundle.putString(DeviceSettingsActivity.KEY_NUM_OF_DEVICES, String.valueOf(deviceGroupAdapter.getItem(i).getBaseDevice().getNumOfDevices()));

        intent.putExtras(bundle);
        startActivityForResult(intent, EDIT_DEVICE_SETTINGS_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO: Fix this
        if (requestCode == ADD_DEVICE_SETTINGS_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                addDeviceActivityResult(data);
            }
        }

        if (requestCode == EDIT_DEVICE_SETTINGS_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                if (editDeviceActivityResult(data)) return;
            }
        }

        if (requestCode == IMPORT_DEVICE_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                importDeviceActivityResult(data);
            }

        }

    }

    private void importDeviceActivityResult(Intent data) {
        if (data != null) {
            // Get the URI of the selected file
            final Uri uri = data.getData();
            System.out.println("Uri = " + uri.toString());
            try {
                // Get the file path from the URI
                final String path = FileUtils.getPath(this, uri);
                Toast.makeText(DevicesActivity.this,
                        "File Selected: " + path, Toast.LENGTH_LONG).show();

                System.out.println(MobIoTApplication.getStringFromFile(path));

                String jsonStr = MobIoTApplication.getStringFromFile(path);
                GsonDevice obj = gson.fromJson(jsonStr, GsonDevice.class);

                Device importedBaseDevice = Device.fromJson(obj);
                DeviceGroup deviceGroup = new DeviceGroup(importedBaseDevice);
                deviceGroupList.add(deviceGroup);
                deviceGroupAdapter.notifyDataSetChanged();

                saveDevicesToPrefs();
            } catch (Exception e) {
                System.out.println("DevicesActivity" + " File select error" + e);
            }
        }
    }

    private boolean editDeviceActivityResult(Intent data) {
        Bundle bundle = data.getExtras();
        if (bundle != null) {
            System.out.println("onActivityResult edit bundle " + bundle.toString());
            Device device = getDeviceFromBundle(bundle);
            Integer position = Integer.parseInt(bundle.getString(DeviceSettingsActivity.KEY_EDIT_IT));

            if (deviceGroupList.get(position).getBaseDevice().equals(device)) {
                System.out.println("NOT Edited");
                return true;
            }

            deviceGroupList.get(position).removeFromCloud();

            DeviceGroup group = new DeviceGroup(device);

            System.out.println("Edit deviceGroupList from " + position + " : " + deviceGroupAdapter.getItem(position));
            System.out.println("Edit deviceGroupList to: " + device);
            deviceGroupList.add(position, group);

            deviceGroupList.remove(position + 1);
            deviceGroupAdapter.notifyDataSetChanged();
            //saveDeviceToCloud(device);
            saveDevicesToPrefs();
        } else {
            System.out.println("onActivityResult edit bundle NULL");
        }
        return false;
    }

    private void addDeviceActivityResult(Intent data) {
        Bundle bundle = data.getExtras();
        if (bundle != null) {
            System.out.println("onActivityResult add bundle " + bundle.toString());

            Device device = getDeviceFromBundle(bundle);
            DeviceGroup group = new DeviceGroup(device);

            System.out.println("Add deviceGroupList: " + device.toString());
            deviceGroupAdapter.add(group);
            deviceGroupAdapter.notifyDataSetChanged();
            saveDevicesToPrefs();
        } else {
            System.out.println("onActivityResult add bundle NULL");
        }
    }


    @NonNull
    private Device getDeviceFromBundle(Bundle bundle) {
        return new Device(
                bundle.getString(DeviceSettingsActivity.KEY_ORGANIZATION_ID),
                bundle.getString(DeviceSettingsActivity.KEY_TYPE_ID),
                bundle.getString(DeviceSettingsActivity.KEY_DEVICE_ID),
                bundle.getString(DeviceSettingsActivity.KEY_TOKEN),
                bundle.getString(DeviceSettingsActivity.KEY_TYPE),
                Double.parseDouble(bundle.getString(DeviceSettingsActivity.KEY_FREQ)),
                SensorDataWrapper.sensorDataFromSerial(bundle.getString(DeviceSettingsActivity.KEY_SENSORS)),
                bundle.getString(DeviceSettingsActivity.KEY_TRACE_LOCATION),
                Integer.parseInt(bundle.getString(DeviceSettingsActivity.KEY_NUM_OF_DEVICES)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        MobIoTApplication.setActivity(this);

        initButtons();
        initDevices();

    }

    public void initDevices() {


        String devicesStr = MobIoTApplication.loadData(MobIoTApplication.KEY_DEVICES);
        System.out.println("devicesStr: " + devicesStr);

        if (devicesStr != null && !devicesStr.equals("")) {
            deviceGroupList = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(devicesStr, "<");
            while (st.hasMoreTokens()) {
                String deviceSerial = st.nextToken();
                DeviceGroup group = new DeviceGroup(Device.fromSerial(deviceSerial));
                deviceGroupList.add(group);
            }

            initDevicesList();

        }

        if (deviceGroupList == null) {
            deviceGroupList = new ArrayList<>();
        }

        if (deviceGroupList.size() == 0) {
            System.out.println("empty devices");
            String organizationId = MobIoTApplication.loadData(CloudSettingsActivity.KEY_ORGANIZATION_ID);
            DeviceGroup d = new DeviceGroup(new Device(organizationId, "MobIoTSimType", "MobIoTSimDevice01", "RFoDC-zKRO_BJ*d+x8",
                    "Custom", 1, SensorDataWrapper.sensorDataFromSerial("parameter1+1+30"), "random",1));
            deviceGroupList.add(d);
            DeviceGroup d2 = new DeviceGroup(new Device(organizationId, "MobIoTSimType", "MobIoTSimDevice02", "8f3n4rE?rnA-rCF-vR",
                    "Custom", 2, SensorDataWrapper.sensorDataFromSerial("parameter1+10+25"), "random",1));
            deviceGroupList.add(d2);

            initDevicesList();
            saveDevicesToPrefs();
        }

    }

    private void initButtons() {
        findViewById(R.id.add_new_device_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(DevicesActivity.this);
                builder.setTitle("Select a Device Type");

                ArrayAdapter<String> adp = new ArrayAdapter<>(DevicesActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.device_types));

                final Spinner sp = new Spinner(DevicesActivity.this);
                sp.setAdapter(adp);
                builder.setView(sp);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(DevicesActivity.this, DeviceSettingsActivity.class);

                        Bundle bundle = new Bundle();
                        bundle.putString(DeviceSettingsActivity.KEY_ORGANIZATION_ID, MobIoTApplication.loadData(CloudSettingsActivity.KEY_ORGANIZATION_ID));

                        //TODO from resources
                        String type = "Custom";
                        double freq = 1;
                        String paramName = "";
                        int min = 0;
                        int max = 0;
                        Resources res = getResources();

                        switch (String.valueOf(sp.getSelectedItem())) {
                            case "Custom":
                                type = getString(R.string.type_custom);
                                paramName = getString(R.string.param_custom);
                                min = res.getInteger(R.integer.custom_min);
                                max = res.getInteger(R.integer.custom_max);
                                freq = res.getInteger(R.integer.custom_frequency);
                                break;
                            case "Thermostat":
                                type = getString(R.string.type_thermostat);
                            case "Temperature":
                                type = getString(R.string.type_temperature);
                                paramName = getString(R.string.param_temperature);
                                min = res.getInteger(R.integer.temp_min);
                                max = res.getInteger(R.integer.temp_max);
                                freq = res.getInteger(R.integer.temp_frequency);
                                break;
                            case "Humidity":
                                type = getString(R.string.type_humidity);
                                freq = res.getInteger(R.integer.humidity_frequency);
                                paramName = getString(R.string.param_humidity);
                                min = res.getInteger(R.integer.humidity_min);
                                max = res.getInteger(R.integer.humidity_max);
                                break;

                        }


                        bundle.putString(DeviceSettingsActivity.KEY_TYPE, type);
                        bundle.putString(DeviceSettingsActivity.KEY_FREQ, Double.toString(freq));
                        bundle.putString(DeviceSettingsActivity.KEY_SENSORS, paramName + "+" + min + "+" + max);
                        bundle.putString(DeviceSettingsActivity.KEY_TRACE_LOCATION, "random");
                        bundle.putString(DeviceSettingsActivity.KEY_NUM_OF_DEVICES, "1");
                        intent.putExtras(bundle);

                        startActivityForResult(intent, ADD_DEVICE_SETTINGS_REQ_CODE);
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

        ((Button) findViewById(R.id.import_device_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();

            }

        });

        ((Button) findViewById(R.id.stop_all_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (DeviceGroup deviceGroup : deviceGroupList) {
                    deviceGroup.stopDevices(getApplicationContext());
                    deviceGroupAdapter.notifyDataSetChanged();
                }
            }
        });

        ((Button) findViewById(R.id.start_all_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (DeviceGroup deviceGroup : deviceGroupList) {
                    if (!deviceGroup.isRunning()) {
                        deviceGroup.startDevices();
                    }
                    deviceGroupAdapter.notifyDataSetChanged();
                }
            }
        });

        ((Button) findViewById(R.id.delete_all_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (DeviceGroup group : deviceGroupList) {
                    group.removeFromCloud();
                }
                deviceGroupList.clear();
                deviceGroupAdapter.notifyDataSetChanged();
                saveDevicesToPrefs();
            }
        });
    }

    private void saveDevicesToPrefs() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < deviceGroupAdapter.getCount(); i++) {
            Device d = deviceGroupAdapter.getItem(i).getBaseDevice();
            sb.append("<");
            sb.append(d.getSerial());
        }
        MobIoTApplication.saveData(MobIoTApplication.KEY_DEVICES, sb.toString());
    }

    public void initDevicesList() {
        devicesLV = (ListView) findViewById(R.id.devices_lv);
        deviceGroupAdapter = new DeviceGroupAdapter(this, R.layout.device_item, deviceGroupList);
        devicesLV.setAdapter(deviceGroupAdapter);
        deviceGroupAdapter.notifyDataSetChanged();
    }

    private void showFileChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, "choose file");
        try {
            startActivityForResult(intent, IMPORT_DEVICE_REQ_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
            System.out.println("Can't show the file chooser: " + e);
        }
    }

}


