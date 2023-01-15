package com.woncan.beidouprobe;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.woncan.beidouprobe.databinding.ActivityMainBinding;
import com.woncan.device.Device;
import com.woncan.device.DeviceManager;
import com.woncan.device.bean.DeviceInfo;
import com.woncan.device.bean.WLocation;
import com.woncan.device.listener.LocationListener;
import com.woncan.device.listener.OnDeviceAttachListener;
import com.woncan.device.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding dataBinding;
    public static final String SP_NAME = "BDP_NTRIP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        dataBinding.tvNmea.setMovementMethod(ScrollingMovementMethod.getInstance());
        if (!checkLocationPermission()) {
            Toast.makeText(this, "没有定位权限", Toast.LENGTH_SHORT).show();
        }

    }

    private void connectDevice(UsbSerialDriver driver) {
        SharedPreferences sharedPreferences = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String ip = sharedPreferences.getString("ip", "");
        String port = sharedPreferences.getString("port", "0");
        String account = sharedPreferences.getString("account", "");
        String password = sharedPreferences.getString("password", "");
        String mountPoint = sharedPreferences.getString("mountPoint", "");

        Device device = DeviceManager.connectDevice(getApplicationContext(), driver,Device.CGCS2000);
        if (device == null) {
            return;
        }
        //获取设备信息
        DeviceInfo deviceInfo = device.getDeviceInfo();
        //修改输出频率
        device.set5Hz();
        //device.set1Hz();

        device.setNtripAccount(ip, Integer.parseInt(port), account, password, mountPoint);

        if (!TextUtils.isEmpty(port)) {
            device.setNtripAccount(ip, Integer.parseInt(port), account, password, mountPoint);
        }
        device.setLocationListener(new LocationListener() {
            @Override
            public void onError(int errCode, String msg) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, errCode + "   " + msg, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onLocationChanged(WLocation location) {
                runOnUiThread(() -> dataBinding.tvLocation.setText(String.format(Locale.CHINA, "status：%d\nLat：%.8f\nLon：%.8f\nAlt：%.3f\nDxy：%.3f\nDz ：%.3f\nDiffAge ：%.3f",
                        location.getFixStatus(), location.getLatitude(), location.getLongitude(), location.getAltitudeCorr() + location.getAltitude(),
                        location.getAccuracy(), location.getmVerticalAccuracy(), location.diffAge)));
            }
        });
        device.setSatelliteListener(satellite -> Log.i("TAG", "onSatelliteListener: "));
        device.setNMEAListener(s -> {
            //NMEA回调
            runOnUiThread(() -> {
                if (s.startsWith("$GNGGA") || s.startsWith("$GPGGA")) {
                    dataBinding.tvNmea.setText("");
                }
                dataBinding.tvNmea.append(s);
            });
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        dataBinding.tvNmea.setText("");
        dataBinding.tvLocation.setText("");
        int itemId = item.getItemId();
        if (itemId == R.id.connect) {
            List<UsbSerialDriver> serialDeviceList = DeviceManager.getSerialDeviceList(getApplicationContext());
            if (serialDeviceList == null || serialDeviceList.isEmpty()) {
                Toast.makeText(MainActivity.this, "没有设备", Toast.LENGTH_SHORT).show();
                return true;
            }
            UsbSerialDriver usbSerialDriver = serialDeviceList.get(0);
            requestPermission(usbSerialDriver);
        }
        if (itemId == R.id.register_connect) {
            DeviceManager.registerDeviceAttachListener(this, this::requestPermission);
        }
        if (itemId == R.id.one_hz) {
            Device device = DeviceManager.getInstance().getDevice();
            if (device != null) {
                device.set1Hz();
            } else {
                Toast.makeText(this, "请先连接设备", Toast.LENGTH_SHORT).show();
            }
        }
        if (itemId == R.id.five_hz) {
            Device device1 = DeviceManager.getInstance().getDevice();
            if (device1 != null) {
                device1.set5Hz();
            } else {
                Toast.makeText(this, "请先连接设备", Toast.LENGTH_SHORT).show();
            }
        }
        if (itemId==R.id.ntrip_setting) {
            startActivity(new Intent(this,NtripActivity.class));
        }
        return true;
    }


    private void requestPermission(UsbSerialDriver usbSerialDriver) {
        if (((UsbManager) getSystemService(Context.USB_SERVICE)).hasPermission(usbSerialDriver.getDevice())) {
            connectDevice(usbSerialDriver);
        } else {
            DeviceManager.requestPermission(MainActivity.this, usbSerialDriver, isGranted -> {
                if (isGranted) {
                    connectDevice(usbSerialDriver);
                }
            });
        }
    }


    private boolean checkLocationPermission() {
        //判断当前系统是否高于或等于6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //当前系统大于等于6.0
            return requestPermission(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
        } else {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean requestPermission(String[] permissions) {
        List<String> needPermissions = getDeniedPermissions(permissions);
        if (needPermissions.size() == 0) {
            return true;
        }
        ActivityCompat.requestPermissions(this, needPermissions.toArray(new String[0]),
                1);
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private List<String> getDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                needRequestPermissionList.add(permission);
            }
        }
        return needRequestPermissionList;
    }

}