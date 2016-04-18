package com.app.joe.sleeptracker;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.MetaWearBoard;

public class InfoFragment extends Fragment {

    public MetaWearBoard mwBoard;

    private MainActivity myActivity;
    private String serialNumber;
    private String fwVersion;
    private String macAddress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, vg, false);

        mwBoard.readDeviceInformation().onComplete(new AsyncOperation.CompletionHandler<MetaWearBoard.DeviceInformation>() {
            @Override
            public void success(MetaWearBoard.DeviceInformation result) {
                serialNumber = result.serialNumber();
                fwVersion = result.firmwareRevision();
                macAddress = mwBoard.getMacAddress();
            }
        });

        TextView test = (TextView) view.findViewById(R.id.textViewInfoSerialNumber);
        test.setText(serialNumber);

        TextView textViewFWVersion = (TextView) view.findViewById(R.id.textViewInfoFWRevision);
        textViewFWVersion.setText(fwVersion);

        TextView textViewMACAddress = (TextView) view.findViewById(R.id.textViewInfoMACAddress);
        textViewMACAddress.setText(macAddress);

        return view;
    }
}