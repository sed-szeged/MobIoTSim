package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.pgdevice;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Measurement;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenDevice;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenDeviceType;

public class PGDeviceSelectTypeAdapter extends BaseAdapter {
    private Context context;
    private List<PhenGenDeviceType> items;
    LayoutInflater inflater;

    public PGDeviceSelectTypeAdapter(Context context, int resource, List<PhenGenDeviceType> items) {
        //super(context, resource, items);
        this.items = items;
        this.context = context;
        inflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Nullable
    @Override
    public PhenGenDeviceType getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            //LayoutInflater vi;
            //vi = LayoutInflater.from(getContext());
            v = inflater.inflate(R.layout.listitem_pgdevice_newdevice_type, null);
        }

        //final PhenGenDeviceType pgdeviceType = getItem(position);

        //if (pgdeviceType != null) {
            TextView deviceTypeName = (TextView) v.findViewById(R.id.pgdevice_newdevice_typeName);
            if (deviceTypeName != null) deviceTypeName.setText(items.get(position).getDeviceInnerType());

            TextView deviceCategoryName = (TextView) v.findViewById(R.id.pgdevice_newdevice_typeCategory);
            if (deviceCategoryName != null) deviceCategoryName.setText(items.get(position).getDeviceInnerType_category());

        //}

        return v;
    }
}
