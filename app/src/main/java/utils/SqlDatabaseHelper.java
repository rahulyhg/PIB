package utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by bunny on 03/10/17.
 */

public class SqlDatabaseHelper extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "NewsManager";

    // Contacts table name
    private static final String TABLE_READ_FEEDS = "readfeeds";
    private static final String TABLE_SAVED_FEED = "savedfeeds";


    // Contacts Table Columns names
    private static final String KEY_RELID = "relid";
    private static final String KEY_LINK = "link";
    private static final String KEY_HEADING = "heading";
    private static final String KEY_SUB_HEADING = "subheading";
    private static final String KEY_DATE = "date";
    private static final String KEY_FLAG = "flag";
    private static final String KEY_HTML_STRING = "htmlString";


    public SqlDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATEREAD_FEED = "CREATE TABLE " + TABLE_READ_FEEDS + "("
                + KEY_RELID + " INTEGER PRIMARY KEY,"
                + KEY_LINK + " TEXT,"
                + KEY_HEADING + " TEXT" +
                ")";


        String CREATESAVED_FEED = "CREATE TABLE " + TABLE_SAVED_FEED + "("
                + KEY_RELID + " INTEGER PRIMARY KEY,"
                + KEY_LINK + " TEXT,"
                + KEY_HEADING + " TEXT,"
                + KEY_SUB_HEADING + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_HTML_STRING + " TEXT" +
                ")";


        db.execSQL(CREATEREAD_FEED);
        db.execSQL(CREATESAVED_FEED);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_READ_FEEDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_FEED);

        // Create tables again
        onCreate(db);
    }

    // Adding new Feed
    public void addReadNews(News news) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LINK, news.getLink());
        values.put(KEY_HEADING, news.getTitle());

        Uri uri = Uri.parse(news.getLink());
        String relID = uri.getQueryParameter("PRID");

        if (relID == null) {
            relID = uri.getQueryParameter("NoteId");
        } else if (relID.isEmpty()) {
            relID = uri.getQueryParameter("NoteId");
        }

        if (relID == null) {
            relID = news.getDescription();
        }

        values.put(KEY_RELID, relID);

        try {
            // Inserting Row
            db.insert(TABLE_READ_FEEDS, null, values);
            db.close(); // Closing database connection

        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    public boolean getNewsReadStatus(News news) {

        String link = news.getLink();

        SQLiteDatabase db = this.getReadableDatabase();
        Uri uri = Uri.parse(link);
        String relID = uri.getQueryParameter("PRID");

        if (relID == null) {
            relID = uri.getQueryParameter("NoteId");
        } else if (relID.isEmpty()) {
            relID = uri.getQueryParameter("NoteId");
        }

        if (relID == null) {
            relID = news.getDescription();
        }

        try {
            Cursor cursor = db.query(TABLE_READ_FEEDS, new String[]{KEY_RELID,
                            KEY_LINK}, KEY_RELID + "=?",
                    new String[]{relID}, null, null, null, null);

            if (cursor == null) {

                return false;
            } else {

                if (cursor.moveToFirst()) {
                    cursor.close();
                    return true;
                } else {
                    cursor.close();
                    return false;
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean getNewsBookMarkStatus(News news) {
        String link = news.getLink();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Uri uri = Uri.parse(link);
            String relID = uri.getQueryParameter("PRID");

            if (relID == null) {
                relID = uri.getQueryParameter("NoteId");
            } else if (relID.isEmpty()) {
                relID = uri.getQueryParameter("NoteId");
            }

            if (relID == null) {
                relID = news.getDescription();
            }


            Cursor cursor = db.query(TABLE_SAVED_FEED, new String[]{KEY_RELID,
                            KEY_LINK}, KEY_RELID + "=?",
                    new String[]{relID}, null, null, null, null);

            if (cursor == null) {

                return false;
            } else {

                if (cursor.moveToFirst()) {
                    cursor.close();
                    return true;
                } else {
                    cursor.close();
                    return false;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }

    public void addSavedNews(News news, String response) {

        if (getNewsBookMarkStatus(news)) {

            deleteSavedNote(news);
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(KEY_LINK, news.getLink());
        values.put(KEY_HEADING, news.getTitle());

        Uri uri = Uri.parse(news.getLink());
        String relID = uri.getQueryParameter("PRID");

        if (relID == null) {
            relID = uri.getQueryParameter("NoteId");
        } else if (relID.isEmpty()) {
            relID = uri.getQueryParameter("NoteId");
        }

        if (relID == null) {
            relID = news.getDescription();
        }

        try {
            values.put(KEY_RELID, relID);

            values.put(KEY_SUB_HEADING, news.getDescription());
            values.put(KEY_DATE, news.getPubDate());

            values.put(KEY_HTML_STRING, response);


            // Inserting Row
            long i = db.insert(TABLE_SAVED_FEED, null, values);
            Log.d("TAG", "addSavedNews: " + i);
            db.close(); // Closing database connection

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void deleteSavedNote(News news) {
        SQLiteDatabase db = this.getWritableDatabase();
        Uri uri = Uri.parse(news.getLink());
        String relID = uri.getQueryParameter("PRID");

        if (relID == null) {
            relID = uri.getQueryParameter("NoteId");
        } else if (relID.isEmpty()) {
            relID = uri.getQueryParameter("NoteId");
        }

        if (relID == null) {
            relID = news.getDescription();
        }

        db.delete(TABLE_SAVED_FEED, KEY_RELID + " =?", new String[]{relID
        });
        db.close();


    }

    public ArrayList<News> getAllSavedNotes() {

        ArrayList<News> newsArrayList = new ArrayList<News>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SAVED_FEED;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                News news = new News();
                news.setLink(cursor.getString(1));
                news.setTitle(cursor.getString(2));
                news.setDescription(cursor.getString(3));
                news.setPubDate(cursor.getString(4));

                // Adding contact to list
                newsArrayList.add(news);
            } while (cursor.moveToNext());


        }

        return newsArrayList;
    }


    public String getFullNews(News news) {

        String link = news.getLink();


        SQLiteDatabase db = this.getReadableDatabase();
        Uri uri = Uri.parse(link);
        String relID = uri.getQueryParameter("PRID");

        if (relID == null) {
            relID = uri.getQueryParameter("NoteId");
        } else if (relID.isEmpty()) {
            relID = uri.getQueryParameter("NoteId");
        }

        if (relID == null) {
            relID = news.getDescription();
        }

        String htmlText = "";

        try {
            Cursor cursor = db.query(TABLE_SAVED_FEED, new String[]{KEY_RELID,
                            KEY_HTML_STRING}, KEY_RELID + "=?",
                    new String[]{relID}, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {

                    htmlText = cursor.getString(1);

                } while (cursor.moveToNext());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return htmlText;
    }

}
