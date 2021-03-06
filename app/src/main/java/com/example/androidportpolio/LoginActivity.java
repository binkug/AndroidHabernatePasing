package com.example.androidportpolio;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;

import java.io.FileOutputStream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    EditText emailinput, pwinput;
    Button btnLogin, btnJoin, btnMain;
    ImageView imgProfile;
    class ThreadEx extends Thread{
        //다운로드 받을 문자열을 저장할 변수
        StringBuilder sb = new StringBuilder();
        public void run(){
            try{
                URL url = new URL(
                        "http://192.168.0.117:9000/user/login");
                HttpURLConnection con =
                        (HttpURLConnection)url.openConnection();
                con.setUseCaches(false);
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setConnectTimeout(30000);

                //POST 설정
                con.setRequestMethod("POST");
                //파라미터 만들기
                String parameter =
                        URLEncoder.encode("user_email", "UTF-8")
                                + "=" + URLEncoder.encode(
                                emailinput.getText().toString().trim(),
                                "UTF-8")
                                + "&" + URLEncoder.encode("user_password", "UTF-8")
                                + "=" + URLEncoder.encode(
                                pwinput.getText().toString().trim(),"UTF-8"
                        );
                //파라미터 전송
                OutputStreamWriter os =
                        new OutputStreamWriter(con.getOutputStream());
                os.write(parameter);
                os.flush();

                //결과 가져오기
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(
                                        con.getInputStream()));
                while(true){
                    String line = br.readLine();
                    if(line == null){
                        break;
                    }
                    sb.append(line + "\n");
                }
                br.close();
                con.disconnect();
                Log.e("다운로드 받은 문자열", sb.toString());

            }catch (Exception e){
                Log.e("서버 연동 예외", e.getMessage());
            }

            try{
                Map<String, Object> map = new HashMap<>();
                JSONObject object = new JSONObject(sb.toString());

                map.put("result", (Boolean)object.getBoolean("result"));
                //로그인 성공한 경우에만 나머지 데이터를 읽어옵니다.
                if((Boolean)object.getBoolean("result") == true) {
                    //map.put("user_email", (String) object.getString("user_email"));
                    map.put("user_image", (String) object.getString("user_image"));
                    map.put("user_name", (String) object.getString("user_name"));
                }

                Message message = new Message();
                message.obj = map;
                handler.sendMessage(message);

            }catch (Exception e){
                Log.e("파싱 예외", e.getMessage());
            }
        }
    }

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message message){
            Map<String, Object> map =
                    (Map<String, Object>)message.obj;
            Boolean result = (Boolean)map.get("result");
            if(result == true){
                Toast.makeText(LoginActivity.this,"로그인 성공",Toast.LENGTH_LONG).show();
                //회원정보를 파일에 저장
                String user_email = (String)map.get("user_email");
                String user_name = (String)map.get("user_name");
                String user_image = (String)map.get("user_image");
                //파일에 저장할 문자열 생성
                String str = user_email + ":" + user_name + ":"
                        + user_image;
                try {
                    FileOutputStream fos = openFileOutput(
                            "login.txt", Context.MODE_PRIVATE);
                    fos.write(str.getBytes());
                    fos.flush();
                    fos.close();
                    //로그아웃이나 로그인 실패했을 때는
                    //delete("login.txt")를 호출
                    //login.txt가 존재하면 로그인 된 상태이고
                    //존재하지 않으면 로그인 이 안된 상태가 됩니다.
                }catch(Exception e){
                    Log.e("파일 저장 예외", e.getMessage());
                }

                //이미지 다운로드 받는 스레드 생성
                new ImageThread(user_image).start();

            }else{
                Toast.makeText(LoginActivity.this,
                        "로그인 실패",Toast.LENGTH_LONG).show();
            }
        }
    };

    //이미지 파일을 다운로드 받는 스레드
    class ImageThread extends Thread{
        String profile;
        public ImageThread(String profile){
            this.profile = profile;
        }

        public void run(){
            try{
                //파일 다운로드를 위한 스트림을 생성
                InputStream is =
                        new URL(
                                "http://192.168.0.117:9000" +
                                        "profile/"+ profile).openStream();
                //파일로 저장
                /*
                FileOutputStream fos = openFileOutput(
                        profile, Context.MODE_PRIVATE);
                while(true){
                    byte []  b = new byte[1024];
                    int length = is.read(b);
                    if(length <= 0){
                        break;
                    }
                    fos.write(b, 0, length);
                    fos.flush();
                }
                fos.close();
                is.close();
                */

                Bitmap bit = BitmapFactory.decodeStream(is);
                is.close();
                Message message = new Message();
                message.obj = bit;
                imageHandler.sendMessage(message);

            }catch(Exception e){
                Log.e("이미지 다운로드 실패", e.getMessage());
            }
        }
    }

    Handler imageHandler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message message){
            Bitmap bit = (Bitmap)message.obj;
            imgProfile.setImageBitmap(bit);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailinput = (EditText)findViewById(R.id.emailinput);
        pwinput = (EditText)findViewById(R.id.pwinput);

        imgProfile = (ImageView)findViewById(R.id.user_image);
        btnLogin = (Button)findViewById(R.id.btnlogin);
        btnMain = (Button)findViewById(R.id.btnmain);
        btnJoin = (Button)findViewById(R.id.btnjoin);

        btnLogin.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                //유효성 검사 작업 수행

                //서버에 요청
                new ThreadEx().start();
            }
        });

    }
}