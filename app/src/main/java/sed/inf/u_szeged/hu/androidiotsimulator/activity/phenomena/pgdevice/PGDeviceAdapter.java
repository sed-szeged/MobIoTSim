package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.pgdevice;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Measurement;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.PhenGenDevice;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Subject;

public class PGDeviceAdapter extends ArrayAdapter<PhenGenDevice> {
    private Context context;

    public PGDeviceAdapter(Context context, int resource, List<PhenGenDevice> items) {
        super(context, resource, items);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.listitem_pgdevice, null);
        }

        final PhenGenDevice pgdevice = getItem(position);

        if (pgdevice != null) {
            TextView deviceName = (TextView) v.findViewById(R.id.pgdeviceList_item_name);
            if (deviceName != null) deviceName.setText(pgdevice.getDeviceID());

            TextView serialNum = (TextView) v.findViewById(R.id.pgdevice_productSerialNumber);
            if (serialNum != null) serialNum.setText(context.getString(R.string.pgdevice_productSerial_caption)
                    + pgdevice.getProductionSerialNumber());

            TextView deviceType = (TextView) v.findViewById(R.id.pgdevice_type);
            if (deviceType != null) deviceType.setText(pgdevice.getPgdeviceType().getDeviceInnerType());

            TextView attachedSubject = (TextView) v.findViewById(R.id.pgdevice_attachedTo);
            Iterator<Measurement> iteratorM = pgdevice.getAttachedMeasurements().values().iterator();
            String subjectName = iteratorM.hasNext() ? iteratorM.next().subject.getSubjectName() : "-"; //first element
            if (attachedSubject != null) attachedSubject.setText(
                    context.getString(R.string.pgdevice_atteched_subject_caption)
                    + subjectName );

        }

        return v;
    }
}
