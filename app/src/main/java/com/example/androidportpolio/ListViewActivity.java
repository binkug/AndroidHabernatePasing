package com.example.androidportpolio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class ListViewActivity extends AppCompatActivity {
    ListView listView;
    //출력할 데이터
    String [] data;
    //연결할 Adapter
    ArrayAdapter<CharSequence> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        data = new String[4];
        data[0] = "SI";
        data[1] = "SM";
        data[2] = "QA";
        data[3] = "DevOps";

        //출력할 뷰를 생성
        listView = (ListView)findViewById(R.id.listview);

        //adapter 생성
        //첫번째양는 출력을 위한 Context
        //두번째는 ListVIew의 행 모양
        //android.R.layout에 기본 모양이 제공
        //세번째는 출력할 데이터
        /*adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,data);*/

        adapter = ArrayAdapter.createFromResource(this,R.array.pl,android.R.layout.simple_spinner_dropdown_item);
        //view에 Adapter를 설정
        listView.setAdapter(adapter);

    }
}