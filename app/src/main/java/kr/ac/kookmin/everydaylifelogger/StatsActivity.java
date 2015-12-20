package kr.ac.kookmin.everydaylifelogger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class StatsActivity extends AppCompatActivity {

    ListView sList;
    Button time_bt;
    Button type_bt;
    Button back_bt;

    ArrayAdapter<String> statsAdapter;
    ArrayList<String> staList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        //위젯 초기화
        sList = (ListView) findViewById(R.id.statsList);
        time_bt = (Button) findViewById(R.id.asTime);
        type_bt = (Button) findViewById(R.id.asType);
        back_bt = (Button) findViewById(R.id.statsBack);

        staList = new ArrayList<String>();
        statsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, staList);
        sList.setAdapter(statsAdapter);

        //리스트 초기화
        initList();

        time_bt.setOnClickListener(buttonClickListener);
        type_bt.setOnClickListener(buttonClickListener);
        back_bt.setOnClickListener(buttonClickListener);
    }

    //버튼 클릭 리스너
    Button.OnClickListener buttonClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            staList.clear();
            switch (v.getId())
            {
                //시간별로 통계 출력
                case R.id.asTime:
                    ArrayList<String> getTimeArr = getIntent().getStringArrayListExtra("총시간");
                    ArrayList<String> getAsDate = getIntent().getStringArrayListExtra("시간");

                    for(int a=1; a < getAsDate.size(); a++){
                        String insertStats = getAsDate.get(a);
                        staList.add(insertStats);

                        int numOfStats = 0;
                        for(int b=0; b < getTimeArr.size(); b++){
                            if(getAsDate.get(a).equals(getTimeArr.get(b))){
                                numOfStats++;
                            }
                        }
                        float percent = Float.parseFloat(String.valueOf(numOfStats))/Float.parseFloat(String.valueOf(getTimeArr.size()));
                        insertStats = "                 --기록한 로그 : " + numOfStats + "개, " + (percent*100)+"%";
                        staList.add(insertStats);
                    }
                    statsAdapter.notifyDataSetChanged();
                    break;
                //종류별로 통계 출력
                case R.id.asType:
                    ArrayList<String> getTypeArr = getIntent().getStringArrayListExtra("총종류");
                    ArrayList<String> getAsType = getIntent().getStringArrayListExtra("종류");

                    for(int a=1; a < getAsType.size(); a++){
                        String insertStats = getAsType.get(a);
                        staList.add(insertStats);

                        int numOfStats = 0;
                        for(int b=0; b < getTypeArr.size(); b++){
                            if(getAsType.get(a).equals(getTypeArr.get(b))){
                                numOfStats++;
                            }
                        }
                        float percent = Float.parseFloat(String.valueOf(numOfStats))/Float.parseFloat(String.valueOf(getTypeArr.size()));
                        insertStats = "                 --기록한 로그 : " + numOfStats + "개, " + (percent*100)+"%";
                        staList.add(insertStats);
                    }
                    statsAdapter.notifyDataSetChanged();
                    break;
                case R.id.statsBack:
                    finish();
                    break;
            }
        }
    };

    //ListView 초기화 함수
    public void initList(){
        staList.clear();
        staList.add("하단의 버튼을 눌러 주세요.");
        statsAdapter.notifyDataSetChanged();
    }
}
