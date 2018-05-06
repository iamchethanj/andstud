package com.apptronix.alfred.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by DevOpsTrends on 12/7/2016.
 */

public class DBContract {

    public static final String CONTENT_AUTHORITY = "com.apptronix.alfred.data.DBProvider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_CONTROLS = "controls";

    public static final String PATH_CONTROL_GROUPS = "control_groups";

    public static final String PATH_CONTROL_TOPIC = "control_topic";

    public static final String PATH_CONTROLS_SCHEDULE = "controlschedule";

    public static  final class ControlsEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTROLS).build();

        public static final Uri GROUPS_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTROL_GROUPS).build();

        public static final Uri TOPIC_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTROL_TOPIC).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTROLS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTROLS;

        public static final String TABLE_NAME = "controls";

        public static final String COLUMN_CONTROL_NAME = "name";

        public static final String COLUMN_CONTROL_STATUS = "status";

        public static final String COLUMN_CONTROL_GROUP = "control_group";

        public static final String COLUMN_CONTROL_TYPE = "type";

        public static final String COLUMN_MQTT_TOPIC = "mqtt_topic";

        public static Uri buildControlGroupListUri(String group){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(group)).build();
        }

        public static final String getControlGroupName(Uri uri){
            return uri.getPathSegments().get(1);
        }
    }

    public static  final class ControlScheduleEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTROLS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTROLS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTROLS;

        public static final String TABLE_NAME = "controlschedule";

        public static final String COLUMN_CONTROL_NAME = "name";

        public static final String COLUMN_SCHEDULED_STATE = "sched_status";

        public static final String COLUMN_SCHEDULED_TIME = "time";

        public static final String COLUMN_SCHEDULED_REPEAT = "repeat";

        public static final String getControlName(Uri uri){
            return uri.getPathSegments().get(1);
        }

    }

}
