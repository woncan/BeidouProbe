package com.woncan.beidouprobe;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.woncan.beidouprobe.databinding.ActivityMainBinding;
import com.woncan.device.Device;
import com.woncan.device.DeviceManager;
import com.woncan.device.bean.DeviceInfo;
import com.woncan.device.bean.WLocation;
import com.woncan.device.listener.LocationListener;
import com.woncan.device.listener.NMEAListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        dataBinding.btnConnect.setOnClickListener(v -> {

            List<UsbSerialDriver> serialDeviceList = DeviceManager.getSerialDeviceList(getApplicationContext());
            if (serialDeviceList == null || serialDeviceList.isEmpty()) {
                Log.i("TAG", "UsbSerialDriver:   ==  0");
                return;
            }
            UsbSerialDriver usbSerialDriver = serialDeviceList.get(0);

            if (((UsbManager) getSystemService(Context.USB_SERVICE)).hasPermission(usbSerialDriver.getDevice())) {
                connectDevice(usbSerialDriver);
            } else {
                DeviceManager.requestPermission(MainActivity.this, usbSerialDriver, isGranted -> {
                    if (isGranted) {
                        connectDevice(usbSerialDriver);
                    }
                });
            }
        });
    }

    private void connectDevice(UsbSerialDriver driver) {

        Device device = DeviceManager.connectDevice(getApplicationContext(), driver);
        device.setNMEAListener(new NMEAListener() {
            @Override
            public void onNMEAReceiver(String s) {
                Log.i("TAG", "onNMEAReceiver: "+s);
            }
        });
        device.setLocationListener(new LocationListener() {
            @Override
            public void onError(Exception e) {
                Log.i("TAG", "onError: "+e.getMessage());
            }

            @Override
            public void onLocationChanged(WLocation location) {
                Log.i("TAG", "onLocationChanged: " + location.toString());
            }
        });
        device.setSatelliteListener(satellite -> Log.i("TAG", "onSatelliteListener: "));
        device.setNMEAListener(new NMEAListener() {
            @Override
            public void onNMEAReceiver(String s) {
                //NMEA回调
            }
        });
        DeviceInfo deviceInfo = device.getDeviceInfo();
    }

}