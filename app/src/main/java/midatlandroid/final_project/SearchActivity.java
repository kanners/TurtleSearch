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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
                        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                        intent.setClass(SearchActivity.this, MainActivity.class);
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra("Search", "from SearchActivity");
                        SearchActivity.this.startActivity(intent);
                        break;
//                    case 4:
//                        // Start FragementSettings from MainActivity
//                        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
//                        intent.setClass(SearchActivity.this, MainActivity.class);
//                        intent.setAction(Intent.ACTION_SEND);
//                        intent.putExtra("Search", "from SearchActivity");
//                        SearchActivity.this.startActivity(intent);
//                        break;
                }
            }
        });


        handleIntent(getIntent());

        List<ProductListing> list = new ArrayList();
        ListingResults listingResults = new ListingResults();

        // Get the intent, verify action, and perform query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            list = listingResults.getResults(query);
            Toast toast = Toast.makeText(getApplicationContext(), "results got", Toast.LENGTH_LONG);
            toast.show();
        }


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

            String path = "/data/data/" + getPackageName() + "/turtle_search.db";
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path, null);

            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast toast;

            //EditText field = (EditText) findViewById(R.id.queryField);
            String input = query;
            //String input = field.getText().toString().trim();

            if (input.equals("")) {
                toast = Toast.makeText(context, "Nothing", duration);
                toast.show();
                return;
            }

            ListingResults listingResults = new ListingResults();
            ArrayList<ProductListing> listings = listingResults.getResults(input);

            db.execSQL("DROP TABLE Results;");
            db.execSQL("CREATE TABLE IF NOT EXISTS Results(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, price DECIMAL, retailer TEXT, url TEXT);");

            ProductListing[] list = listings.toArray(new ProductListing[0]);

            for (ProductListing pl : list) {
                db.execSQL(String.format("INSERT INTO Results (name, price, retailer, url) VALUES (\"%s\", %f, \"%s\", \"%s\");", pl.name, pl.price, pl.retailer, pl.url));
            }

            String[] cols = {"name","price", "retailer", "url"};
            Cursor cursor = db.query("Results", cols, null, null, null, null, null);

            cursor.moveToFirst();
            List<ListSearchItem> listView = new ArrayList<>();

            // Gather info
            String dbName = "", dbRet = "", dbURL = "";
            double dbPrice = 0.;
            while (cursor.moveToNext()) {
                dbName = cursor.getString(cursor.getColumnIndex("name"));
                dbPrice = cursor.getDouble(cursor.getColumnIndex("price"));
                dbRet = cursor.getString(cursor.getColumnIndex("retailer"));
                ListSearchItem item = new ListSearchItem();
                item.name = dbName;
                item.price = dbPrice;
                item.retailer = dbRet;
                listView.add(item);
            }

            ListItemAdapter adapter;
            adapter = new ListItemAdapter(this, 0, listView);
            ListView listV = (ListView) findViewById(R.id.ListView);
            listV.setAdapter(adapter);

            db.close();

        } if (Intent.ACTION_SEND.equals(intent.getAction())) {
            intent.getExtras();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }
}
