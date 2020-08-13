package com.example.androidportpolio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ActivitiyDetailActivity extends AppCompatActivity {
    //화면에 사용할 뷰
    TextView activity_subject,activity_content,activity_type;
    ImageView activity_image;


    //텍스트 데이터를 웹에서 다운로드 받아서 출력
    //다운로드 -> 파싱 -> 출력
    //다운로드는 스레드를 이용 출력은 핸들러를 이용해야 한다.

    //이미지 출력을 위한 핸들러
    Handler handlerImage = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {

            //스레드가 전달해준 데이터를 이미지 뷰에 출력
            Bitmap bitmap = (Bitmap) msg.obj;
            activity_image.setImageBitmap(bitmap);
        }
    };
    //이미지 다운로드를 위한 스레드
    class ImageThread extends Thread{
        String subject_image;

        public ImageThread(String subject_image){
            this.subject_image  = subject_image;
        }

        @Override
        public void run() {
            try {
                URL url = new URL("http://192.168.0.117:9000/img/"+subject_image);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setUseCaches(false);
                con.setConnectTimeout(30000);

                //이미지 출력
                InputStream is = url.openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                //Message에 저장
                Message message = new Message();
                message.obj = bitmap;
                handlerImage.sendMessage(message);
            }catch (Exception e){
                Log.e("이미지 다운로드 에러",e.getMessage());
            }
        }
    }


    //텍스트 데이터를 출력할 핸들러
    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            //넘어온 데이터 찾아오기
            Map<String,Object> map = (Map<String, Object>) msg.obj;
            //데이터 출력하기
            activity_subject.setText((String) map.get("activity_subject"));
            activity_content.setText((String) map.get("activity_content"));
            activity_type.setText((String) map.get("activity_type"));
            //이미지 파일명을 ImageThread에게 넘겨서 출력
            new ImageThread((String) map.get("activity_image")).start();


        }
    };


    //텍스트 데이터를 가져올 스레드 클래스
    class ThreadEx extends Thread{
        @Override
        public void run() {
            //텍스트 데이터 다운로드
            StringBuilder sb = new StringBuilder();
            try {
                //호출하는 인텐트 가져오기
                Intent intent = getIntent();
                //액티비티넘의 값을 정수로 가져오고 없을 때 1
                int activity_num = intent.getIntExtra("activity_num",1);
                //URL 만들기
                URL url = new URL("http://192.168.0.117:9000/activity/detail?activity_num="+activity_num);

                //Connection 객체 만들기
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(3000);
                con.setUseCaches(false);

                //스트림 객체 생성
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                //문자열 읽기
                while(true){
                    String line = br.readLine();
                    if(line == null){
                        break;
                    }
                    sb.append(line+"\n");
                }
                br.close();
                con.disconnect();

            }catch (Exception e){
                //이 메세지가 보이면 서버가 구동중인지 확인하고 URL은 제대로 입력했는지 확인
                Log.e("다운로드 에러",e.getMessage());
            }
            //데이터를 다운로드 받았는지 확인 할 수 있는 코드
            Log.e("다운로드 받은 문자열",sb.toString());

            try {
                //다운로드 받은 문자열에서 필요한 데이터 추출
                JSONObject object = new JSONObject(sb.toString());
                JSONObject activity = object.getJSONObject("activity");

                String activity_subject = activity.getString("activity_subject");
                String activity_type = activity.getString("activity_type");
                String activity_content = activity.getString("activity_content");
                String activity_image = activity.getString("activity_image");

                //4개의 데이터를 하나로 묶기
                Map<String,Object> map = new HashMap<String, Object>();
                map.put("activity_subject",activity_subject);
                map.put("activity_type",activity_type);
                map.put("activity_content",activity_content);
                map.put("activity_image",activity_image);

                //핸들러에게 데이터를 전송하고 호출
                Message message = new Message();
                message.obj = map;
                handler.sendMessage(message);

            }catch (Exception e){
                Log.e("파싱 에러",e.getMessage());
            }
        }
    }

    //확인용 메소드
    @Override
    public void onResume(){
        super.onResume();
        new ThreadEx().start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activitiy_detail);

        //디자인 한 뷰 찾아오기
        activity_subject = (TextView)findViewById(R.id.activity_subject);
        activity_content = (TextView)findViewById(R.id.activity_content);
        activity_type = (TextView)findViewById(R.id.activity_type);
        activity_image = (ImageView) findViewById(R.id.activity_image);

        Button backbtn = (Button)findViewById(R.id.backbtn);

        backbtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //현재 엑티비티 종료
                finish();
            }
        });
    }
}