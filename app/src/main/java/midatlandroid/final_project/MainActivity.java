package midatlandroid.final_project;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
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

        if (savedInstanceState == null) {
            FragmentManager fm = getFragmentManager();
            Fragment fragment = new FragmentSearch();
            fm.beginTransaction().replace(R.id.content_frame, fragment).commit();
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
