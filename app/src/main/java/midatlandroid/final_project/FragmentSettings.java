package midatlandroid.final_project;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import midatlandroid.final_project.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSettings extends Fragment {


    public FragmentSettings() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        //max value set from database
        //light mode set from database
        String results = "56";
        boolean colorLight = true;

        // Assign the different fields to variables
        TextView max = (TextView) v.findViewById(R.id.resultsPerPage);
        RadioButton light = (RadioButton) v.findViewById(R.id.radioLight);
        RadioButton dark = (RadioButton) v.findViewById(R.id.radioDark);

        // Assign values to the fields from the database
        max.setText(results);
        if (colorLight) {
            light.toggle();
        } else {
            dark.toggle();
        }




        return v;
    }

}
