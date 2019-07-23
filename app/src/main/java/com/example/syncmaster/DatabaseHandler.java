package com.example.syncmaster;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "NewsHub";
    private static final String TABLE_NEWS = "News";
    private static final String KEY_SOURCE = "source";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_LASTUPDATED_TIME = "lastupdated";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NEWS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_SOURCE + " TEXT,"
                + KEY_TITLE + " TEXT," + KEY_IMAGE + " TEXT," + KEY_DESCRIPTION + " TEXT,"
                + KEY_LASTUPDATED_TIME + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS);

        // Create tables again
        onCreate(db);
    }

    public void addNews(NewsModel newsModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, newsModel.getTitle()); // News title
        values.put(KEY_SOURCE, newsModel.getNewsSource()); // News Source
        values.put(KEY_IMAGE, newsModel.getImage()); // News Image
        values.put(KEY_DESCRIPTION, newsModel.getDescription()); // News Description
        values.put(KEY_LASTUPDATED_TIME, newsModel.getTime()); // News Updated Time

        // Inserting Row
        db.insert(TABLE_NEWS, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    public List<NewsModel> getAllNews() {
        List<NewsModel> newsModels = new ArrayList<NewsModel>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NEWS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                NewsModel newsModel = new NewsModel();
                newsModel.setId(Integer.parseInt(cursor.getString(0)));
                newsModel.setTitle(cursor.getString(1));
                newsModel.setNewsSource(cursor.getString(2));
                newsModel.setImage(cursor.getString(3));
                newsModel.setDescription(cursor.getString(4));
                newsModel.setTime(cursor.getString(5));
                // Adding contact to list
                newsModels.add(newsModel);
            } while (cursor.moveToNext());
        }

        // return contact list
        return newsModels;
    }

    public String getLastUpdatedNewsTime() throws CursorIndexOutOfBoundsException {
        String selectQuery = "SELECT  * FROM " + TABLE_NEWS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToLast();
        String time = cursor.getString(5);
        cursor.close();
        return time;


    }


    // Getting News Count
    public int getNewsCount() throws CursorIndexOutOfBoundsException {
        String countQuery = "SELECT  * FROM " + TABLE_NEWS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);


        // return count
        int count = cursor.getCount();

        cursor.close();

        return count;
    }


}
