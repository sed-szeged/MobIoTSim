package sed.inf.u_szeged.hu.androidiotsimulator.activity.phenomena.subject;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import sed.inf.u_szeged.hu.androidiotsimulator.R;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Population;
import sed.inf.u_szeged.hu.androidiotsimulator.model.phenGenerator.general.Subject;

public class SubjectAdapter extends ArrayAdapter<Subject> {
    private Context context;

    public SubjectAdapter(Context context, int resource, List<Subject> items) {
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
            v = vi.inflate(R.layout.listitem_subject, null);
        }

        final Subject subject = getItem(position);

        if (subject != null) {
            TextView subjectName = (TextView) v.findViewById(R.id.subjectsList_item_name);
            if (subjectName != null) subjectName.setText(subject.getSubjectName());

            TextView quickInfo = (TextView) v.findViewById(R.id.subject_quickInfo);
            if (quickInfo != null) quickInfo.setText(subject.getQuickInfo());

            TextView deviceCount = (TextView) v.findViewById(R.id.subject_deviceCount);
            if (deviceCount != null) deviceCount.setText(
                    context.getString(R.string.subjects_device_count_caption) + " "
                    + subject.getNumberOfAttechedDevices()
            );

        }

        return v;
    }
}
