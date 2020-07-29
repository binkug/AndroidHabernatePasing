package com.example.androidportpolio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomCellUseActivity extends AppCompatActivity {
    ListView listView;

    //출력할 데이터
    ArrayList<Map<String,Object>> data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_cell_use);

        listView = (ListView)findViewById(R.id.listview);
        data = new ArrayList<>();

        Map<String,Object> map = new HashMap<>();
        map.put("image",R.mipmap.ic_launcher);
        map.put("title","SI");
        map.put("content","시스템 개발");
        data.add(map);

        map = new HashMap<>();
        map.put("image",R.mipmap.ic_launcher);
        map.put("title","SM");
        map.put("content","시스템 운영 유지보수 및 관리");
        data.add(map);

        map = new HashMap<>();
        map.put("image",R.mipmap.ic_launcher);
        map.put("title","QA");
        map.put("content","품질관리 및 테스트");
        data.add(map);

        map = new HashMap<>();
        map.put("image",R.mipmap.ic_launcher);
        map.put("title","DevOps");
        map.put("content","운영 환경 구축");
        data.add(map);

        //어댑터 생성
        JobAdapter adapter = new JobAdapter(this,data);
        listView.setAdapter(adapter);

    }


}