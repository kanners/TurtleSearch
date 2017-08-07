package midatlandroid.final_project;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSearch extends Fragment {

    public FragmentSearch() {}

    /*
    Impossible to implement standard Android search interface with a Fragment
    Reasons:
    1. When creating a searchable interface, must specify default
    searchable activity in manifest. Fragment cannot exist without
    parent Activity, so separation is not possible.
    2. Internal system responsible for providing search results expects
    and Activity not Fragment.
    Documentation states:
    "When the user executes a search in the search dialog or widget,
    the system starts your searchable activity and delivers it the
    search query in an Intent with the ACTION_SEARCH action. Your
    searchable activity retrieves the query from the intent's
    QUERY extra, then searches your data and presents the results."

    Therefore I have linked this fragment with a new activity I have
    created, SearchActivity.
    */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate
        final View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        // Open the database
        String path = "/data/data/" + getActivity().getPackageName() + "/turtle_search.db";
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path, null);

        // Close database
        db.close();

        // click listener for new_search_btn, start SearchActivity
        final Button newSearchBtn = (Button) rootView.findViewById(R.id.new_search_btn);
        newSearchBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }
}
