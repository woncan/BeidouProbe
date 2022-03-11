package com.woncan.beidouprobe;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.woncan.beidouprobe.databinding.ActivityNtripBinding;
import com.woncan.device.Device;
import com.woncan.device.DeviceManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class NtripActivity extends AppCompatActivity {
    private ActivityNtripBinding dataBinding;
    public static final String SP_NAME = "BDP_NTRIP";
    private NtripViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_ntrip);
        viewModel = new ViewModelProvider(this).get(NtripViewModel.class);
        dataBinding.setViewModel(viewModel);
        dataBinding.ivBack.setOnClickListener(v -> finish());

        SharedPreferences sharedPreferences = getSharedPreferences("BDP_NTRIP", Context.MODE_PRIVATE);
        String ip = sharedPreferences.getString("ip", "");
        String port = sharedPreferences.getString("port", "");
        String account = sharedPreferences.getString("account", "");
        String password = sharedPreferences.getString("password", "");
        String mountPoint = sharedPreferences.getString("mountPoint", "");
        List<String> list = new ArrayList<>();
        list.add(mountPoint);
        dataBinding.spinner.attachDataSource(list);
        dataBinding.spinner.setSelectedIndex(0);
        viewModel.ip.set(ip);
        viewModel.port.set(port);
        viewModel.account.set(account);
        viewModel.password.set(password);


        dataBinding.tvSave.setOnClickListener(v -> {
            String ip1 = viewModel.ip.get();
            String portStr = viewModel.port.get();
            String account1 = viewModel.account.get();
            String password1 = viewModel.password.get();
            String mountPoint1 = dataBinding.spinner.getSelectedItem().toString();
            if (TextUtils.isEmpty(ip1)) {
                Toast.makeText(NtripActivity.this, "IP不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(portStr)) {
                Toast.makeText(NtripActivity.this, "端口号不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(account1)) {
                Toast.makeText(NtripActivity.this, "账号不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password1)) {
                Toast.makeText(NtripActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(mountPoint1)) {
                Toast.makeText(NtripActivity.this, "请选择挂载点", Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferences sharedPreferences1 = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPreferences1.edit();
            edit.putString("ip", ip1);
            edit.putString("port", portStr);
            edit.putString("account", account1);
            edit.putString("password", password1);
            edit.putString("mountPoint", mountPoint1);
            if (edit.commit()) {
                Toast.makeText(NtripActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                Device device = DeviceManager.getInstance().getDevice();
                if (device != null && !TextUtils.isEmpty(portStr)) {
                    assert portStr != null;
                    device.setNtripAccount(ip1, Integer.parseInt(portStr), account1, password1, mountPoint1);
                }
            }
        });

        dataBinding.btnMountPoint.setOnClickListener(v -> getMountPoint());
    }


    private void getMountPoint() {
        String ip = viewModel.ip.get();
        String port = viewModel.port.get();
        if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(port)) {
            Toast.makeText(this, "IP和端口号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    assert port != null;
                    Socket socket = new Socket(ip, Integer.parseInt(port));
                    socket.setSoTimeout(10000);
                    if (socket.isConnected()) {
                        OutputStream outputStream = socket.getOutputStream();
                        InputStream inputStream = socket.getInputStream();
                        if (outputStream == null || inputStream == null) {
                            return;
                        }
                        String params = "GET / HTTP/1.0\r\nUser-Agent: NTRIP GNSSInternetRadio/1.4.10\r\nAccept: */*\r\nConnection: close\r\n";
                        outputStream.write((params + "\r\n").getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                        boolean isEnd = false;
                        StringBuilder source = new StringBuilder();
                        while (!isEnd) {
                            int count;
                            do {
                                count = inputStream.available();
                            } while (count == 0);
                            byte[] data = new byte[count];
                            int length = inputStream.read(data, 0, count);
                            if (length != -1) {
                                source.append(new String(data));
                                if (!source.toString().startsWith("SOURCETABLE")) {
                                    isEnd = true;
                                }
                                if (source.toString().contains("ENDSOURCETABLE")) {
                                    isEnd = true;
                                }
                            }
                            Log.i("TAG", "run: " + source.toString().trim());
                        }
                        String result = source.toString();
                        if (result.contains("SOURCETABLE 200 OK")) {
                            List<String> mountPointList = new ArrayList<>();
                            String[] split = result.split("\r\n");
                            for (String s : split) {
                                if (s.startsWith("STR;")) {
                                    String[] split1 = s.split(";");
                                    mountPointList.add(split1[1]);
                                }
                            }
                            dataBinding.spinner.attachDataSource(mountPointList);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();


    }
}