package com.example.dagger2;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public LocationClient mLocationClient;
    private volatile boolean isRefreshUI = true;
    TextView positionText = null;
    StringBuilder currenPosition=null;
    public LatLng p1LL=null,p2LL=null;
    public double distance=0.0d;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }

    private Handler refreshHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    positionText.setText(currenPosition);
                    Log.d(TAG, "handleMessage: "+currenPosition);
                    break;
            }

        }
    };


    private Timer refreshTimer = new Timer(true);
    private TimerTask refreshTask = new TimerTask() {
        @Override
        public void run() {
            if (isRefreshUI) {
                Message msg = refreshHandler.obtainMessage();
                msg.what = 1;
                refreshHandler.sendMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplication());
        setContentView(R.layout.activity_main);
        mLocationClient=new LocationClient(MainActivity.this);
        mLocationClient.registerLocationListener(new MylocationListener());
        positionText= ((TextView) findViewById(R.id.textabcd));
        List<String> permissionList= new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.
                permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
       if (!permissionList.isEmpty()){
           String [] permissions = permissionList.toArray(new String[permissionList.size()]);
           ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
       }else {
           requestLication();


       }
    }

    private void  requestLication(){
        init();
        mLocationClient.start();

    }
    private void  init(){
        LocationClientOption option =new LocationClientOption();
        option.setOpenGps(true);
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setCoorType("bd09ll");
        option.setScanSpan(5000);
        mLocationClient.setLocOption(option);
        refreshTimer.schedule(refreshTask, 0, 5000);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result : grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须授权",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLication();
                }else {
                    Toast.makeText(this,"错误",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class MylocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location) {
            currenPosition =new StringBuilder();
            currenPosition.append("纬度").append(location.getLatitude()).append("\n");
            currenPosition.append("经度").append(location.getLongitude()).append("\n");

            currenPosition.append("定位方式:");
            if(location.getLocType()==BDLocation.TypeGpsLocation){
                currenPosition.append("GPS");
                if(p1LL==null){
                    p1LL=new LatLng(location.getLatitude(),location.getLongitude());

                }
                else {
                    p2LL=new LatLng(location.getLatitude(),location.getLongitude());
                    Double d= DistanceUtil.getDistance(p1LL,p2LL);
                    Log.d(TAG, "onReceiveLocation: "+d.toString());
                    distance+=d;
                    p1LL=p2LL;
                    currenPosition.append("\n距离："+String.valueOf(distance));

                }
            }else if (location.getLocType()==BDLocation.TypeNetWorkLocation){
                currenPosition.append("网络");
            }



        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }
}
