package sed.inf.u_szeged.hu.androidiotsimulator.activity.device;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
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
import sed.inf.u_szeged.hu.androidiotsimulator.activity.adapter.DeviceAdapter;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.Device;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.SensorDataWrapper;
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.JsonDevice;

public class DevicesActivity extends AppCompatActivity {


    public static final int ADD_DEVICE_SETTINGS_REQ_CODE = 987;
    public static final int EDIT_DEVICE_SETTINGS_REQ_CODE = 654;
    public static final int MSG_W_WARNING = 21;
    public static final int MSG_W_EDIT = 32;
    public static final int MSG_W_DELETE = 42;
    public static final int MSG_W_SWITCH = 52;
    private static final int IMPORT_DEVICE_REQ_CODE = 6384;

    List<Device> devices;
    DeviceAdapter deviceAdapter;
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
        int count = deviceAdapter.getCount();
        for (int i = 0; i < count; i++) {
            Device d = deviceAdapter.getItem(i);
            if (d.getDeviceID().equals(data.get(DeviceSettingsActivity.KEY_DEVICE_ID))) {
                d.setWarning(true);
                break;
            }
        }
        deviceAdapter.notifyDataSetInvalidated();
    }

    private void switchIt() {
        deviceAdapter.notifyDataSetInvalidated();
    }


    private void delete(int position) {
        System.out.println("DELETE " + deviceAdapter.getItem(position));
        devices.remove(position);
        deviceAdapter.notifyDataSetChanged();
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

        bundle.putString(DeviceSettingsActivity.KEY_ORGANIZATION_ID, deviceAdapter.getItem(i).getOrganizationID());
        bundle.putString(DeviceSettingsActivity.KEY_TYPE_ID, deviceAdapter.getItem(i).getTypeID());
        bundle.putString(DeviceSettingsActivity.KEY_DEVICE_ID, deviceAdapter.getItem(i).getDeviceID());
        bundle.putString(DeviceSettingsActivity.KEY_TOKEN, deviceAdapter.getItem(i).getToken());
        bundle.putString(DeviceSettingsActivity.KEY_TYPE, deviceAdapter.getItem(i).getType());
        bundle.putString(DeviceSettingsActivity.KEY_FREQ, String.valueOf(deviceAdapter.getItem(i).getFreq()));
        bundle.putString(DeviceSettingsActivity.KEY_SENSORS, String.valueOf(deviceAdapter.getItem(i).getSensors()));
        bundle.putString(DeviceSettingsActivity.KEY_EDIT_IT, String.valueOf(i));
        bundle.putString(DeviceSettingsActivity.KEY_REPLAY_LOCATION, deviceAdapter.getItem(i).getReplayFileLocation());

        intent.putExtras(bundle);
        startActivityForResult(intent, EDIT_DEVICE_SETTINGS_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO: Fix this
        if (requestCode == ADD_DEVICE_SETTINGS_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    System.out.println("onActivityResult add bundle " + bundle.toString());

                    Device device = getDeviceFromBundle(bundle);
                    System.out.println("Add device: " + device.toString());
                    deviceAdapter.add(device);
                    deviceAdapter.notifyDataSetChanged();
                    saveDevicesToPrefs();
                } else {
                    System.out.println("onActivityResult add bundle NULL");
                }
            }
        }

        if (requestCode == EDIT_DEVICE_SETTINGS_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    System.out.println("onActivityResult edit bundle " + bundle.toString());
                    Device device = getDeviceFromBundle(bundle);
                    Integer position = Integer.parseInt(bundle.getString(DeviceSettingsActivity.KEY_EDIT_IT));

                    if (devices.get(position).equals(device)) {
                        System.out.println("NOT Edited");
                        return;
                    }

                    System.out.println("Edit device from " + position + " : " + deviceAdapter.getItem(position));
                    System.out.println("Edit device to: " + device);
                    devices.add(position, device);
                    devices.remove(position + 1);
                    deviceAdapter.notifyDataSetChanged();
                    saveDevicesToPrefs();
                } else {
                    System.out.println("onActivityResult edit bundle NULL");
                }
            }
        }

        if (requestCode == IMPORT_DEVICE_REQ_CODE) {
            if (resultCode == RESULT_OK) {
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
                        JsonDevice obj = gson.fromJson(jsonStr, JsonDevice.class);

                        Device importedDevice = Device.fromJson(obj);
                        deviceAdapter.add(importedDevice);
                        saveDevicesToPrefs();
                    } catch (Exception e) {
                        System.out.println("DevicesActivity" + " File select error" + e);
                    }
                }
            }

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
                bundle.getString(DeviceSettingsActivity.KEY_REPLAY_LOCATION));
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
            devices = new ArrayList<Device>();
            StringTokenizer st = new StringTokenizer(devicesStr, "<");
            while (st.hasMoreTokens()) {
                String deviceSerial = st.nextToken();
                devices.add(Device.fromSerial(deviceSerial));
            }

            initDevicesList();

        }

        if (devices == null) {
            devices = new ArrayList<>();
        }

        if (devices.size() == 0) {
            System.out.println("empty devices");
            String organizationId = MobIoTApplication.loadData(CloudSettingsActivity.KEY_ORGANIZATION_ID);
            Device d = new Device(organizationId, "MobIoTSimType", "MobIoTSimDevice01", "RFoDC-zKRO_BJ*d+x8",
                    "Custom", 1, SensorDataWrapper.sensorDataFromSerial("parameter1+1+30"), "random");
            System.out.println("MobIoT_test01 json: " + d.getSerial());
            devices.add(d);
            Device d2 = new Device(organizationId, "MobIoTSimType", "MobIoTSimDevice02", "8f3n4rE?rnA-rCF-vR",
                    "Custom", 2, SensorDataWrapper.sensorDataFromSerial("parameter1+10+25"), "random");
            devices.add(d2);

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

                        Toast.makeText(DevicesActivity.this,
                                "OnClickListener : " +
                                        "\nSpinner: " + String.valueOf(sp.getSelectedItem()),
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(DevicesActivity.this, DeviceSettingsActivity.class);

                        Bundle bundle = new Bundle();
                        bundle.putString(DeviceSettingsActivity.KEY_ORGANIZATION_ID, MobIoTApplication.loadData(CloudSettingsActivity.KEY_ORGANIZATION_ID));

                        //TODO from resources
                        String type = "Custom";
                        double freq = 1;
                        int min = 0;
                        int max = 25;

                        switch (String.valueOf(sp.getSelectedItem())) {
                            case "Custom":
                                type = "Custom";
                                freq = 1;
                                min = 0;
                                max = 25;
                                break;
                            case "Temperature":
                                type = "Temperature";
                                freq = 1;
                                min = -25;
                                max = 25;
                                break;
                            case "Humidity":
                                type = "Humidity";
                                freq = 10;
                                min = 25;
                                max = 85;
                                break;
                            case "Thermostat":
                                type = "Thermostat";
                                freq = 1;
                                min = -25;
                                max = 25;
                                break;

                        }


                        bundle.putString(DeviceSettingsActivity.KEY_TYPE, type);
                        bundle.putString(DeviceSettingsActivity.KEY_FREQ, Double.toString(freq));
                        bundle.putString(DeviceSettingsActivity.KEY_SENSORS, "empty+1+10");
                        intent.putExtras(bundle);
                        bundle.putString(DeviceSettingsActivity.KEY_REPLAY_LOCATION, "random");
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
                for (Device device : devices) {
                    device.stop(getApplicationContext());
                    deviceAdapter.notifyDataSetChanged();
                }
            }
        });

        ((Button) findViewById(R.id.start_all_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Device device : devices) {
                    if (!device.isRunning()) {
                        new Thread(device).start();
                    }
                    deviceAdapter.notifyDataSetChanged();
                }
            }
        });

        ((Button) findViewById(R.id.delete_all_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devices.clear();
                deviceAdapter.notifyDataSetChanged();
                saveDevicesToPrefs();
            }
        });
    }

    private void saveDevicesToPrefs() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < deviceAdapter.getCount(); i++) {
            Device d = deviceAdapter.getItem(i);
            sb.append("<");
            sb.append(d.getSerial());
        }
        MobIoTApplication.saveData(MobIoTApplication.KEY_DEVICES, sb.toString());
    }

    public void initDevicesList() {
        devicesLV = (ListView) findViewById(R.id.devices_lv);
        deviceAdapter = new DeviceAdapter(this, R.layout.device_item, devices);
        devicesLV.setAdapter(deviceAdapter);
        deviceAdapter.notifyDataSetChanged();
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


