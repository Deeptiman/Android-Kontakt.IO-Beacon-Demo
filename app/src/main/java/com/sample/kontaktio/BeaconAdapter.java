package com.sample.kontaktio;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;

import java.util.List;

public class BeaconAdapter extends BaseAdapter{


    Context context;
    List<RemoteBluetoothDevice> deviceList;

    public BeaconAdapter(Context context, List<RemoteBluetoothDevice> deviceList){
        this.context = context;
        this.deviceList = deviceList;
    }


    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
         ViewHolder viewHolder = null;

        if(convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.beacon_list, null);
            viewHolder = new ViewHolder();

            viewHolder.beaconName = (TextView) convertView.findViewById(R.id.beacon_name);
            viewHolder.beaconRSSI = (TextView) convertView.findViewById(R.id.beacon_rssi);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        RemoteBluetoothDevice remoteBluetoothDevice = deviceList.get(position);

        viewHolder.beaconName.setText(remoteBluetoothDevice.getUniqueId());

        viewHolder.beaconRSSI.setText(""+remoteBluetoothDevice.getRssi());

        return convertView;
    }

    public class ViewHolder{
        TextView beaconName , beaconRSSI;
    }

}
