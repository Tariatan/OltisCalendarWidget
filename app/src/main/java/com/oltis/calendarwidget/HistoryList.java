package com.oltis.calendarwidget;

import java.io.File;
import java.util.Calendar;

import android.app.ListActivity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class HistoryList extends ListActivity 
{
	@Override
	public void onCreate(Bundle icicle) 
	{
	    super.onCreate(icicle);

		String DB_PATH = getExternalFilesDir(null) + "/friends.db";

		File file = new File(DB_PATH);
		if(!file.exists())
		{
			return;
		}

	    setContentView(R.layout.historylist);
		try(SQLiteDatabase db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY))
		{
			try(Cursor cursor = db.rawQuery("SELECT * FROM friends;", null))
			{
				if((cursor != null) && cursor.moveToFirst())
				{
					final int len = cursor.getCount();
					String items[] = new String[len];

					String month;
					String day;

					Calendar date = Calendar.getInstance();
					int index = 0;
					for(int i = len - 1; i >= 0; --i)
					{
						cursor.moveToPosition(i);
						date.set(cursor.getInt(1), cursor.getInt(2), cursor.getInt(3));
						long prevMS = date.getTimeInMillis();

						String item = 	String.valueOf(cursor.getInt(1));
						month = String.valueOf(cursor.getInt(2) + 1);	// increase month to start from 1
						if(month.length() == 1)
						{
							month = "0" + month;
						}
						item += "-" + month;
						day = String.valueOf(cursor.getInt(3));
						if(day.length() == 1)
						{
							day = "0" + day;
						}
						item += "-" + day;

						if((i - 1) >= 0)
						{
							cursor.moveToPosition(i - 1);
							date.set(cursor.getInt(1), cursor.getInt(2), cursor.getInt(3));
							long curMS = date.getTimeInMillis();

							if((prevMS > 0) && (curMS > 0))
							{
								item += "\t\t\t" + ((prevMS - curMS) / 1000 / 24 / 60 / 60) + " days";
							}

						}
						items[index++] = item;
					}
					cursor.close();

					setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
				}
			}
			catch(SQLiteException e)
			{
				// Failed to open database
			}
		}
		catch(SQLiteException e)
		{
			// Failed to open database
		}
	}
}
