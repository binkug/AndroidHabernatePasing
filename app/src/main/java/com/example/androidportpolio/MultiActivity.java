package com.example.androidportpolio;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MultiActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<String> data;
    ArrayAdapter<String> adapter;

    EditText iteminput;
    Button addbtn,deletebtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi);

        listView =(ListView)findViewById(R.id.listview);
        data = new ArrayList<>();
        data.add("Oracle");
        data.add("Mysql");
        data.add("MongoDB");
        data.add("MS-SQL Server");

        //여러개 선택 가능한 모양으로 어댑터를 생성
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_multiple_choice,data);
        listView.setAdapter(adapter);
        //선 모양 설정
        listView.setDivider(new ColorDrawable(Color.RED));
        listView.setDividerHeight(3);
        //선택모드를 변경
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        iteminput = (EditText)findViewById(R.id.iteminput);
        addbtn = (Button)findViewById(R.id.addbtn);
        deletebtn = (Button)findViewById(R.id.deletebtn);

        addbtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //유효성 검사
                String item = iteminput.getText().toString().trim();
                if(item.length() < 1){
                    Toast.makeText(MultiActivity.this,"입력을 해야함",Toast.LENGTH_LONG).show();
                    return;
                }
                data.add(item);
                //ListView를 재출력
                adapter.notifyDataSetChanged();
                Toast.makeText(MultiActivity.this,"데이터 입력 성공!",Toast.LENGTH_LONG).show();
                //키보드 숨기기
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(iteminput.getWindowToken(),0);
                //입력 뷰를 초기화
                iteminput.setText("");
            }
        });

        deletebtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //listView에서 각 행에 대한 선택여부를 가져오기
                SparseBooleanArray sba = listView.getCheckedItemPositions();
                //여러개의 인덱스를 삭제할 때는 뒤에서부터 삭제해야 한다.
                for(int i=listView.getCount()-1 ; i>=0 ; i=i-1){
                    if(sba.get(i) == true){
                        data.remove(i);
                    }
                }
                //선택 해제
                listView.clearChoices();;
                adapter.notifyDataSetChanged();
            }
        });
    }
}