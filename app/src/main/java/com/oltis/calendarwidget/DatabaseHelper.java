package com.oltis.calendarwidget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static DatabaseHelper sInstance;
    private static String DATABASE_NAME;
    private static final String TABLE_NAME = "friends";
    private static final String KEY_YEAR = "nYear";
    private static final String KEY_MONTH = "nMonth";
    private static final String KEY_DAY = "nDay";
    private static final int DB_SCHEMA = 1;

    public static synchronized DatabaseHelper getInstance(Context context)
    {
        if (sInstance == null)
        {
            File dbDir = new File(context.getExternalFilesDir(null).getPath());
            if(!dbDir.exists())
            {
                dbDir.mkdirs();
                dbDir.setReadable(true);
            }
            DATABASE_NAME = dbDir.getPath() + "/friends.db";

            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DB_SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        try
        {
            db.execSQL("CREATE TABLE IF NOT EXISTS friends(id INTEGER PRIMARY KEY, nYear INTEGER, nMonth INTEGER, nDay INTEGER);");
        }
        catch (Exception e)
        {
            //
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion)
    {
    }

    public void insert(int year, int month, int day)
    {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try
        {
            ContentValues values = new ContentValues();
            values.put(KEY_YEAR, year);
            values.put(KEY_MONTH, month);
            values.put(KEY_DAY, day);

            db.insertOrThrow(TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        }
        catch (Exception e)
        {
        }
        finally
        {
            db.endTransaction();
        }
    }

    public void delete(Integer year, Integer month, Integer day)
    {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = "nYear =? AND nMonth =? AND nDay =?";
        String whereArgs[] = {year.toString(), month.toString(), day.toString()};

        db.delete(TABLE_NAME, whereClause, whereArgs);
    }

    public int[] getDaysForDate(int year, int month)
    {
        int days[] = {0, 0};

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        String selectQuery = "SELECT nDay FROM friends where nYear = " + year + " and nMonth = " + month;
        try(Cursor cursor = db.rawQuery(selectQuery, null))
        {
            try
            {
                if ((cursor != null) && cursor.moveToFirst())
                {
                    days[0] = cursor.getInt(0);
                    if (cursor.moveToNext())
                    {
                        days[1] = cursor.getInt(0);
                    }
                    db.setTransactionSuccessful();
                }
            }
            finally
            {
            }
        }
        catch (Exception e)
        {
        }
        finally
        {
            db.endTransaction();
        }

        return days;
    }
}