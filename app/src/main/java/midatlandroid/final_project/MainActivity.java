package midatlandroid.final_project;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private RelativeLayout drawerRelativeLayout;
    private ActionBarDrawerToggle drawerToggle;

    String[] drawerOptionLabels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Initialize the database or open it if it exists
        String path = "/data/data/" + getPackageName() + "/turtle_search.db";
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path, null);

        // Add a settings table if it doesn't exist
        db.execSQL("CREATE TABLE IF NOT EXISTS Settings(id INTEGER PRIMARY KEY AUTOINCREMENT, results INTEGER, theme INTEGER);");
        // Check to see if the settings table is empty
        String[] settingsCols = {"results","theme"};
        Cursor cursor = db.query("Settings", settingsCols, null, null, null, null, null);
        if (cursor.getCount() == 0) {
            // If it is empty, add default values
            db.execSQL("INSERT INTO Settings (results, theme) VALUES (15,1);");
        }
        // Gather info
        int dbResults = 0, dbTheme = 0;

        while (cursor.moveToNext()) {
            dbResults = cursor.getInt(cursor.getColumnIndex("results"));
            dbTheme = cursor.getInt(cursor.getColumnIndex("theme"));
        }
        switch (dbTheme) {
            case 1:
                setTheme(R.style.AppTheme);
                break;
            case 0:
                setTheme(R.style.AppThemeDark);
        }


        // Close the database
        db.close();
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListView = (ListView) findViewById(R.id.left_drawer);
        drawerRelativeLayout = (RelativeLayout) findViewById(R.id.left_drawer_layout);

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
               FragmentManager fm = getFragmentManager();
               Fragment fragment = new FragmentSearch();

               switch(position) {
                   case 0:
                       fragment = new FragmentSearch();
                       break;
                   case 1:
                       fragment = new FragmentSettings();
                       break;
               }

               fm.beginTransaction().replace(R.id.content_frame, fragment).commit();
               setTitle(drawerOptionLabels[position]);
               drawerListView.setItemChecked(position, true);
               drawerLayout.closeDrawer(drawerRelativeLayout);
           }
        });

        Intent intent = getIntent();

        // if FragmentSettings called from SearchActivity
        if (intent.hasExtra("Search")) {
            FragmentManager fm = getFragmentManager();
            Fragment fragment = new FragmentSettings();
            fm.beginTransaction().add(R.id.content_frame, fragment).commit();
        }else if (savedInstanceState == null) {
            FragmentManager fm = getFragmentManager();
            Fragment fragment = new FragmentSearch();
            fm.beginTransaction().add(R.id.content_frame, fragment).commit();
            setTitle(drawerOptionLabels[0]);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
