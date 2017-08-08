package midatlandroid.final_project;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.SearchRecentSuggestions;
import android.speech.RecognizerIntent;
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
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private RelativeLayout drawerRelativeLayout;
    private ActionBarDrawerToggle drawerToggle;
    private String resultsPerSearch;

    String[] drawerOptionLabels;

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
        resultsPerSearch = Integer.toString(dbResults);
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
                        finish();
                        Intent intent;
                        intent = new Intent(SearchActivity.this, MainActivity.class);
                        intent.setClass(SearchActivity.this, MainActivity.class);
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra("Search", "from SearchActivity");
                        SearchActivity.this.startActivity(intent);
                        break;

                }
            }
        });

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
               // ProgressBar spinner = (ProgressBar) findViewById();
                // set visible
                onSearchRequested();
                // set gone
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
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query,null);

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

            db.execSQL("DROP TABLE IF EXISTS Results;");
            db.execSQL("CREATE TABLE IF NOT EXISTS Results(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, price DECIMAL, retailer TEXT, url TEXT);");

            ProductListing[] list = listings.toArray(new ProductListing[0]);

            for (ProductListing pl : list) {
                db.execSQL(String.format("INSERT INTO Results (name, price, retailer, url) VALUES (\"%s\", %f, \"%s\", \"%s\");", pl.name, pl.price, pl.retailer, pl.url));
            }

            String[] cols = {"name","price", "retailer", "url"};
            Cursor cursor = db.query("Results", cols, null, null, null, null, "price", resultsPerSearch);

            cursor.moveToFirst();
            List<ListSearchItem> listView = new ArrayList<>();

            // Gather info
            String dbName = "", dbRet = "", dbURL = "";
            double dbPrice = 0.;

            Bitmap walmartImage;
            walmartImage =
                    BitmapFactory.decodeResource(getResources(), R.mipmap.walmart_listing);
            Bitmap ebayImage;
            ebayImage =
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ebay_listing);

            if (cursor.getCount() != 0) {
                do {
                    dbName = cursor.getString(cursor.getColumnIndex("name"));
                    dbPrice = cursor.getDouble(cursor.getColumnIndex("price"));
                    dbRet = cursor.getString(cursor.getColumnIndex("retailer"));
                    dbURL = cursor.getString(cursor.getColumnIndex("url"));
                    ListSearchItem item = new ListSearchItem();
                    item.name = dbName;
                    item.price = dbPrice;
                    item.retailer = dbRet;
                    if (item.retailer.equals("Walmart")) {
                        item.image = walmartImage;
                    } if (item.retailer.equals("Ebay")) {
                        item.image = ebayImage;
                    }
                    item.url = dbURL;
                    listView.add(item);
                } while (cursor.moveToNext());
            }

            ListItemAdapter adapter;
            adapter = new ListItemAdapter(this, 0, listView);
            ListView listV = (ListView) findViewById(R.id.ListView);
            listV.setAdapter(adapter);


            listV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                   ListSearchItem clicked = new ListSearchItem();
                    clicked = (ListSearchItem) (parent.getItemAtPosition(position));
                    String URL = clicked.url;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(URL));
                    startActivity(i);
                }
            });

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
