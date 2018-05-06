package com.apptronix.alfred.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.apptronix.alfred.R;
import com.apptronix.alfred.data.DBContract;

import org.json.JSONObject;

import timber.log.Timber;


/**
 * Created by DevOpsTrends on 6/25/2017.
 */

public class ControlsAdapter extends CursorAdapter{

    Cursor cursor;
    ControlViewHolder holder;
    Context mContext;

    public ControlsAdapter(Context context, Cursor c) {
        super(context, c);
        cursor=c;
        mContext=context;
    }

    ClickMessageListener clickMessageListener;
    String topic, name, type;

    public void setCustomButtonListner(ClickMessageListener listener) {
        this.clickMessageListener = listener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.control_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        Timber.i("view binded");
        if(cursor.getCount()>0){
           setupView(view,cursor);
        }
    }

    public  void setupView(View v,Cursor cursor){
        holder = new ControlViewHolder(v);
        type = cursor.getString(cursor.getColumnIndex(DBContract.ControlsEntry.COLUMN_CONTROL_TYPE));
        name = cursor.getString(cursor.getColumnIndex(DBContract.ControlsEntry.COLUMN_CONTROL_NAME));
        topic = cursor.getString(cursor.getColumnIndex(DBContract.ControlsEntry.COLUMN_MQTT_TOPIC));
        holder.controlName.setText(name);
        int state = cursor.getInt(cursor.getColumnIndex(DBContract.ControlsEntry.COLUMN_CONTROL_STATUS));

        if(type.contains("Light")){
            holder.controlImage.setImageResource(R.drawable.lightbulb);
            if(state==0){
                holder.controlState.setChecked(false);
            } else {
                holder.controlState.setChecked(true);
            }
            holder.controlState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    String msg;
                    if(b){
                        msg = "{state:1}";
                    } else {
                        msg = "{state:0}";
                    }
                    clickMessageListener.sendMqttMessage(topic,msg);
                }
            });
        } else if(type.contains("Fan")){
            holder.controlImage.setImageResource(R.drawable.fan);
        } else if(type.contains("Power")){
            holder.controlImage.setImageResource(R.drawable.ic_power_black_24dp);
            if(state==0){
                holder.controlState.setChecked(false);
            } else {
                holder.controlState.setChecked(true);
            }
            holder.controlState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    String msg;
                    if(b){
                        msg = "{state:1}";
                    } else {
                        msg = "{state:0}";
                    }
                    clickMessageListener.sendMqttMessage(topic,msg);
                }
            });
        }
    }

    private class ControlViewHolder {

        View base;
        TextView controlName;
        ImageView controlImage;
        Switch controlState;

        public ControlViewHolder(View itemView) {
            base=itemView;
            controlName=(TextView)itemView.findViewById(R.id.controlText);
            controlImage=(ImageView) itemView.findViewById(R.id.controlImage);
            controlState=(Switch) itemView.findViewById(R.id.controlStatus);
        }

    }

    public interface ClickMessageListener {
        void sendMqttMessage(String topic, String msg);
    }
}
