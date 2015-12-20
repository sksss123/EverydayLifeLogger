package kr.ac.kookmin.everydaylifelogger;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class AddActivity extends AppCompatActivity {

    //맵 관련
    private GoogleMap map;
    double lat;
    double lng;
    LatLng now;
    LocationManager locationManager;
    Marker myLocation;
    int checkMap=0;

    //위젯관련
    Spinner typeAddSpinner;
    TextView dateTextView;
    Button add_bt;
    Button back_bt;
    EditText editText;

    //시간관련
    Calendar calendar = Calendar.getInstance();
    int year;
    int month;
    int day;
    int hour;
    int minute;
    String nowDate;

    //스레드
    timeRefresh update;
    Thread Update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        //구글 맵 받아오기
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        //날짜시간 갱신
        dateTextView = (TextView) findViewById(R.id.nowDate);

        //typeAddSpinner 연결
        typeAddSpinner = (Spinner) findViewById(R.id.addTypeCategory);
        ArrayAdapter adapterTypeAdd = ArrayAdapter.createFromResource(this, R.array.CategoryForType, android.R.layout.simple_spinner_item);
        adapterTypeAdd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeAddSpinner.setAdapter(adapterTypeAdd);

        //시간 업데이트
        Update = new Thread(new timeRefresh());
        Update.start();

        //텍스트 위젯
        editText = (EditText) findViewById(R.id.eventText);

        //추가버튼 클릭
        add_bt = (Button) findViewById(R.id.addFinish);
        add_bt.setOnClickListener(buttonClickListener);

        //뒤로가기버튼 클릭
        back_bt = (Button)findViewById(R.id.cancelAdd);
        back_bt.setOnClickListener(buttonClickListener);

    }
    //맵위치
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //Marker 중복 생성을 막기 위한 if문
            if(checkMap==0) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                now = new LatLng(lat, lng);

                map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                myLocation = map.addMarker(new MarkerOptions().position(now).title("MyLocation"));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(now, 15));
                map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                checkMap=1;
            }
            else
            {
                myLocation.remove();
                lat = location.getLatitude();
                lng = location.getLongitude();
                now = new LatLng(lat, lng);
                myLocation = map.addMarker(new MarkerOptions().position(now).title("MyLocation"));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(now, 15));
            }
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            return;
        }
        @Override
        public void onProviderEnabled(String provider) {
            return;
        }
        @Override
        public void onProviderDisabled(String provider) {
            return;
        }
    };

    //시간 갱신을 위한 스레드
    public class timeRefresh implements Runnable {
        @Override
        public void run() {
            while(true) {
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                hour = calendar.get(Calendar.HOUR);
                minute = calendar.get(Calendar.MINUTE);

                nowDate = year + "/";
                if (month < 10) {
                    nowDate += "0";
                }
                nowDate += month + "/";
                if (day < 10) {
                    nowDate += "0";
                }
                nowDate += day + " ";
                if (hour < 10) {
                    nowDate += "0";
                }
                nowDate += hour + ":";
                if (minute < 10) {
                    nowDate += "0";
                }
                nowDate += minute;

                Log.d(nowDate,nowDate);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        dateTextView.setText(nowDate);
                    }
                });


                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //버튼 클릭 리스너
    Button.OnClickListener buttonClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            Intent intent;
            switch (v.getId())
            {
                //추가시
                case R.id.addFinish:
                    if(typeAddSpinner.getSelectedItemPosition() == 0)
                        Toast.makeText(getApplicationContext(),"종류를 선택해 주세요.",Toast.LENGTH_SHORT).show();
                    else if(editText.getText().toString().equals(""))
                        Toast.makeText(getApplicationContext(),"내용을 입력해 주세요.",Toast.LENGTH_SHORT).show();
                    else {
                        intent = new Intent(getApplicationContext(),MainActivity.class);
                        Log.d("날짜",dateTextView.getText().subSequence(0,10).toString());
                        Log.d("시간",dateTextView.getText().subSequence(11,16).toString());
                        intent.putExtra("날짜", dateTextView.getText().subSequence(0, 10).toString());
                        intent.putExtra("시간",dateTextView.getText().subSequence(11,16).toString());
                        intent.putExtra("종류",(String) typeAddSpinner.getAdapter().getItem(typeAddSpinner.getSelectedItemPosition()));
                        intent.putExtra("내용",editText.getText().toString());
                        intent.putExtra("lng", lng);
                        intent.putExtra("lat",lat);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    break;
                case R.id.cancelAdd:
                    setResult(RESULT_CANCELED);
                    finish();
                    break;
            }
        }
    };
}
