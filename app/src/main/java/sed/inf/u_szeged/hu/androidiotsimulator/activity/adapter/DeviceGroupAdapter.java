package sed.inf.u_szeged.hu.androidiotsimulator.activity.adapter;

import android.content.Context;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import sed.inf.u_szeged.hu.androidiotsimulator.MobIoTApplication;
import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.activity.device.DevicesActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.DeviceGroup;

/**
 * Created by Tomi on 2016. 01. 21..
 */
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
            final Button startBtn = (Button) v.findViewById(R.id.start_btn);
            final Button editBtn = (Button) v.findViewById(R.id.edit_btn);
            final Button deleteBtn = (Button) v.findViewById(R.id.delete_btn);

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


            if (Objects.equals(deviceGroup.getBaseDevice().getType(), "Thermostat")) {
                v.findViewById(R.id.swtich).setVisibility(View.VISIBLE);
                if (deviceGroup.getDevicesList().get(0).isOn()) {
                    ((ImageView) v.findViewById(R.id.swtich)).setImageResource(R.drawable.ic_on_circle);
                } else {
                    ((ImageView) v.findViewById(R.id.swtich)).setImageResource(R.drawable.ic_off_circle);
                }
            } else {
                v.findViewById(R.id.swtich).setVisibility(View.GONE);
            }


            if (startBtn != null) {
                boolean runs = deviceGroup.isRunning();
                if (runs) {
                    startBtn.setText(getContext().getResources().getText(R.string.stop_btn));
                    if (editBtn != null) {
                        editBtn.setEnabled(false);
                    }
                } else {
                    startBtn.setText(getContext().getResources().getText(R.string.start_btn));
                    editBtn.setEnabled(true);
                }

                startBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (deviceGroup.isRunning()) {
                            deviceGroup.stopDevices(getContext());
                            startBtn.setText(getContext().getResources().getText(R.string.start_btn));
                            assert editBtn != null;
                            editBtn.setEnabled(true);
                        } else {
                            deviceGroup.startDevices();
                            startBtn.setText(getContext().getResources().getText(R.string.stop_btn));
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

                        if (MobIoTApplication.getActivity() instanceof DevicesActivity) {
                            DevicesActivity devicesActivity = (DevicesActivity) MobIoTApplication.getActivity();
                            Message message = new Message();
                            message.what = DevicesActivity.MSG_W_EDIT;
                            message.arg1 = position;
                            message.setTarget(devicesActivity.handler);

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

                        if (MobIoTApplication.getActivity() instanceof DevicesActivity) {
                            DevicesActivity devicesActivity = (DevicesActivity) MobIoTApplication.getActivity();
                            Message message = new Message();
                            message.what = DevicesActivity.MSG_W_DELETE;
                            message.arg1 = position;
                            message.setTarget(devicesActivity.handler);

                            message.sendToTarget();
                        }
                    }
                });
            }

        }

        return v;
    }


}
