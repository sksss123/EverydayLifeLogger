package kr.ac.kookmin.everydaylifelogger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class InformationActivity extends AppCompatActivity {

    //맵 사용 변수
    Double lat;
    Double lng;
    LatLng MYLOCATION;
    private GoogleMap map;

    //위젯
    TextView date;
    TextView type;
    TextView information;
    Button delete_bt;
    Button back_bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        lat = getIntent().getDoubleExtra("lat",0);
        lng = getIntent().getDoubleExtra("lng",0);

        Log.d(lat+"",lng+"");

        MYLOCATION = new LatLng(lat, lng);

        //맵 설정
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        Marker myLocation  = map.addMarker(new MarkerOptions().position(MYLOCATION).title("MyLocation"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(MYLOCATION, 15));
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

        //시간 설정
        date = (TextView)findViewById(R.id.InformationDate);
        date.setText(getIntent().getStringExtra("날짜") + " " + getIntent().getStringExtra("시간"));

        //종류 설정
        type = (TextView)findViewById(R.id.InformationType);
        type.setText(getIntent().getStringExtra("종류"));

        //내용 설정
        information = (TextView)findViewById(R.id.InformationText);
        information.setText(getIntent().getStringExtra("내용"));

        //버튼 설정
        delete_bt = (Button)findViewById(R.id.InformationDelete);
        delete_bt.setOnClickListener(buttonClickListener);

        back_bt = (Button)findViewById(R.id.InformationBack);
        back_bt.setOnClickListener(buttonClickListener);
    }

    //버튼 클릭 리스너
    Button.OnClickListener buttonClickListener = new View.OnClickListener(){
        Intent intent;
        @Override
        public void onClick(View v){
            Intent intent;
            switch (v.getId())
            {
                //삭제시
                case R.id.InformationDelete:
                    intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.putExtra("index",getIntent().getIntExtra("index",0));
                    intent.putExtra("날짜",getIntent().getStringExtra("날짜"));
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
                case R.id.InformationBack:
                    setResult(RESULT_CANCELED);
                    finish();
                    break;
            }
        }
    };
}
