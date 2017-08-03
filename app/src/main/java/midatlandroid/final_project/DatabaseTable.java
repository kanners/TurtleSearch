package midatlandroid.final_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by kanners on 7/27/2017.
 */

// Class for virtual table
public class DatabaseTable {

    private static final String TAG = "ProductDatabase";

    // Columns in the product result database table
    public static final String COL_NAME = "NAME";
    public static final String COL_RETAILER = "RETAILER";
    public static final String COL_PRICE = "PRICE";
    public static final String COL_RATING = "RATING";
    public static final String COL_URL = "URL";
    public static final String COL_ID = "ID";

    private static final String DATABASE_NAME = "RESULTS";
    private static final String FTS_VIRTUAL_TABLE = "FTS"; // full text search
    private static final int DATABASE_VERSION = 1;

    private final DatabaseOpenHelper mDatabaseOpenHelper;

    private static String searchItem = "";

    public DatabaseTable (Context context) {
        mDatabaseOpenHelper = new DatabaseOpenHelper(context);
    }

    public Cursor getResults (String query, String[] columns) {
        searchItem = query;
        String selection = COL_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {searchItem+"*"+"*"+"*"+"*"+"*"};

        return query(selection, selectionArgs, columns);
    }

    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), columns,
                selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    // Creates table
    private static class DatabaseOpenHelper extends SQLiteOpenHelper {
        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE" + FTS_VIRTUAL_TABLE + "USING fts3 (" + COL_NAME
                + ", " + COL_RETAILER + ", " + COL_PRICE + ", " + COL_RATING + ", "
                + COL_URL + ", " + COL_ID + ")";

        DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        private void loadResults() {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        loadData();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        // TODO: load data from search results here
        private void loadData() throws IOException {

            ListingResults result = new ListingResults();

            ArrayList <ProductListing> resultList = result.getResults(searchItem);

            for (int i = 0; i < resultList.size(); i++) {
                String name = resultList.get(i).name;
                String retailer = resultList.get(i).retailer;
                double price = resultList.get(i).price;
                double rating = resultList.get(i).rating;
                String url = resultList.get(i).url;
                double id = 0.; id++;
                addData(name, retailer, price, rating, url, id);
            }
        }

        private long addData(String name, String retailer, double price, double rating, String url, double id) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_NAME, name);
            initialValues.put(COL_RETAILER, retailer);
            initialValues.put(COL_PRICE, price);
            initialValues.put(COL_RATING, rating);
            initialValues.put(COL_URL, url);
            initialValues.put(COL_ID, id);

            return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            mDatabase.execSQL(FTS_TABLE_CREATE);
            loadResults();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
            + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }
    }
}
