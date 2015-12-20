package kr.ac.kookmin.everydaylifelogger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //SQLite 관련 위젯 및 변수
    SQLiteDatabase DB;
    String DBName = "logList.db";
    String tableName = "logListTable";
    int DBMode = Context.MODE_PRIVATE;
    ArrayAdapter<String> baseAdapter;
    ArrayList<String> eventList;
    ArrayList<Integer> IdIndex;
    //메인 리스트뷰 및 스피너 관련 위젯 및 변수
    ArrayAdapter adapterType;
    ArrayAdapter adapterTime;
    ArrayList<String> timeArray;
    Spinner typeSpinner;
    Spinner timeSpinner;
    ListView listView;
    //버튼 관련 위젯
    Button add_bt;
    Button stats_bt;
    Button finish_bt;
    //상수
    final String N = "nop";
    final int ADD_REQUEST = 1000;
    final int INFORMATION_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //DB 생성
        DB = openOrCreateDatabase(DBName,DBMode,null);
        createTable();

        //위젯 연결
        typeSpinner = (Spinner)findViewById(R.id.typeCategory);
        timeSpinner = (Spinner)findViewById(R.id.timeCategory);
        listView = (ListView)findViewById(R.id.logList);
        add_bt = (Button)findViewById(R.id.add);
        stats_bt = (Button)findViewById(R.id.stats);
        finish_bt = (Button)findViewById(R.id.finish);

        //DB 어댑터 연결
        eventList = new ArrayList<String>();
        baseAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, eventList);
        listView.setAdapter(baseAdapter);
        IdIndex = new ArrayList<Integer>();  //ListView에서 ID를 구분하는 링크드리스트

        //typeSpinner 어댑터 연결
        adapterType = ArrayAdapter.createFromResource(this, R.array.CategoryForType, android.R.layout.simple_spinner_item);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapterType);

        //timeSpinner 어댑터 연결
        timeArray = new ArrayList<String>();
        adapterTime = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, timeArray);
        adapterTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(adapterTime);

        //리스트뷰 초기화
        initRefreshList();

        //스피너 클릭리스너 연결
        timeSpinner.setOnItemSelectedListener(spinnerSelctedListener);
        typeSpinner.setOnItemSelectedListener(spinnerSelctedListener);

        //버튼 클릭리스너 연결
        add_bt.setOnClickListener(buttonClickListener);
        stats_bt.setOnClickListener(buttonClickListener);
        finish_bt.setOnClickListener(buttonClickListener);

        //리스트뷰 클릭 리스너 연결
        listView.setOnItemClickListener(ListViewClickListener);
    }

    //스피너 셀렉트 리스너
    Spinner.OnItemSelectedListener spinnerSelctedListener = new AdapterView.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String type = (String) typeSpinner.getAdapter().getItem(typeSpinner.getSelectedItemPosition());
            String time = (String) timeSpinner.getAdapter().getItem(timeSpinner.getSelectedItemPosition());
            //각 스피너 선택 범주에 맞는 데이터를 리스트뷰에 보여준다.(N(nop)일 경우 전체 출력)
            if(typeSpinner.getSelectedItemPosition() == 0 && timeSpinner.getSelectedItemPosition() == 0){
                refreshList(N,N,false);
            }
            else if(typeSpinner.getSelectedItemPosition() == 0){
                refreshList(N,time,false);
            }
            else if(timeSpinner.getSelectedItemPosition() == 0){
                Log.d("타입전달?",type);
                refreshList(type, N,false);
            }
            else
            {
                refreshList(type,time,false);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    //버튼 클릭 리스너
    Button.OnClickListener buttonClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            Intent intent;
            switch (v.getId())
            {
                //추가 버튼
                case R.id.add:
                    intent = new Intent(getApplicationContext(),AddActivity.class);
                    startActivityForResult(intent, ADD_REQUEST);
                    break;
                //통계 버튼
                case R.id.stats:
                    intent = new Intent(getApplicationContext(),StatsActivity.class);
                    ArrayList<String> typeArr = new ArrayList<String>();
                    ArrayList<String> dateArr = new ArrayList<String>();
                    ArrayList<String> typeInfoArr = new ArrayList<String>();
                    initRefreshList();
                    for(int a=0; a<IdIndex.size(); a++)
                    {
                        typeArr.add(setType(IdIndex.get(a)));
                        dateArr.add(setDate(IdIndex.get(a)));
                    }
                    for(int b=0; b < adapterType.getCount(); b++)
                    {
                        typeInfoArr.add(adapterType.getItem(b).toString());
                    }
                    intent.putExtra("총종류",typeArr);
                    intent.putExtra("총시간",dateArr);
                    intent.putExtra("종류",typeInfoArr);
                    intent.putExtra("시간",timeArray);
                    startActivity(intent);
                    break;
                //종료 버튼 (종료 팝업 사용)
                case R.id.finish:
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("종료하시겠습니까?")
                            .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    break;
            }
        }
    };

    //리스트뷰 클릭 리스너(선택한 리스트 뷰에 대한 정보를 다른 액티비티로 보여준다.)
    ListView.OnItemClickListener ListViewClickListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getApplicationContext(),InformationActivity.class);
            Log.d(position+"","?");
            Log.d(IdIndex.get(position)+"","?");
            String date = setDate(IdIndex.get(position));
            String time = setTime(IdIndex.get(position));
            String type = setType(IdIndex.get(position));
            String event = setEvent(IdIndex.get(position));
            Double lng = setLng(IdIndex.get(position));
            Double lat = setLat(IdIndex.get(position));
            intent.putExtra("날짜", date);
            intent.putExtra("시간", time);
            intent.putExtra("종류", type);
            intent.putExtra("내용", event);
            intent.putExtra("lng", lng);
            intent.putExtra("lat",lat);
            intent.putExtra("index",IdIndex.get(position));
            startActivityForResult(intent, INFORMATION_REQUEST);
        }
    };

    //인텐트 재반환
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        //DB 추가시
        if(requestCode == ADD_REQUEST && resultCode == RESULT_OK){
            insertData(data.getStringExtra("날짜"), data.getStringExtra("시간"), data.getStringExtra("종류"), data.getStringExtra("내용"), data.getDoubleExtra("lng", 0), data.getDoubleExtra("lat", 0));
            initRefreshList();
            Toast.makeText(getApplicationContext(),"추가 되었습니다.",Toast.LENGTH_SHORT).show();
        }
        //DB 삭제시
        if(requestCode == INFORMATION_REQUEST && resultCode == RESULT_OK){
            removeData(data.getIntExtra("index", 0));
            initRefreshList();
            Toast.makeText(getApplicationContext(),"삭제 되었습니다.",Toast.LENGTH_SHORT).show();
        }
    }

    //sqlite 활용 함수
    //테이블 생성
    public void createTable(){
        try {
            DB.execSQL("create table " + tableName + "(id integer primary key autoincrement, day text, time text, type text, event text, longitude real, latitude real);");
        } catch (android.database.sqlite.SQLiteException e){
            Log.d("SQlite","에러 발생 : " + e);
        }
    }

    //DB 추가 함수
    public void insertData(String day, String time, String text, String event, Double lng, Double lat) {
        String sql = "insert into " + tableName + " values(NULL, '" + day + "','" + time + "','" + text + "','" + event + "','" + lng + "','" + lat + "');";
        Log.d("open ? ",sql);
        DB.execSQL(sql);
    }

    //DB 삭제 함수
    public void removeData(int index) {
        String sql = "delete from " + tableName + " where id = " + index + ";";
        DB.execSQL(sql);
    }

    //DB 검색 함수들
    public String setDate(int index) {
        String date;
        String sql = "select * from " + tableName + " where id = " + index + ";";
        Cursor result = DB.rawQuery(sql, null);
        result.moveToFirst();
        date = result.getString(1);
        result.close();
        return date;
    }
    public String setTime(int index) {
        String time;
        String sql = "select * from " + tableName + " where id = " + index + ";";
        Cursor result = DB.rawQuery(sql, null);
        result.moveToFirst();
        time = result.getString(2);
        result.close();
        return time;
    }
    public String setType(int index) {
        String type;
        String sql = "select * from " + tableName + " where id = " + index + ";";
        Cursor result = DB.rawQuery(sql, null);
        result.moveToFirst();
        type = result.getString(3);
        result.close();
        return type;
    }
    public String setEvent(int index) {
        String event;
        String sql = "select * from " + tableName + " where id = " + index + ";";
        Cursor result = DB.rawQuery(sql, null);
        result.moveToFirst();
        event = result.getString(4);
        result.close();
        return event;
    }
    public Double setLng(int index) {
        Double lng;
        String sql = "select * from " + tableName + " where id = " + index + ";";
        Cursor result = DB.rawQuery(sql, null);
        result.moveToFirst();
        lng = Double.parseDouble(result.getString(5));
        result.close();
        return lng;
    }
    public Double setLat(int index) {
        Double lat;
        String sql = "select * from " + tableName + " where id = " + index + ";";
        Cursor result = DB.rawQuery(sql, null);
        result.moveToFirst();
        lat = Double.parseDouble(result.getString(6));
        result.close();
        return lat;
    }


    //DB 리스트 출력 함수(isTimeArraySetting 변수는 time 스피너의 값을 재설정 여부를 확인하는 boolean 변수)
    public void showList(String selectedEvent, String selectedDate, boolean isTimeArraySetting) {
        String sql = "select * from " + tableName + ";";
        Cursor results = DB.rawQuery(sql, null);
        results.moveToLast();
        IdIndex.clear();

        int index=0;
        if(isTimeArraySetting)
        {
            timeArray.clear();
            timeArray.add("시간선택");
        }

        while (!results.isBeforeFirst()) {
            int id = results.getInt(0);
            String date = results.getString(1);
            String time = results.getString(2);
            String type = results.getString(3);
            String event = results.getString(4);
            String location = results.getDouble(5) + "," + results.getDouble(6);
            Log.d(event,selectedEvent);
            if(selectedDate.equals("nop") || selectedDate.equals(date)) {
                if(selectedEvent.equals("nop") || selectedEvent.equals(type)) {
                    String name =  date;
                    name += " " + time;
                    name += ", 종류 : " + type +"\r\n";
                    name += ", 내용 : " + event;
                    name += ", 위치 : " + location;
//            Toast.makeText(this, "index= " + id + " name=" + name, Toast.LENGTH_LONG).show();
                    Log.d("lab_sqlite", "index= " + id + " name=" + name);

                    eventList.add(name);
                    IdIndex.add(id);
                }
            }
            if(isTimeArraySetting)
            {
                if(index==0 || !timeArray.get(index).equals(date)) {
                    timeArray.add(date);
                    index++;
                }
            }
            results.moveToPrevious();
        }
        if(isTimeArraySetting)
            adapterTime.notifyDataSetChanged();

        results.close();
    }

    //ListViewr 갱신함수
    public void refreshList(String selectedEvent, String selectedDate, boolean isTimeSetting){
        eventList.clear();
        showList(selectedEvent, selectedDate, isTimeSetting);
        baseAdapter.notifyDataSetChanged();
    }

    //ListView 초기화 함수
    public void initRefreshList()
    {
        refreshList(N,N,true);
        typeSpinner.setSelection(0);
        timeSpinner.setSelection(0);
    }
}