package midatlandroid.final_project;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SearchActivity2 extends AppCompatActivity {

    private View view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search2);

        Button submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = "/data/data/" + getPackageName() + "/turtle_search.db";
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path, null);

                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast toast;

                EditText field = (EditText) findViewById(R.id.queryField);
                String input = field.getText().toString().trim();

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
                db.close();
            }
        });

    }

}
