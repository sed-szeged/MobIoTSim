package sed.inf.u_szeged.hu.androidiotsimulator.activity.adapter;

import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.main.IoTSimulatorActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.DeviceGroup;

import static sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesFragment.isAllDevicesStarted;

public class DeviceGroupAdapter extends ArrayAdapter<DeviceGroup> {

    public DeviceGroupAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public DeviceGroupAdapter(Context context, int resource, List<DeviceGroup> items) {
        super(context, resource, items);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.device_item, null);
        }

        final DeviceGroup deviceGroup = getItem(position);
        if (deviceGroup != null) {
            TextView deviceIDTV = (TextView) v.findViewById(R.id.device_id);
            TextView subdevicesTv = (TextView) v.findViewById(R.id.subdevices_tv);
            final ImageButton startBtn = (ImageButton) v.findViewById(R.id.start_device_btn);
            final ImageButton editBtn = (ImageButton) v.findViewById(R.id.edit_device_btn);
            final ImageButton displayChartBtn = (ImageButton) v.findViewById(R.id.chart_btn);
            final ImageButton deleteBtn = (ImageButton) v.findViewById(R.id.delete_device_btn);

            if (deviceIDTV != null) {
                deviceIDTV.setText(deviceGroup.getBaseDevice().getDeviceID());
            }

            if (subdevicesTv != null) {
                subdevicesTv.setText(String.valueOf(deviceGroup.getBaseDevice().getNumOfDevices()));
            }

             if (deviceGroup.isWarning()) {
                v.findViewById(R.id.warning).setVisibility(View.VISIBLE);
                v.findViewById(R.id.warning).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setVisibility(View.GONE);
                    }
                });
            } else {
                v.findViewById(R.id.warning).setVisibility(View.GONE);
            }


            if (Objects.equals(deviceGroup.getBaseDevice().getType(), "Thermostat")) { //TODO:
                v.findViewById(R.id.on_off_container).setVisibility(View.VISIBLE);

                int numOn = deviceGroup.getNumOfOnDevices();
                ((TextView) v.findViewById(R.id.on_devices_tv)).setText("On: " + numOn);

                int numOff = deviceGroup.getDeviceGroup().size() - numOn;
                ((TextView) v.findViewById(R.id.off_devices_tv)).setText("Off: " + numOff);

            } else {
                v.findViewById(R.id.on_off_container).setVisibility(View.GONE);
            }


            if (startBtn != null) {
                boolean runs = deviceGroup.isRunning();
                if (runs || isAllDevicesStarted) {
                    startBtn.setImageResource(R.drawable.ic_btn_stop_device);
                    if (editBtn != null) {
                        editBtn.setEnabled(false);
                    }
                } else {
                    startBtn.setImageResource(R.drawable.ic_btn_start_device);
                    editBtn.setEnabled(true);
                }

                startBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (deviceGroup.isRunning()) {
                            deviceGroup.stopDevices(getContext());
                            startBtn.setImageResource(R.drawable.ic_btn_start_device);
                            assert editBtn != null;
                            editBtn.setEnabled(true);
                        } else {
                            deviceGroup.startDevices();
                            startBtn.setImageResource(R.drawable.ic_btn_stop_device);
                            if (editBtn != null) {
                                editBtn.setEnabled(false);
                            }
                        }
                    }
                });
            }

            if (editBtn != null) {
                editBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("editBtn clicked " + position);
                        if (MobIoTApplication.getActivity() instanceof IoTSimulatorActivity) {
                            IoTSimulatorActivity iotactivity = (IoTSimulatorActivity) MobIoTApplication.getActivity();
                            Message message = new Message();
                            message.what = DevicesFragment.MSG_W_EDIT;
                            message.arg1 = position;
                            message.setTarget(iotactivity.handler);

                            message.sendToTarget();
                        }
                    }
                });
            }

            if (deleteBtn != null) {
                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("deleteBtn clicked " + position);
                        if (MobIoTApplication.getActivity() instanceof IoTSimulatorActivity) {
                        IoTSimulatorActivity iotactivity = (IoTSimulatorActivity) MobIoTApplication.getActivity();
                        Message message = new Message();
                        message.what = DevicesFragment.MSG_W_DELETE;
                        message.arg1 = position;
                        message.setTarget(iotactivity.handler);

                        message.sendToTarget();
                    }
                }
            });
        }
            if (displayChartBtn != null) {
                displayChartBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (MobIoTApplication.getActivity() instanceof IoTSimulatorActivity) {
                            IoTSimulatorActivity iotactivity = (IoTSimulatorActivity) MobIoTApplication.getActivity();
                            Message message = new Message();
                            message.what = DevicesFragment.MSG_W_CHART;
                            message.arg1 = position;
                            message.setTarget(iotactivity.handler);

                            message.sendToTarget();
                        }
                    }
                });
            }
        }
        return v;
    }


}

