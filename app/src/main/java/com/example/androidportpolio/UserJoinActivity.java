package com.example.androidportpolio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserJoinActivity extends AppCompatActivity {
    EditText emailInput, pwinput, nameinput;
    Button btnjoin, btnlogin, btnmain;
    Spinner gender;

    private ArrayAdapter<CharSequence> adapter;

    class ThreadEx extends Thread {
        @Override
        public void run() {
            //데이터 유효성 검사를 하지 말고 오직 서버와 연결된 작업만 실행하는 것이 좋다.
            StringBuilder sb = new StringBuilder();
            //서버와 연동 작업
            try {
                URL url = new URL("http://192.168.0.117:9000/user/register");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                //옵션 설정
                con.setRequestMethod("POST");
                con.setUseCaches(false);
                con.setConnectTimeout(30000);
                con.setDoInput(true);
                con.setDoOutput(true);

                //파일 전송을 위한 설정
                String boundary = UUID.randomUUID().toString();
                con.setRequestProperty("ENCTYPE","multipart/form-data");
                con.setRequestProperty("Content-Type","multipart/form-data;boundary="+boundary);

                //파라미터 만들기
                String lineEnd = "\r\n";
                String [] data = {emailInput.getText().toString().trim(),nameinput.getText().toString().trim(),pwinput.getText().toString().trim(),gender.getSelectedItem().toString()};
                String [] dataName = {"user_email","user_name","user_password","user_gender"};

                //파라미터 전송
                String delimiter = "--"+boundary+lineEnd;
                StringBuffer postDataBuilder = new StringBuffer();
                for(int i=0;i<data.length;i=i+1){
                    postDataBuilder.append(delimiter);
                    postDataBuilder.append("Content-Disposition: form-data; name=\""+dataName[i]+"\""+ lineEnd + lineEnd + data[i] + lineEnd);
                }
                //파일 파라미터 만들기
                String fileName = "ball.png";
                if(fileName != null){
                    postDataBuilder.append(delimiter);
                    postDataBuilder.append("Content-Disposition: form-data; name=\""+"activity_image"+"\";filename=\""+fileName+"\""+lineEnd);
                }

                DataOutputStream ds = new DataOutputStream(con.getOutputStream());
                ds.write(postDataBuilder.toString().getBytes());

                //파일 업로드
                if(fileName != null){
                    ds.writeBytes(lineEnd);
                    //파일 읽어오기 -id에 해당하는 파일을 raw 디렉토리에 복사
                    InputStream fres = getResources().openRawResource(R.raw.ball);
                    byte [] buffer = new byte[fres.available()];
                    int length = -1;
                    //파일의 내용을 읽어서 읽은 내용이 있으면 그 내용을 ds에 기록
                    while ((length = fres.read(buffer)) != -1){
                        ds.write(buffer,0,length);
                    }
                    ds.writeBytes(lineEnd);
                    ds.writeBytes(lineEnd);
                    ds.writeBytes("--"+boundary+"--"+lineEnd);
                    fres.close();
                }else{
                    ds.writeBytes(lineEnd);
                    ds.writeBytes("--"+boundary+"--"+lineEnd);
                }
                ds.flush();
                ds.close();

                //결과 가져오기
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                while(true){
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line+"\n");
                }
                br.close();
                con.disconnect();
                Log.e("다운로드 받은 문자열",sb.toString());

            } catch (Exception e) {
                Log.e("다운로드 에러", e.getMessage());
            }

            //다운로드 받은 데이터를 파싱
            Map<String,Object> map = new HashMap<>();
            try {
                JSONObject object = new JSONObject(sb.toString());
                boolean result = object.getBoolean("result");
                boolean emailcheck = object.getBoolean("emailcheck");

                map.put("result",result);
                map.put("emailcheck",emailcheck);


            }catch (Exception e){
                Log.e("파싱 에러",e.getMessage());
            }


            Message message = new Message();
            message.obj = map;
            handler.sendMessage(message);

        }
    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Map<String,Object> map = (Map<String, Object>) msg.obj;
            Boolean result = (boolean) map.get("result");

            if(result){
                Toast.makeText(UserJoinActivity.this,"회원가입 성공",Toast.LENGTH_LONG).show();
            }else{
                boolean emailcheck = (boolean) map.get("emailcheck");
                if(emailcheck==false){
                    Toast.makeText(UserJoinActivity.this,"이메일 중복",Toast.LENGTH_LONG).show();
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_join);

        gender = (Spinner) findViewById(R.id.gender);
        adapter = ArrayAdapter.createFromResource(
                this,R.array.gender,
                android.R.layout.simple_spinner_dropdown_item
        );
        gender.setAdapter(adapter);
        emailInput = (EditText) findViewById(R.id.emailinput);
        pwinput = (EditText) findViewById(R.id.pwinput);
        nameinput = (EditText) findViewById(R.id.nameinput);



        btnjoin = (Button) findViewById(R.id.btnjoin);
        btnlogin = (Button) findViewById(R.id.btnlogin);
        btnmain = (Button) findViewById(R.id.btnmain);

        btnjoin.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //데이터 유효성 검사
                boolean result = validationCheck();

                //서버와 연동
                new ThreadEx().start();
            }
        });
    }

    //유효성 검사
    private boolean validationCheck() {
        boolean result = false;
        String user_email = emailInput.getText().toString().trim();
        String user_password = pwinput.getText().toString().trim();
        String user_name = nameinput.getText().toString().trim();

        //필수 입력 체크와 정규식을 체크
        String msg = null;
        if (user_email.length() < 1) {
            msg = "이메일은 필수 입력 입니다.";
        } else {
            //정규식 객체 생성
            //숫자나 문자로 시작해야 되고 +가 붙으면 몇번이 와도 상관 없음. $는 끝나는 표시
            String regex = "^[_a-z0-9-]+(.[_a-z0-9]+)*@(?:\\w+\\.)+\\w+$";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(user_email);
            //정규식 패턴과 일치하지 않으면
            if (m.matches() == false) {
                msg = "email 형식이 맞지 않습니다.";
            }
        }
        if (user_password.length() < 1) {
            msg = "비밀번호는 필수 입력 입니다.";
        } else {
            //정규식 객체 생성
            //숫자나 문자로 시작해야 되고 +가 붙으면 몇번이 와도 상관 없음. $는 끝나는 표시
            String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%&])" +
                    "[A-Za-z\\d!@#$%&]{8,}";

            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(user_password);
            //정규식 패턴과 일치하지 않으면
            if (m.matches() == false) {
                msg = "비밀번호는 영문 대소문자 1개 숫자 1개 특수문자 1개 이상의 조합으로 만들어져야 합니다.";

            }

        }

        if (user_name.length() < 2) {
            msg = "이름은 두글자 이상 입력하셔야 합니다.";
        } else {
            //정규식 객체 생성
            //숫자나 문자로 시작해야 되고 +가 붙으면 몇번이 와도 상관 없음. $는 끝나는 표시
            String regex = "[0-9]|[a-z]|[A-Z]|[가-힣]";
            for(int i=0; i<user_name.length(); i=i+1){
                String ch = user_name.charAt(i) + "";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(ch);
                //정규식 패턴과 일치하지 않으면
                if(m.matches() == false){
                    msg = "이름은 숫자 그리고 영문자와 한글만 가능합니다.";
                    break;
                }

            }
        }
        if (msg==null){
            result = true;
        }else{
            Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
        }
        return result;
    }
}
