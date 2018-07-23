package com.example.jaypr_000.project_moviedb_p2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;

public class MovieDBDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "movieDBDatabase";  // Name of the database.
    private static final int DB_VERSION = 2;  // The Version of the Database.

    MovieDBDatabaseHelper(Context context)

    {

        super(context, DB_NAME, null, DB_VERSION );

    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {

        updateMyDatabase(db, 0, DB_VERSION);
        System.out.println("I'm in onCreate Method while trying to Create a Database");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

        updateMyDatabase(db, 0, DB_VERSION);
        System.out.println("I'm in onUpgrade Method while trying to Upgrade a Database");

    }

    private static void insertMovie(SQLiteDatabase db, int movieDid, String title )
    {
        ContentValues movieValues = new ContentValues();
        movieValues.put("MOVIEID",movieDid );
        movieValues.put("TITLE",title );
        db.insert("MOVIEDB", null, movieValues);
    }

    private void updateMyDatabase (SQLiteDatabase db, int oldVersion, int newVersion)
    {

        if(oldVersion < 1)
        {

            db.execSQL("CREATE TABLE MOVIEDB (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    +"MOVIEID TEXT,"
                    + "TITLE);");

            insertMovie(db,1 , "Hello Database");
        }

        if(oldVersion < 2)
        {

            // Some code
            db.execSQL("ALTER TABLE MOVIEDB ADD COLUMN FAVORITE NUMERIC;" );
        }

    }



}
