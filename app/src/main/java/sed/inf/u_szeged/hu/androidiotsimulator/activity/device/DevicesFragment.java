package sed.inf.u_szeged.hu.androidiotsimulator.activity.device;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.adapter.DeviceGroupAdapter;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudFragment;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.cloud.CloudSettingsActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.Device;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.DeviceGroup;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.SensorDataWrapper;

import static sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication.KEY_DEVICES;
import static sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication.loadData;
import static sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication.saveData;
import static sed.inf.u_szeged.hu.androidiotsimulator.activity.main.IoTSimulatorActivity.deviceKeysBuffer;

public class DevicesFragment extends Fragment {

    public static final int ADD_DEVICE_SETTINGS_REQ_CODE = 987;
    public static final int EDIT_DEVICE_SETTINGS_REQ_CODE = 654;
    public static final int MSG_W_WARNING = 21;
    public static final int MSG_W_EDIT = 32;
    public static final int MSG_W_DELETE = 42;
    public static final int MSG_W_SWITCH = 52;
    public static final int MSG_W_CHART = 100;
    public static final int IMPORT_DEVICE_REQ_CODE = 6384;

    public static List<DeviceGroup> deviceGroupList;
    public static DeviceGroupAdapter deviceGroupAdapter;
    public static boolean isAllDevicesStarted = false;

    View view;
    Button startAllBtn;
    ListView devicesLV;
    //Gson gson = new Gson();

