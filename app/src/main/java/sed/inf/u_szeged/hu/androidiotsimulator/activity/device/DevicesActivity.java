package sed.inf.u_szeged.hu.androidiotsimulator.activity.device;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;

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
import sed.inf.u_szeged.hu.androidiotsimulator.model.gson.device.GsonDevice;

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
        bundle.putString(DeviceSettingsActivity.KEY_SAVE_TRACE, String.valueOf(deviceGroupAdapter.getItem(i).getBaseDevice().isSaveTrace()));

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
                final String path = getRealPathFromURI(uri);
                Toast.makeText(DevicesActivity.this,
                        "Device imported: " + path, Toast.LENGTH_LONG).show();

                System.out.println(MobIoTApplication.getStringFromFile(path));

                String jsonStr = MobIoTApplication.getStringFromFile(path);
                GsonDevice obj = gson.fromJson(jsonStr, GsonDevice.class);

                Device importedBaseDevice = Device.fromJson(obj);
                DeviceGroup deviceGroup = new DeviceGroup(importedBaseDevice, getApplicationContext());
                deviceGroupList.add(deviceGroup);
                deviceGroupAdapter.notifyDataSetChanged();

            } catch (Exception e) {
                System.out.println("DeviceSettingsActivity" + " File select error" + e);
            }
        }

    }

    private String getRealPathFromURI(Uri contentURI) {
        final String docId = DocumentsContract.getDocumentId(contentURI);
        final String[] split = docId.split(":");
        return Environment.getExternalStorageDirectory() + "/" + split[1];
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

            DeviceGroup group = new DeviceGroup(device, getApplicationContext());

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
            DeviceGroup group = new DeviceGroup(device, getApplicationContext());

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
                Integer.parseInt(bundle.getString(DeviceSettingsActivity.KEY_NUM_OF_DEVICES)),
                Boolean.parseBoolean(bundle.getString(DeviceSettingsActivity.KEY_SAVE_TRACE)));
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
                DeviceGroup group = new DeviceGroup(Device.fromSerial(deviceSerial), getApplicationContext());
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
                    "Custom", 1, SensorDataWrapper.sensorDataFromSerial("parameter1+1+30"), "random", 1, true), getApplicationContext());
            deviceGroupList.add(d);
            DeviceGroup d2 = new DeviceGroup(new Device(organizationId, "MobIoTSimType", "MobIoTSimDevice02", "8f3n4rE?rnA-rCF-vR",
                    "Custom", 2, SensorDataWrapper.sensorDataFromSerial("parameter1+10+25"), "random", 1, true), getApplicationContext());
            deviceGroupList.add(d2);

            initDevicesList();
            saveDevicesToPrefs();
        }

    }

    private void initButtons() {

        findViewById(R.id.new_device_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(DevicesActivity.this);
                builder.setTitle("Select source");
                final String[] cloudTypes = {"Create new device", "Import device template"};
                builder.setItems(cloudTypes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                createNewDeviceAction();
                                break;
                            case 1:
                                showFileChooser();
                                break;
                        }

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


        findViewById(R.id.stop_all_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (DeviceGroup deviceGroup : deviceGroupList) {
                    deviceGroup.stopDevices(getApplicationContext());
                    deviceGroupAdapter.notifyDataSetChanged();
                }
            }
        });

        findViewById(R.id.start_all_btn).setOnClickListener(new View.OnClickListener() {
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

        findViewById(R.id.delete_all_btn).setOnClickListener(new View.OnClickListener() {
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

    private class Param {
        String paramName;
        int min;
        int max;

        Param() {
            paramName = "";
            min = 0;
            max = 0;
        }

        Param(String paramName, int min, int max) {
            this.paramName = paramName;
            this.min = min;
            this.max = max;
        }
    }

    private void createNewDeviceAction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DevicesActivity.this);
        builder.setTitle("Select device type");

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
                /*
                String paramName = "";
                int min = 0;
                int max = 0;
                */
                List<Param> param_container = new ArrayList<Param>();
                Resources res = getResources();

                switch (String.valueOf(sp.getSelectedItem())) {
                    case "Bracelet":
                        type = getString(R.string.type_bracelet);

                        param_container.add(new Param(
                                getString(R.string.param_heart_rate_monitor),
                                res.getInteger(R.integer.heart_rate_monitor_min),
                                res.getInteger(R.integer.heart_rate_monitor_max))
                        );
                        param_container.add(new Param(
                                getString(R.string.param_blood_pressure_monitor),
                                res.getInteger(R.integer.blood_pressure_monitor_min),
                                res.getInteger(R.integer.blood_pressure_monitor_max))
                        );
                        break;
                    case "Custom":
                        type = getString(R.string.type_custom);
                        param_container.add(new Param(
                                getString(R.string.param_custom),
                                res.getInteger(R.integer.custom_min),
                                res.getInteger(R.integer.custom_max))
                        );
                        freq = res.getInteger(R.integer.custom_frequency);
                        break;
                    case "Thermostat":
                        type = getString(R.string.type_thermostat);
                        param_container.add(new Param(
                                getString(R.string.param_temperature),
                                res.getInteger(R.integer.temp_min),
                                res.getInteger(R.integer.temp_max))
                        );
                        freq = res.getInteger(R.integer.temp_frequency);
                        break;
                    case "Temperature":
                        type = getString(R.string.type_temperature);
                        param_container.add(new Param(
                                getString(R.string.param_temperature),
                                res.getInteger(R.integer.temp_min),
                                res.getInteger(R.integer.temp_max))
                        );
                        freq = res.getInteger(R.integer.temp_frequency);
                        break;
                    case "Humidity":
                        type = getString(R.string.type_humidity);
                        freq = res.getInteger(R.integer.humidity_frequency);
                        param_container.add(new Param(
                                getString(R.string.param_humidity),
                                res.getInteger(R.integer.humidity_min),
                                res.getInteger(R.integer.humidity_max))
                        );
                        break;
                    case "Weathergroup":
                        type = getString(R.string.type_custom);
                        param_container.add(new Param(
                                getString(R.string.param_custom),
                                res.getInteger(R.integer.custom_min),
                                res.getInteger(R.integer.custom_max))
                        );
                        freq = res.getInteger(R.integer.custom_frequency);
                        break;
                    default:
                        param_container.add(new Param());
                        break;
                    /*
                    case "Custom":
                        type = getString(R.string.type_custom);
                        paramName = getString(R.string.param_custom);
                        min = res.getInteger(R.integer.custom_min);
                        max = res.getInteger(R.integer.custom_max);
                        freq = res.getInteger(R.integer.custom_frequency);
                        break;
                    case "Thermostat":
                        type = getString(R.string.type_thermostat);
                        paramName = getString(R.string.param_temperature);
                        min = res.getInteger(R.integer.temp_min);
                        max = res.getInteger(R.integer.temp_max);
                        freq = res.getInteger(R.integer.temp_frequency);
                        break;
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
                    case "Weathergroup":
                        type = getString(R.string.type_custom);
                        paramName = getString(R.string.param_custom);
                        min = res.getInteger(R.integer.custom_min);
                        max = res.getInteger(R.integer.custom_max);
                        freq = res.getInteger(R.integer.custom_frequency);
                        break;
                        */
                }


                bundle.putString(DeviceSettingsActivity.KEY_TYPE, type);
                bundle.putString(DeviceSettingsActivity.KEY_FREQ, Double.toString(freq));
                String sensorDataSerial = "";
                for (Param p : param_container) {
                    if (!sensorDataSerial.isEmpty()) sensorDataSerial += "*";
                    sensorDataSerial += p.paramName + "+" + p.min + "+" + p.max;
                }
                bundle.putString(DeviceSettingsActivity.KEY_SENSORS, sensorDataSerial);
                //bundle.putString(DeviceSettingsActivity.KEY_SENSORS, paramName + "+" + min + "+" + max);
                bundle.putString(DeviceSettingsActivity.KEY_TRACE_LOCATION, "random");
                bundle.putString(DeviceSettingsActivity.KEY_NUM_OF_DEVICES, "1");
                bundle.putString(DeviceSettingsActivity.KEY_SAVE_TRACE, String.valueOf(true));
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
        devicesLV = findViewById(R.id.devices_lv);
        deviceGroupAdapter = new DeviceGroupAdapter(this, R.layout.device_item, deviceGroupList);
        devicesLV.setAdapter(deviceGroupAdapter);
        deviceGroupAdapter.notifyDataSetChanged();
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, IMPORT_DEVICE_REQ_CODE);
    }

}


