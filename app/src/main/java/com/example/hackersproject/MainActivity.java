package com.example.hackersproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener{

    private String serverIp="8.140.192.244";
    //private String serverIp="192.168.1.108";
    private String serverPort="8081";
    private String serverIndex="/json/chargers";
    private String checkIndex="/checkUpdate";
    private String downloadIndex="/download/update/app-release.apk";
    private Button btn1,btn2;
    private int needUpdate=0;

    private LocalTime dataTime,localTime;
    private long minute0;
    private double Longitude,Latitude;
    private TextView textView,timeView;
    private LocationManager locationManager;
    private AMapLocationClient mLocationClient;//定位发起端
    private AMapLocationClientOption mLocationOption = new AMapLocationClientOption();//定位参数
    private LocationSource.OnLocationChangedListener mListener = null;//定位监听器
    AlertDialog dialogAbout;
    AlertDialog dialogUpdate;
    AlertDialog dialogUpdateForce;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        public void run () {
            handler.postDelayed(this,1000); //每隔0.5秒刷新一次
            //在这里更新数据
            if(dataTime != null){
                localTime=LocalTime.now();
                long dura =Duration.between(dataTime,localTime).toMinutes();
                if(minute0 != dura) {
                    minute0 = dura;
                    runOnUiThread(() -> timeView.setText((int)minute0 + "分钟"));
                }
            }
        }
    };
    public List<Charger> chargers=new ArrayList<>();
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        timeView = findViewById(R.id.timeView);
        btn1 = findViewById(R.id.button);
        btn2 = findViewById(R.id.button3);
        dataTime = null;
        dialogAbout = new AlertDialog.Builder(this)
                .setTitle("About")    //设置标题
                .setMessage("开发者：曦微（QQ 2273805195）")  //设置提醒的信息
                .setIcon(R.mipmap.appicon)    //设置图标
                .setPositiveButton("已知晓",null) //添加确定按钮
                //.setNegativeButton("取消",null) //添加取消按钮
                .create();
        dialogUpdate = new AlertDialog.Builder(this)
                .setTitle("新版本(非强制)")    //设置标题
                .setMessage("新版本发布，可以选择更新✌️✌️")  //设置提醒的信息
                .setIcon(R.mipmap.appicon)    //设置图标
                .setPositiveButton("更新", (dialog, which) -> download()) //添加确定按钮
                .setNegativeButton("取消",null) //添加取消按钮
                .create();
        dialogUpdateForce = new AlertDialog.Builder(this)
                .setTitle("新版本(强制)")    //设置标题
                .setMessage("新版本发布，涉及到后端数据变化，旧版本可能无法使用，为强制更新🙏🙏")  //设置提醒的信息
                .setIcon(R.mipmap.appicon)    //设置图标
                .setPositiveButton("更新",(dialog, which) -> download()) //添加确定按钮
                .create();
        checkUpdate();
        getPermission();
        getSite();
        // 更加计时
        handler.post(runnable);//启动定时器
        btn1.setOnClickListener(view -> {
            Toast.makeText(getApplicationContext(), "正在获取请稍后😁", Toast.LENGTH_LONG).show(); // 显示提示信息
            sendRequestWithHttpURLConnection();
        });
    }
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        //第二个参数表示此menu的id值，在onOptionsItemSelected方法中通过id值判断是哪个menu被点击了
        menu.add(Menu.NONE, 1, 1, "检查更新");
        menu.add(Menu.NONE, 2, 2, "关于");
        return true;
    }
    //点击实现的操作
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case 1:
                checkUpdate();
                break;
            case 2:
                dialogAbout.show();
                break;
        }
        return true;
    }

    // 检查更新
    private void checkUpdate(){
        new Thread(() -> {
            chargers = new ArrayList<>();
            HttpURLConnection connection = null;
            BufferedReader reader =null;
            String url_text = "http://" + serverIp + ':' + serverPort + checkIndex;
            try {
                System.out.println(url_text);
                URL url = new URL(url_text);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 QIHU 360SE/10.0.2287.0");
                connection.setRequestProperty("contentType", "UTF-8");
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                InputStream in = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
                needUpdate = Integer.valueOf(reader.readLine());
                if(needUpdate == 1){
                    runOnUiThread(() -> dialogUpdate.show());
                }else if(needUpdate == 2){
                    runOnUiThread(() -> dialogUpdateForce.show());
                }else{
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "已是最新版本😊", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> textView.setText(e.toString()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
    public void download(){
        Toast.makeText(this, "正在下载请稍后😁", Toast.LENGTH_LONG).show();
        Uri uri = Uri.parse("http://"+serverIp+":80"+downloadIndex);    //设置跳转的网站
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);

    }
    //获取权限
    private void getPermission(){
        //判断权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            //有权限
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //获取所有可用的位置提供器
            List<String> providerList = locationManager.getProviders(true);
            if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            } else if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            } else {
                //当没有可用的位置提供器时，弹出Toast提示用户
                Toast.makeText(this, "No Location provider to use", Toast.LENGTH_SHORT).show();
            }
        }
        try {
            AMapLocationClient.updatePrivacyShow(this, true, true);
            AMapLocationClient.updatePrivacyAgree(this, true);
            mLocationClient = new AMapLocationClient(getApplicationContext());
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void jump(View v) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, mapActivity.class);
        mapActivity.chargers=chargers;
        startActivity(intent);
    }



    /*开启定位*/
    private void getSite() {
        //初始化定位
        //mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为Hight_Accuracy高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //mLocationOption.
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //可在其中解析amapLocation获取相应内容。
                aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                Latitude=aMapLocation.getLatitude();//获取纬度
                Longitude=aMapLocation.getLongitude();//获取经度
                //System.out.println(Longitude+" "+Latitude);
                aMapLocation.getAccuracy();//获取精度信息
            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("地图错误", "定位失败, 错误码:" + aMapLocation.getErrorCode() + ", 错误信息:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

    private void sendRequestWithHttpURLConnection() {
        textView.setText("");
        new Thread(() -> {
            chargers=new ArrayList<>();
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            String url_text = "http://"+serverIp+':'+serverPort+serverIndex;
            try {
                System.out.println(url_text);
                URL url = new URL(url_text);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 QIHU 360SE/10.0.2287.0");
                connection.setRequestProperty("contentType", "UTF-8");
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                //System.out.println("success1");
                InputStream in = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                pareJSON(response.toString());

            } catch (Exception e) {
                runOnUiThread(() -> textView.setText(e.toString()));
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }


    //解析

    private void pareJSON(String jsonData){
        try {
            JSONObject jsonObject=new JSONObject(jsonData);
            JSONObject jsonTime = jsonObject.getJSONObject("time");
            dataTime = LocalTime.of(jsonTime.getInt("hour"),jsonTime.getInt("minute"),jsonTime.getInt("second"));

            JSONArray array=jsonObject.getJSONArray("data");
            //List<Charger> list=new ArrayList<>();
            int len=array.length();
            for(int i=0;i<len;i++){
                JSONObject jsoni=new JSONObject(array.get(i).toString());
                Charger charger=new Charger();
                charger.name=jsoni.getString("name");
                charger.total=jsoni.getInt("total");
                charger.free= jsoni.getInt("free");
                charger.longitude=jsoni.getDouble("longitude");
                charger.latitude=jsoni.getDouble("latitude");
                charger.distance=AMapUtils.calculateLineDistance(new LatLng(charger.latitude, charger.longitude),new LatLng(Latitude, Longitude));
                chargers.add(charger);

            }
            print();

        }catch (Exception e){
            runOnUiThread(() -> textView.setText(e.toString()));
        }
        this.deactivate();
    }

    private void print(){
        Charger temp;
        for(int i=0;i<chargers.size();i++){
            int mindex=i;
            for(int j=i;j<chargers.size();j++){
                if(chargers.get(j).distance<chargers.get(mindex).distance){
                    mindex=j;
                }
            }
            temp=chargers.get(mindex);
            chargers.set(mindex,chargers.get(i));
            chargers.set(i,temp);
        }
        runOnUiThread(() -> {
            //timeView.setText();
            for(int i = 0; i< chargers.size(); i++){
                if(chargers.get(i).free >0) {
                    StringBuilder S = new StringBuilder();
                    S.append("地址:  " + chargers.get(i).name + "\n");
                    S.append("总插口数:  " + chargers.get(i).total + "\n");
                    S.append("剩余插口数:   " + chargers.get(i).free + "\n");
                    if (chargers.get(i).distance != -1)
                        S.append("距离: " + String.format("%.2f", chargers.get(i).distance) + " m\n");
                    textView.append(S);
                }
            }

        });
    }
    //激活定位
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
    }

    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);//取消定时器
    }

}