    @Override
    public void onPause() {
        super.onPause();
        stopAllDevices();
        flushDeviceKeysToPrefsFromBuffer();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.menu_toolbar_devices, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_devices, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobIoTApplication.setActivity(getActivity()); //nem kell
        initButtons();
        initDevices();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.ic_delete_all_devices:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean allDevicesStopped = false;
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                stopAllDevices();
                                while (!allDevicesStopped) {
                                    allDevicesStopped = true;
                                    for (DeviceGroup group : deviceGroupList) {
                                        if(group.isRunning()){
                                            allDevicesStopped = false;
                                        }else{
                                            group.removeFromCloud();
                                        }
                                    }
                                }
                                deviceKeysBuffer.clear();
                                deviceGroupList.clear();
                                deviceGroupAdapter.notifyDataSetChanged();
                                MobIoTApplication.saveData(MobIoTApplication.KEY_DEVICEKEYS,"");
                                saveDevicesToPrefs();
                                initButtons();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
                builder.setMessage(R.string.delete_all_devices).setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                return false;
            case R.id.devices_toolbar_btn_create_new:
                createNewDeviceAction();
                return false;
            case R.id.devices_toolbar_btn_import:
                showFileChooser();
                return false;
        }
        return false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (CloudFragment.account == null) {
            setHasOptionsMenu(false);
        } else {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    public void initDevices() {
        String devicesStr = "";
        if(CloudFragment.account != null) {
            devicesStr = MobIoTApplication.loadData(MobIoTApplication.KEY_DEVICES + CloudFragment.account.getId());
            System.out.println(MobIoTApplication.KEY_DEVICES + CloudFragment.account.getId());
        }

        /*
        if(MobIoTApplication.loadData(MobIoTApplication.KEY_DEVICES) != null){
            devicesStr = MobIoTApplication.loadData(MobIoTApplication.KEY_DEVICES);
        }
        */

        System.out.println("devicesStr: " + devicesStr);

        if (deviceGroupList == null) {
            deviceGroupList = new ArrayList<>();
        }

        if(CloudFragment.account != null) {
            initDevicesList();
            if (devicesStr != null && devicesStr.length() > 0) {
                System.out.println("elso");
                deviceGroupList = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(devicesStr, "<");
                while (st.hasMoreTokens()) {
                    String deviceSerial = st.nextToken();
                    System.out.println("serial: " + deviceSerial);
                    DeviceGroup group = new DeviceGroup(Device.fromSerial(deviceSerial), getActivity().getApplicationContext()); //getApplicationContext()
                    deviceGroupList.add(group);
                }
                System.out.println("harmadik");
                initDevicesList();
                saveDevicesToPrefs();
            }
        }
    }

    private void stopAllDevices(){
        isAllDevicesStarted = false;
        for(DeviceGroup devGroup: deviceGroupList){
            for(Device dev: devGroup.getDeviceGroup()){
                dev.stop(getContext());
            }
        }
    }
    private void initButtons() {

            startAllBtn = view.findViewById(R.id.start_all_btn);
            startAllBtn.setText(R.string.start_all_devices);
            startAllBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isAllDevicesStarted) {
                        if (deviceGroupList.size() > 0) {
                            for (DeviceGroup deviceGroup : deviceGroupList) {
                                if (!deviceGroup.isRunning()) {
                                    deviceGroup.startDevices();
                                }
                                deviceGroupAdapter.notifyDataSetChanged();
                            }
                            isAllDevicesStarted = true;
                            startAllBtn.setText(R.string.stop_all_devices);
                        } else {
                            Toast.makeText(getActivity(), R.string.no_device_nothing_to_start, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        for (DeviceGroup deviceGroup : deviceGroupList) {
                            if (deviceGroup.isRunning()) {
                                deviceGroup.stopDevices(getActivity().getApplicationContext());
                                deviceGroupAdapter.notifyDataSetChanged();
                            }
                        }
                        isAllDevicesStarted = false;
                        startAllBtn.setText(R.string.start_all_devices);
                    }
                    deviceGroupAdapter.setNotifyOnChange(true);
                }
            });
        if( CloudFragment.account == null ){
            startAllBtn.setVisibility(View.GONE);
        }
    }

    private void createNewDeviceAction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom); //DevicesActivity.this
        builder.setTitle(R.string.select_device_type);

        ArrayAdapter<String> adp = new ArrayAdapter<>(getActivity(), //DevicesActivity.this
                android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.device_types));

        final Spinner sp = new Spinner(getActivity()); //DevicesActivity.this
        sp.setAdapter(adp);
        builder.setView(sp);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(getActivity(), DeviceSettingsActivity.class); //DevicesActivity.this

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
                }
                bundle.putString(DeviceSettingsActivity.KEY_TYPE, type);
                bundle.putString(DeviceSettingsActivity.KEY_FREQ, Double.toString(freq));
                bundle.putString(DeviceSettingsActivity.KEY_SENSORS, paramName + "+" + min + "+" + max);
                bundle.putString(DeviceSettingsActivity.KEY_TRACE_LOCATION, "random");
                bundle.putString(DeviceSettingsActivity.KEY_NUM_OF_DEVICES, "1");
                bundle.putString(DeviceSettingsActivity.KEY_SAVE_TRACE, String.valueOf(true));
                bundle.putString("title", getString(R.string.title_activity_device_settings_add)); //set title of toolbar: Add Device
                intent.putExtras(bundle);

                getActivity().startActivityForResult(intent, ADD_DEVICE_SETTINGS_REQ_CODE);
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

    public static void saveDevicesToPrefs() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < deviceGroupAdapter.getCount(); i++) {
            Device d = deviceGroupAdapter.getItem(i).getBaseDevice();
            sb.append("<");
            sb.append(d.getSerial());
        }
        System.out.println("SAVEDEVICESTOPREFS: " + sb.toString());
        saveData(MobIoTApplication.KEY_DEVICES+CloudFragment.account.getId(), sb.toString());
    }

    public static void flushDeviceKeysToPrefsFromBuffer(){
        StringBuilder sb = new StringBuilder();
        String loadedDevicesKeyRaw = loadData(MobIoTApplication.KEY_DEVICEKEYS);
        for(Map.Entry<String,String> deviceData: deviceKeysBuffer.entrySet()){
            sb.append("<");
            sb.append(deviceData.getKey());
            sb.append("|");
            sb.append(deviceData.getValue());
        }
        if(sb.toString() != "") {
            if (loadedDevicesKeyRaw == null || loadedDevicesKeyRaw.isEmpty()) {
                saveData(MobIoTApplication.KEY_DEVICEKEYS,sb.toString());
            }else{
                loadedDevicesKeyRaw+=sb.toString();
                saveData(MobIoTApplication.KEY_DEVICEKEYS,loadedDevicesKeyRaw);
            }
            deviceKeysBuffer.clear();
        }
    }

    public void initDevicesList() {
        devicesLV = (ListView) view.findViewById(R.id.devices_lv);
        deviceGroupAdapter = new DeviceGroupAdapter(getActivity(), R.layout.device_item, deviceGroupList);
        devicesLV.setAdapter(deviceGroupAdapter);
        deviceGroupAdapter.notifyDataSetChanged();
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        getActivity().startActivityForResult(intent, IMPORT_DEVICE_REQ_CODE);
    }

}
