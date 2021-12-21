package com.woncan.beidouprobe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.woncan.beidouprobe.databinding.ActivityMainBinding;
import com.woncan.device.Device;
import com.woncan.device.DeviceManager;
import com.woncan.device.bean.DeviceInfo;
import com.woncan.device.bean.DeviceMessage;
import com.woncan.device.bean.Satellite;
import com.woncan.device.bean.WLocation;
import com.woncan.device.listener.DeviceInfoListener;
import com.woncan.device.listener.LocationListener;
import com.woncan.device.listener.MessageListener;
import com.woncan.device.listener.SatelliteListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        device.setMessageListener(deviceMessage -> {
            if (deviceMessage.getType()==1){
                Log.i("TAG", "connectDevice: "+new String(deviceMessage.getMessage()));
            }
        });
        device.setLocationListener(new LocationListener() {
            @Override
            public void onLocationChanged(WLocation location) {
                Log.i("TAG", "onLocationChanged: " + location.toString());
            }

            @Override
            public void onStatusChanged(int status) {
                Log.i("TAG", "onStatusChanged: " + status);
            }

        });
        device.setSatelliteListener(satellite -> Log.i("TAG", "onSatelliteListener: "));
        device.setDeviceInfoListener(deviceInfo -> {
            Log.i("TAG", "DeviceInfoListener: ");
        });
    }

}