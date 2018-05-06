package com.apptronix.alfred.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.apptronix.alfred.data.DBContract.ControlsEntry;
import com.apptronix.alfred.data.DBContract.ControlScheduleEntry;

/**
 * Created by DevOpsTrends on 12/8/2016.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "alfred.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_CONTROLS_TABLE = "CREATE TABLE " + ControlsEntry.TABLE_NAME + " (" +
                ControlsEntry._ID + " INTEGER PRIMARY KEY, " +
                ControlsEntry.COLUMN_CONTROL_NAME + " TEXT NOT NULL, " +
                ControlsEntry.COLUMN_CONTROL_STATUS + " INTEGER, " +
                ControlsEntry.COLUMN_CONTROL_GROUP + " TEXT, " +
                ControlsEntry.COLUMN_CONTROL_TYPE + " TEXT NOT NULL, " +
                ControlsEntry.COLUMN_MQTT_TOPIC + " TEXT NOT NULL, UNIQUE " +
                "("+ ControlsEntry.COLUMN_MQTT_TOPIC + "))";

        final String SQL_CREATE_CONTROL_SCHEDULE_TABLE = "CREATE TABLE " + ControlScheduleEntry.TABLE_NAME + " (" +
                ControlScheduleEntry._ID + " INTEGER PRIMARY KEY, " +
                ControlScheduleEntry.COLUMN_CONTROL_NAME + " TEXT NOT NULL, " +
                ControlScheduleEntry.COLUMN_SCHEDULED_TIME + " TEXT NOT NULL, " +
                ControlScheduleEntry.COLUMN_SCHEDULED_STATE + " INTEGER NOT NULL, UNIQUE " +
                "("+ ControlsEntry.COLUMN_CONTROL_NAME + "))";

        db.execSQL(SQL_CREATE_CONTROL_SCHEDULE_TABLE);
        db.execSQL(SQL_CREATE_CONTROLS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + ControlsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ControlScheduleEntry.TABLE_NAME);
        onCreate(db);

    }
}
