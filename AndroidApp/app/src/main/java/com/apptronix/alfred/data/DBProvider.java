package com.apptronix.alfred.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.apptronix.alfred.data.DBContract.ControlsEntry;
import com.apptronix.alfred.data.DBContract.ControlScheduleEntry;

import timber.log.Timber;

/**
 * Created by DevOpsTrends on 12/8/2016.
 */

public class DBProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DBHelper mOpenHelper;

    static final int CONTROL_LIST = 100;
    static final int CONTROL_ITEM_SCHEDULE = 101;
    static final int CONTROL_GROUP_NAMES = 103;
    static final int CONTROL_GROUP_LIST = 104;
    static final int CONTROL_TOPIC = 105;

    private static final SQLiteQueryBuilder sStudentQueryBuilder;

    private SQLiteDatabase db;

    static{

        sStudentQueryBuilder = new SQLiteQueryBuilder();

        sStudentQueryBuilder.setTables(ControlScheduleEntry.TABLE_NAME);
        sStudentQueryBuilder.setTables(ControlsEntry.TABLE_NAME);

    }

    private Cursor getControlSchedule(Uri uri) {

        String day = ControlScheduleEntry.getControlName(uri);

        db=mOpenHelper.getReadableDatabase();

        sStudentQueryBuilder.setTables(ControlScheduleEntry.TABLE_NAME);
        return sStudentQueryBuilder.query(db,
                null,
                ControlScheduleEntry.TABLE_NAME +"."+ ControlScheduleEntry.COLUMN_CONTROL_NAME + " = ? ",
                new String[]{day},
                null,
                null,
                null
        );

    }


    private Cursor getControlsList(Uri uri) {
        db=mOpenHelper.getReadableDatabase();

        String group = ControlsEntry.getControlGroupName(uri);
        sStudentQueryBuilder.setTables(ControlsEntry.TABLE_NAME);

        return sStudentQueryBuilder.query(db,
                null,
                ControlsEntry.TABLE_NAME + "." + ControlsEntry.COLUMN_CONTROL_GROUP + " = ? ",
                new String[]{group},
                null,
                null,
                null
        );

    }

    private Cursor getControlTopics(Uri uri) {
        db=mOpenHelper.getReadableDatabase();

        sStudentQueryBuilder.setTables(ControlsEntry.TABLE_NAME);

        return sStudentQueryBuilder.query(db,
                null,
                null,
                null,
                null,
                null,
                null
        );

    }

    private Cursor getControlGroups(Uri uri) {
        db=mOpenHelper.getReadableDatabase();
        String queryString="SELECT DISTINCT("+ControlsEntry.COLUMN_CONTROL_GROUP+") FROM " + ControlsEntry.TABLE_NAME;
        return db.rawQuery(queryString, null);

    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DBContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, DBContract.PATH_CONTROLS, CONTROL_LIST);
        matcher.addURI(authority, DBContract.PATH_CONTROL_TOPIC, CONTROL_TOPIC);
        matcher.addURI(authority, DBContract.PATH_CONTROLS+"/*", CONTROL_GROUP_LIST);
        matcher.addURI(authority, DBContract.PATH_CONTROL_GROUPS, CONTROL_GROUP_NAMES);
        matcher.addURI(authority, DBContract.PATH_CONTROLS_SCHEDULE + "/*", CONTROL_ITEM_SCHEDULE);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            case CONTROL_GROUP_LIST: {
                retCursor = getControlsList(uri);
                break;
            }
            case CONTROL_ITEM_SCHEDULE: {
                retCursor=getControlSchedule(uri);
                break;
            }
            case CONTROL_GROUP_NAMES: {
                retCursor=getControlGroups(uri);
                break;
            }
            case CONTROL_TOPIC: {
                retCursor=getControlTopics(uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return retCursor;
    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case CONTROL_LIST:
                return ControlsEntry.CONTENT_TYPE;
            case CONTROL_ITEM_SCHEDULE:
                return ControlScheduleEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) throws SQLiteConstraintException {

        db=mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        long _id;
        switch (match) {
            case CONTROL_LIST:{
                _id=db.insertWithOnConflict(ControlsEntry.TABLE_NAME,null,values, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            }

        }
        getContext().getContentResolver().notifyChange(uri, null);

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        db=mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int _id=0;
        switch (match) {
            case CONTROL_LIST:{
                _id=db.delete(ControlsEntry.TABLE_NAME,selection+" = ?",selectionArgs);
                break;
            }

        }
        getContext().getContentResolver().notifyChange(uri, null);

        return _id;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        db=mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int _id=0;

        Timber.i("up1 %d",match);
        switch (match) {
            case CONTROL_LIST:{
                Timber.i("update %d",_id);
                _id=db.update(ControlsEntry.TABLE_NAME,values,ControlsEntry.COLUMN_MQTT_TOPIC+" = ? ",selectionArgs);
                break;
            }

        }
        getContext().getContentResolver().notifyChange(uri, null);

        return _id;
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        db=mOpenHelper.getWritableDatabase();
        int returnCount = 0;

        try{
            db.beginTransaction();

            final int match = sUriMatcher.match(uri);

            long _id;

            switch (match) {
                case CONTROL_LIST:{

                    for(ContentValues value:values){
                        _id=db.insertWithOnConflict(ControlsEntry.TABLE_NAME,null,value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    break;
                }
                default:{
                    return super.bulkInsert(uri, values);
                }

            }
            getContext().getContentResolver().notifyChange(uri, null);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            Log.i("DB","Ended transaction");
        }


        return returnCount;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
