package com.example.androidportpolio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;

public class UpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        try{
            //로그인 된 경우 처리
            FileInputStream fis = openFileInput("login.txt");
            byte [] b = new byte[fis.available()];
            int length = fis.read(b);
            String str = new String(b,0,length);
            String [] ar = str.split(":");
            Toast.makeText(this,ar[0],Toast.LENGTH_LONG).show();
        }catch (Exception e){
            //로그인 안된 경우 처리
            Log.e("파일 읽기 에러",e.getMessage());
        }

    }
}