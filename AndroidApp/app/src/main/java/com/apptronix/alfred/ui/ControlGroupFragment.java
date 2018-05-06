package com.apptronix.alfred.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.apptronix.alfred.R;
import com.apptronix.alfred.adapter.ControlsAdapter;
import com.apptronix.alfred.data.DBContract;

import timber.log.Timber;

public class ControlGroupFragment extends Fragment implements LoaderCallbacks<Cursor>, ControlsAdapter.ClickMessageListener {

    public static final String ARG_GROUP = "group";
    ListView controlsList;
    ControlsAdapter controlsAdapter;
    String group;
    Cursor data;

    private OnFragmentInteractionListener mListener;

    public ControlGroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            group=getArguments().getString(ARG_GROUP);
            getLoaderManager().initLoader(0,null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_control_group, container, false);
        controlsList=(ListView)rootView.findViewById(R.id.controlList);
        controlsAdapter = new ControlsAdapter(getActivity(),null);
        controlsList.setAdapter(controlsAdapter);
        controlsAdapter.setCustomButtonListner(this);
        controlsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                data.moveToPosition(i);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Pick an Action")
                        .setItems(R.array.actions_array, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String topic = data.getString(data.getColumnIndex(DBContract.ControlsEntry.COLUMN_MQTT_TOPIC));
                                switch (i){
                                    case 0:{
                                        Intent intent = new Intent(getActivity(),AddControlItemActivity.class);
                                        intent.putExtra("topic",topic);
                                        startActivity(intent);
                                        break;
                                    }
                                    case 1:{
                                        getActivity().getContentResolver().delete(DBContract.ControlsEntry.CONTENT_URI,DBContract.ControlsEntry.COLUMN_MQTT_TOPIC,new String[]{topic});
                                        break;
                                    }
                                }
                            }
                        });
                AlertDialog dialog=builder.create();
                dialog.show();
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            if(controlsAdapter!=null){
                controlsAdapter.notifyDataSetChanged();
            }
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new android.support.v4.content.CursorLoader(this.getContext(),
                DBContract.ControlsEntry.buildControlGroupListUri(group),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Timber.i("group %s %d",group,data.getCount());
        this.data=data;
        controlsAdapter.swapCursor(data);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0,null, this);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        controlsAdapter.swapCursor(null);
    }

    @Override
    public void sendMqttMessage(String topic, String msg) {
        mListener.messageMqttTopic(topic,msg);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void messageMqttTopic(String topic, String msg);
    }
}
