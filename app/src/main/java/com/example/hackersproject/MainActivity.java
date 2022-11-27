package com.example.hackersproject;

import androidx.appcompat.app.AppCompatActivity;



import android.content.Intent;

import android.location.LocationManager;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.LocationSource;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener{

    private Button btn1,btn2;
    private double Longitude,Latitude;
    private TextView textView;
    private LocationManager locationManager;
    private String provider;
    private AMapLocationClient mLocationClient;//定位发起端
    private AMapLocationClientOption mLocationOption = new AMapLocationClientOption();//定位参数
    private LocationSource.OnLocationChangedListener mListener = null;//定位监听器

    public List<Charger> chargers=new ArrayList<>();
    private List<double[]> locations=new ArrayList<>();
    private void initlist(){
        double baselo=120.685713;
        double basela=36.374363;
        for(int i=0;i<8;i++){
            if(i==4){
                baselo=120.685681;
                basela=36.374991;
            }
            double[] pos=new double[2];

            pos[0]= baselo+(double)i*0.00025;
            pos[1]=basela;
            locations.add(pos);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        btn1 = findViewById(R.id.button);
        btn2 = findViewById(R.id.button3);


        initlist();
        try {
            AMapLocationClient.updatePrivacyShow(this, true, true);
            AMapLocationClient.updatePrivacyAgree(this, true);
            mLocationClient = new AMapLocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        getSite();
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "正在获取请稍后😁", Toast.LENGTH_LONG).show(); // 显示提示信息
                sendRequestWithHttpURLConnectionNorth();
                sendRequestWithHttpURLConnectionSouth();

            }
        });
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
        mLocationOption.setInterval(1000);
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

    private void sendRequestWithHttpURLConnectionNorth() {
        textView.setText("");
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                List<String> urls=new ArrayList<>();
                urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000012097");
                urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000012095");
                urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000012099");
                urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000012096");
                urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000009531");
                urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000009532");
                urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000011046");
                urls.add("http://cdz.gpsserver.cn/ChargeCarSys?gtel=18000011047");
                try {
                    for(int i=0;i<8;i++) {
                        System.out.println(urls.get(i));
                        URL url = new URL(urls.get(i));
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(3000);
                        connection.setReadTimeout(3000);
                        //System.out.println("success1");
                        InputStream in = connection.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        System.out.println(response.toString());
                        pareNorthJSON(response.toString(),i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
            }
        }).start();
    }
    //解析 1
    private void pareNorthJSON(String jsonData,int n){
        try {
            //JSONObject jsonObject=new JSONObject(jsonData);
            JSONArray array1=new JSONArray(jsonData);
            JSONObject jsonObject=array1.getJSONObject(0);
            String name=jsonObject.getString("mc");
            String stotal=jsonObject.getString("gls");


            StringBuilder list=new StringBuilder();
            Charger charger=new Charger();
            charger.name=name;
            charger.latitude=locations.get(n)[1];
            charger.longitude=locations.get(n)[0];
            list.append("地址:  "+name+"\n");
            list.append("总插口数:  "+stotal+"\n");
            int total=Integer.parseInt(stotal);
            charger.total=total;
            int free=total;
            for(int i=1;i<=total;i++){
                int s=jsonObject.getInt("glzt"+i);
                System.out.println(s);
                if(s==1){
                    free-=1;
                };
            }
            charger.free=free;
            list.append("剩余插口数:  "+free+"\n");

            if(free>0) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        chargers.add(charger);
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }
    private void sendRequestWithHttpURLConnectionSouth() {
        textView.setText("");
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                getSite();
                try {

                    double longitude=Longitude;
                    double latitude=Latitude;
                    System.out.println(Longitude+" "+Latitude);
                    String urlst="https://xlr.xlvren.com/jweb_autocharge/position/listPosition.json?longitude="+longitude+"&latitude="+latitude+"&sid=8Gsomxku5QrL&showProprietary=1";
                    System.out.println(urlst);
                    URL url = new URL(urlst);

                    //URL url = new URL("https://xlr.xlvren.com/jweb_autocharge/position/listPosition.json?longitude=120.687256&latitude=36.37475&sid=8Gsomxku5QrL&showProprietary=1");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(3000);
                    connection.setReadTimeout(3000);
                    //System.out.println("success1");
                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println(response.toString());
                    pareSouthJSON(response.toString());

                } catch (Exception e) {
                    e.printStackTrace();
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
            }
        }).start();
    }
    //解析2
    private void pareSouthJSON(String jsonData){
        try {
            JSONObject jsonObject=new JSONObject(jsonData);
            JSONArray array=jsonObject.getJSONArray("data");
            //List<Charger> list=new ArrayList<>();
            int len=array.length();
            for(int i=0;i<len;i++){
                JSONObject jsoni=new JSONObject(array.get(i).toString());
                Charger charger=new Charger();
                charger.name=jsoni.getString("stationName");
                charger.distance=(double)jsoni.get("distance");
                charger.total=jsoni.getInt("totalPile");
                charger.free= jsoni.getInt("freePile");
                charger.longitude=jsoni.getDouble("longitude");
                charger.latitude=jsoni.getDouble("latitude");
                if(charger.free>0){
                    chargers.add(charger);
                }else{
                    len--;
                }

            }
            //Log.d("MainActivity","今天的天气是：\n"+content);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i< chargers.size(); i++){
                        StringBuilder S=new StringBuilder();
                        S.append("地址:  "+chargers.get(i).name+"\n");
                        S.append("总插口数:  "+chargers.get(i).total+"\n");
                        S.append("剩余插口数:   "+chargers.get(i).free+"\n");
                        if(chargers.get(i).distance!=-1)S.append("距离: "+chargers.get(i).distance+" m\n");
                        textView.append(S);
                    }

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        this.deactivate();
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


    /**
     * 方法必须重写
     */
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

    }
}