package midatlandroid.final_project;


import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.app.Fragment;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHistory extends Fragment {


    public FragmentHistory() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       /* final View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        // Open the database
        String path = "/data/data/" + getActivity().getPackageName() + "/turtle_search.db";
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path, null);

        // Close database
        db.close();

        List<ListSearchItem> list = new ArrayList<>();

        // TODO: store recent search
        // database or arraylist to store recent searches and date accessed
        // display most recent search first
        // on click takes user to that search

        ListItemAdapter adapter;
        adapter = new ListItemAdapter(getActivity(), 0, list);

        ListView listView = (ListView) getActivity().findViewById(R.id.ListView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: take user to search
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                //put extra item name
                startActivity(intent);
            }
            });

        return rootView; */
       return null;
    }

}
