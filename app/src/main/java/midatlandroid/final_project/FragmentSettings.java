package midatlandroid.final_project;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

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
        final View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Open the database
        String path = "/data/data/" + getActivity().getPackageName() + "/turtle_search.db";
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path, null);

        // Gather existing information from the database
        String[] settingsCols = {"results","theme"};
        Cursor cursor = db.query("Settings", settingsCols, null, null, null, null, null);
        int dbResults = 0, dbTheme = 0;
        while (cursor.moveToNext()) {
            dbResults = cursor.getInt(cursor.getColumnIndex("results"));
            dbTheme = cursor.getInt(cursor.getColumnIndex("theme"));
        }

        // Convert the database settings for the program to display
        boolean colorLight = (dbTheme == 1) ? true : false;
        String results = Integer.toString(dbResults);

        // Assign the different fields to variables
        TextView max = (TextView) view.findViewById(R.id.resultsPerPage);
        RadioButton light = (RadioButton) view.findViewById(R.id.radioLight);
        RadioButton dark = (RadioButton) view.findViewById(R.id.radioDark);

        // Assign values to the fields from the database
        max.setText(results);
        if (colorLight) {
            light.toggle();
        } else {
            dark.toggle();
        }

        // Apply button
        Button apply = (Button) view.findViewById(R.id.settingsApply);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast info
                Context context = getActivity().getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast toast;

                TextView results = (TextView) view.findViewById(R.id.resultsPerPage);
                RadioButton light = (RadioButton) view.findViewById(R.id.radioLight);
                int themeToApply = (light.isEnabled()) ? 1 : 0;

                String resultsText = results.getText().toString().trim();

                if (resultsText.equals("")) {
                    toast = Toast.makeText(context, "Results cannot be empty", duration);
                    toast.show();
                    return;
                }

                int resultsToApply = Integer.parseInt(resultsText);

                if (resultsToApply < 15 || resultsToApply > 100) {
                    toast = Toast.makeText(context, "Invalid range for results", duration);
                    toast.show();
                    return;
                }

                // Database opening, updating and closing
                String path = "/data/data/" + getActivity().getPackageName() + "/turtle_search.db";
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path, null);
                String sqlUpdate = String.format("UPDATE Settings SET theme = %d, results = %d where id == 1;", themeToApply, resultsToApply);
                db.execSQL(sqlUpdate);
                db.close();

                toast = Toast.makeText(context, "Settings applied", duration);
                toast.show();
            }
        });

        // Close the database
        db.close();
        return view;
    }

}
