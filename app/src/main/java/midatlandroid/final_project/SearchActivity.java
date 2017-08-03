package midatlandroid.final_project;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private RelativeLayout drawerRelativeLayout;
    private ActionBarDrawerToggle drawerToggle;

    String[] drawerOptionLabels;

    DatabaseTable dbResults = new DatabaseTable(this);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Open settings database
        String path = "/data/data/" + getPackageName() + "/turtle_search.db";
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, 0);

        // Gather existing information
        String[] settingsCols = {"results","theme"};
        Cursor cursor = db.query("Settings", settingsCols, null, null, null, null, null);
        int dbResults = 0, dbTheme = 0;
        while (cursor.moveToNext()) {
            dbResults = cursor.getInt(cursor.getColumnIndex("results"));
            dbTheme = cursor.getInt(cursor.getColumnIndex("theme"));
        }
        // Apply theme
        switch (dbTheme) {
            case 1:
                setTheme(R.style.AppTheme);
                break;
            case 0:
                setTheme(R.style.AppThemeDark);
        }
        // Close settings database
        db.close();
        setContentView(R.layout.activity_search);

        drawerLayout = (DrawerLayout) findViewById(R.id.search_drawer_layout);
        drawerListView = (ListView) findViewById(R.id.search_left_drawer);
        drawerRelativeLayout = (RelativeLayout) findViewById(R.id.search_left_drawer_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        Resources resources = getResources();

        drawerOptionLabels = resources.getStringArray(R.array.sliding_drawer_array);
        ArrayAdapter<String> drawerAdapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerOptionLabels);
        drawerListView.setAdapter(drawerAdapter);

        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch(position) {
                    case 0:
                        // end SearchActivity and go to MainActivity
                        finish();
                        break;
                    case 1:
                        // Start FragementSettings from MainActivity
                        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                        intent.setClass(SearchActivity.this, MainActivity.class);
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra("Search", "from SearchActivity");
                        SearchActivity.this.startActivity(intent);
                        break;
                }
            }
        });
        List<ListSearchItem> list = new ArrayList<ListSearchItem>();
        handleIntent(getIntent());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the search menu from searchable XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch(item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        //verify action
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Cursor c = dbResults.getResults(query, null);

        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }
}
