# BeiDouProbe
北斗探针SDK
#### Gradle集成SDK

在Project的build.gradle文件中配置repositories，添加maven仓库地址
```
	allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```
在主工程的build.gradle文件配置dependencies
```
	dependencies {
	       implementation 'com.github.woncan:BeiDouProbe:latest.release' //其中latest.release指代最新SDK版本号
	}
```
###  使用

#### 搜索设备
```
    搜索设备
    List<UsbSerialDriver> serialDeviceList = DeviceManager.getSerialDeviceList(MainActivity.this);
    UsbSerialDriver usbSerialDriver = serialDeviceList.get(0);
    判断权限
    if (((UsbManager) getSystemService(Context.USB_SERVICE)).hasPermission(usbSerialDriver.getDevice())) {
            connectDevice(usbSerialDriver);
    } else {
            DeviceManager.requestPermission(MainActivity.this, usbSerialDriver, isGranted -> {
            if (isGranted) {
                    connectDevice(usbSerialDriver);
                }
            });
    }
```

#### 连接设备
```
    private void connectDevice(UsbSerialDriver driver){
        Device device = DeviceManager.connectDevice(getApplicationContext(), driver);
        //监听定位信息
        device.setLocationListener(new LocationListener() {
            @Override
            public void onLocationChanged(WLocation location) {
                //定位信息
            }

            @Override
            public void onError(Exception e) {
                Log.i("TAG", "onError: "+e.getMessage());
            }
        });
        //监听卫星数据
        device.setSatelliteListener(new SatelliteListener() {
            @Override
            public void onSatelliteListener(Satellite[] satellites) {
                //卫星信息
            }
        });
        //监听设备信息
         device.setDeviceInfoListener(new DeviceInfoListener() {
            @Override
            public void onDeviceInfoReceiver(DeviceInfo deviceInfo) {
                //设备ID
                String deviceID = deviceInfo.getDeviceID();
                //设备名
                String productName = deviceInfo.getProductName(Locale.CHINA);
            }
        });
        //监听设备信息
        device.setMessageListener(new MessageListener() {
            @Override
            public void onMessageReceiver(DeviceMessage deviceMessage) {
                 if (deviceMessage.getType() == 1) {
                    String nmea = new String(deviceMessage.getMessage());
                    //type=1时输出NMEA数据
                }
            }
        });
    }
```
#### 断开连接


```
    DeviceManager.disconnect();

```


#### 日志
```
    LogUtil.setIsDebug(BuildConfig.DEBUG);
```


#### WLocation

| 方法名 | 说明| 备注|
| :--| :-- | :-- |
| getFixStatus| 获取定位状态 |0=无法定位；1=单点定位；5=浮点定位；4=固定定位|
| getDiffAge| 差分延时 |
|getLatitude| 纬度 |
| getLongitude|经度  |
| getAltitude|海拔高 |
| getAltitudeCorr|高程异常值 |大地高=海拔高+高程异常值|
| getnSatsInView| 卫星数 |
| getnSatsInUse| 参与解算的卫星数 |
| getTime| 时间戳 |
| getSpeed| 速度 |
| getBearing| 速度方向 |
| getAccuracy| 定位水平精度|
| getmVerticalAccuracy| 定位垂直精度|
| gethDOP| hdop|
| getvDOP| vdop|
| getpDOP| pdop|
