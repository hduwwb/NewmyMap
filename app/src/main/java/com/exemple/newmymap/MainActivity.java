package com.exemple.newmymap;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private BluetoothSPP bluetoothSPP;
    private List<String> contactsNumList = new ArrayList<>();
    private List<Contacts> contactsList;
    private static String add;
    private DrawerLayout drawerLayout;
    public LocationClient locationClient;
    public MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    private MyLocationData locData;
    private int ci_shu = 1;
    private int jian_ge = 5;
    private int spiner_cishu;
    private int spiner_jiange;
    private Handler handler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 9:
                    String data=(String)msg.obj;
                    if(data.equals("true")){
                        Toast.makeText(MainActivity.this,"准备发送发送短信",Toast.LENGTH_SHORT).show();
                        SendMessage(mCurrentLat,mCurrentLon);
                    }
                    break;
                default:
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mapView = (MapView)findViewById(R.id.baiduMap);
        baiduMap = mapView.getMap();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//获取传感器管理服务
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_scan:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        if (bluetoothSPP.getServiceState() == BluetoothState.STATE_CONNECTED) {
                            bluetoothSPP.disconnect();
                        } else {
                            Intent intent1 = new Intent(getApplicationContext(), DeviceList.class);
                            startActivityForResult(intent1, BluetoothState.REQUEST_CONNECT_DEVICE);
                        }
                        break;
                    case R.id.nav_friends:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        Intent intent = new Intent(MainActivity.this,MyContacts.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_disConnect:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        if (bluetoothSPP.getServiceState() == BluetoothState.STATE_CONNECTED) {
                            bluetoothSPP.disconnect();
                        }
                        break;
                    case R.id.nav_send:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        send("true");
                        break;
                    case R.id.help:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        Intent intent1 = new Intent(MainActivity.this,Help.class);
                        startActivity(intent1);
                        break;
                    case R.id.msgSet:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        showDialogView();
                        break;
                }
                return true;
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        }
        initBL();//初始化蓝牙
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.CALL_PHONE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CONTACTS)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_CONTACTS);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.SEND_SMS);
        }
        if (!permissionList.isEmpty()){
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            requstLocation();
        }

    }
    private void showDialogView() {
        AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(MainActivity.this);
        final View dialogView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.msg_set,null);
        customizeDialog.setTitle("选择短信频率和次数");
        customizeDialog.setView(dialogView);
        Spinner spiner_cishu = (Spinner)dialogView.findViewById(R.id.spinner_cishu);
        int current_cishu = (int)SharedPreferencesUtils.get(MainActivity.this,"cishu",1);
        if (current_cishu == 1){
            spiner_cishu.setSelection(0);
        }else if (current_cishu == 2){
            spiner_cishu.setSelection(1);
        }else if (current_cishu == 3){
            spiner_cishu.setSelection(2);
        }else if (current_cishu == 4){
            spiner_cishu.setSelection(3);
        }else if (current_cishu == 5){
            spiner_cishu.setSelection(4);
        }
        Spinner spiner_jiange = (Spinner)dialogView.findViewById(R.id.spinner_jiange);
        int current_jiange = (int)SharedPreferencesUtils.get(MainActivity.this,"jiange",5000);
        if (current_jiange == 5000){
            spiner_jiange.setSelection(0);
        }else if (current_jiange == 10000){
            spiner_jiange.setSelection(1);
        }else if (current_jiange == 15000){
            spiner_jiange.setSelection(2);
        }else if (current_jiange == 20000){
            spiner_jiange.setSelection(3);
        }else if (current_jiange == 25000){
            spiner_jiange.setSelection(4);
        }else if (current_jiange == 30000){
            spiner_jiange.setSelection(5);
        }
        spiner_cishu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        SharedPreferencesUtils.put(MainActivity.this,"cishu",1);
                        Log.d("Dai",position+"");
                        break;
                    case 1:
                        SharedPreferencesUtils.put(MainActivity.this,"cishu",2);
                        break;
                    case 2:
                        SharedPreferencesUtils.put(MainActivity.this,"cishu",3);
                        break;
                    case 3:
                        SharedPreferencesUtils.put(MainActivity.this,"cishu",4);
                        break;
                    case 4:
                        SharedPreferencesUtils.put(MainActivity.this,"cishu",5);
                        break;
                    default:
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spiner_jiange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        SharedPreferencesUtils.put(MainActivity.this,"jiange",5000);
                        break;
                    case 1:
                        SharedPreferencesUtils.put(MainActivity.this,"jiange",10000);
                        break;
                    case 2:
                        SharedPreferencesUtils.put(MainActivity.this,"jiange",15000);
                        break;
                    case 3:
                        SharedPreferencesUtils.put(MainActivity.this,"jiange",20000);
                        break;
                    case 4:
                        SharedPreferencesUtils.put(MainActivity.this,"jiange",25000);
                        break;
                    case 5:
                        SharedPreferencesUtils.put(MainActivity.this,"jiange",30000);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        customizeDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 获取EditView中的输入内容

                        ci_shu = (int)SharedPreferencesUtils.get(MainActivity.this,"cishu",1);
                        jian_ge = (int)SharedPreferencesUtils.get(MainActivity.this,"jiange",5000);
                    }
                });
        customizeDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        customizeDialog.show();

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
            baiduMap.setMyLocationData(locData);
        }
        lastX = x;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void requstLocation(){
        initLocation();

        locationClient.start();

    }
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

        int span=1000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死


     locationClient.setLocOption(option);
        baiduMap.setMyLocationEnabled(true);
    }
    public class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            navigateTo(bdLocation);
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }
    private void navigateTo(BDLocation location){
        if (location == null || mapView == null) {
            return;
        }
        mCurrentLat = location.getLatitude();
        mCurrentLon = location.getLongitude();
        mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder()
                   .accuracy(location.getRadius())
                   // 此处设置开发者获取到的方向信息，顺时针0-360
                   .direction(mCurrentDirection).latitude(location.getLatitude())
                   .longitude(location.getLongitude()).build();
// 设置定位数据
           baiduMap.setMyLocationData(locData);
// 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
        LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
        getadd(ll);
        if (isFirstLocate){
            isFirstLocate = false;
            baiduMap.setMyLocationConfiguration(new MyLocationConfiguration(null,true,null));
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(18.0f);
            baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            getadd(ll);
        }

    }
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode){
                case 1:
                    if (grantResults.length > 0){
                        for (int result : grantResults){
                            if (result != PackageManager.PERMISSION_GRANTED){
                                Toast.makeText(this, "必须同意所有权限才能使用本程序！", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }
                        }
                        requstLocation();
                    }else {
                        Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
                default:
            }
        }
    private void initBL(){
        bluetoothSPP=new BluetoothSPP(this,handler);
        if(!bluetoothSPP.isBluetoothAvailable()){
            Toast.makeText(getApplicationContext()
                    , "蓝牙不可用"
                    , Toast.LENGTH_SHORT).show();
        }
        bluetoothSPP.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        bluetoothSPP.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "连接至 " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext()
                        , "失去连接", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext()
                        , "无法连接", Toast.LENGTH_SHORT).show();
            }

        });

    }
    /**
     * 把百度坐标转化成地址
     * @param ll
     */
    public void getadd(LatLng ll){
        GeoCoder geoCoder=GeoCoder.newInstance();
        OnGetGeoCoderResultListener listener=new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                add =  reverseGeoCodeResult.getAddress();
            }
        };
        geoCoder.setOnGetGeoCodeResultListener(listener);
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(ll));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.scan:
                if (bluetoothSPP.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bluetoothSPP.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
                break;
            case R.id.discoverable:
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                //最长可见时间为300s
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
                break;
            case R.id.about:
                break;
            case R.id.share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
                intent.putExtra(Intent.EXTRA_TEXT, "蓝牙调试助手实现智能蓝牙");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, getTitle()));
                break;
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:

        }
        return true;
    }
    private void send(String a){

        bluetoothSPP.send(a, true);

    }
    @Override
    protected void onStart() {
        super.onStart();
        if (!bluetoothSPP.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bluetoothSPP.isServiceAvailable()) {
                bluetoothSPP.setupService();
                bluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                try {
                    bluetoothSPP.connect(data);
                    String address = data.getExtras().getString(BluetoothState.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
                }catch (Exception e){
                    e.printStackTrace();
                }

        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bluetoothSPP.setupService();
                bluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
            }
        }
    }

    /**
     * 选择联系人
     * @param MyLatitude
     * @param MyLongitude
     */
    private synchronized void SendMessage(double MyLatitude,double MyLongitude){
        if (!contactsNumList.isEmpty()){
            contactsNumList.clear();
        }
        contactsList = DataSupport.findAll(Contacts.class);
        for (Contacts contacts : contactsList){
            contactsNumList.add(contacts.getNumber());
        }
        if(contactsNumList.isEmpty()){
            Toast.makeText(this, "联系人不能为空", Toast.LENGTH_SHORT).show();
        }else{
            String Message="Help! 我的"+"当前经度:"+String.valueOf(MyLatitude)+"当前纬度:"+String.valueOf(MyLongitude)+"当前地理位置"+this.add;
                send2(contactsNumList,Message);
        }

    }

    private void send2(List<String> contactsNum, String message){
        SmsManager smsm=SmsManager.getDefault();
        PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(), 0);
        for (int i = 0;i < contactsNum.size();i++){
            smsm.sendTextMessage(contactsNum.get(i),null,message,null,pi);
        }
        if (ci_shu  > 1){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.sleep(jian_ge);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    SendMessage(mCurrentLat,mCurrentLon);
                }
            }).start();
            ci_shu = ci_shu - 1;
        }else {
            ci_shu = 2;
        }
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        locationClient.stop();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
        baiduMap.setBaiduHeatMapEnabled(false);
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理

    }
    @Override
    protected void onStop() {
        //取消注册传感器监听
        mSensorManager.unregisterListener(this);
        super.onStop();
    }
}
