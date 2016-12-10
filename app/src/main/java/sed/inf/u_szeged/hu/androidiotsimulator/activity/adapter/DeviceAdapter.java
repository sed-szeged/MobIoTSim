package sed.inf.u_szeged.hu.androidiotsimulator.activity.adapter;

import android.content.Context;
import android.os.Message;
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
import sed.inf.u_szeged.hu.androidiotsimulator.activity.DevicesActivity;
import sed.inf.u_szeged.hu.androidiotsimulator.model.device.Device;

/**
 * Created by Tomi on 2016. 01. 21..
 */
public class DeviceAdapter extends ArrayAdapter<Device> {

    public DeviceAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public DeviceAdapter(Context context, int resource, List<Device> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.device_item, null);
        }

        final Device d = getItem(position);

        if (d != null) {
            //TextView typeIdTV = (TextView) v.findViewById(R.id.type_id);
            TextView deviceIDTV = (TextView) v.findViewById(R.id.device_id);
            final Button exportBtn = (Button) v.findViewById(R.id.export_btn);
            final Button startBtn = (Button) v.findViewById(R.id.start_btn);
            final Button editBtn = (Button) v.findViewById(R.id.edit_btn);
            final Button deleteBtn = (Button) v.findViewById(R.id.delete_btn);
/*
                if(typeIdTV != null){
                    typeIdTV.setText( d.getTypeID() );
                }
*/
            if (deviceIDTV != null) {
                deviceIDTV.setText(d.getDeviceID());
            }

            if (d.isWarning()) {
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


            if (Objects.equals(d.getType(), "Thermostat")) {
                v.findViewById(R.id.swtich).setVisibility(View.VISIBLE);
                if (d.isOn()) {
                    ((ImageView) v.findViewById(R.id.swtich)).setImageResource(R.drawable.ic_on_circle);
                } else {
                    ((ImageView) v.findViewById(R.id.swtich)).setImageResource(R.drawable.ic_off_circle);
                }
            } else {
                v.findViewById(R.id.swtich).setVisibility(View.GONE);
            }


            if (startBtn != null) {
                boolean runs = d.isRunning();
                if (runs) {
                    startBtn.setText("Stop");
                    if (editBtn != null) {
                        editBtn.setEnabled(false);
                    }
                } else {
                    startBtn.setText("Start");
                    editBtn.setEnabled(true);
                }

                startBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (d.isRunning()) {
                            d.stop();
                            startBtn.setText("Start");
                            editBtn.setEnabled(true);
                        } else {
                            new Thread(d).start();
                            startBtn.setText("Stop");
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
