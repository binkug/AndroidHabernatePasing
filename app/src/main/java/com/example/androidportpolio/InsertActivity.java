package com.example.androidportpolio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class InsertActivity extends AppCompatActivity {
    EditText activity_subject,activity_type,activity_content;
    Button btninsert;



    class InsertThread extends Thread{
        @Override
        public void run() {

            try {
                //upload할 주소 만들기
                URL url = new URL("http://192.168.0.117:9000/activity/insert");
                //서버에게 넘겨줄 문자열 파라미터 생성
                //user_email을 설정해 준 이유는 아직 user_email을 받아 올 수 없어서 직접 설정해 줬다.
                String user_email = "PncQZK8LD1Lf+/LlORX+haAqcb9DeZQjrPRnoJYr8Skuf9FVm8LIqfTClXYdPeYXz6ndcI67b8vyW8ATC9uvH/NZ7U8=";
                String [] data = {user_email,activity_subject.getText().toString().trim(),activity_type.getText().toString().trim(),activity_content.getText().toString().trim()};
                String [] dataName = {"user_email","activity_subject","activity_type","activity_content"};

                Log.e("user_email",user_email);
                Log.e("activity_subject",activity_subject.getText().toString().trim());
                Log.e("activity_type",activity_type.getText().toString().trim());
                Log.e("activity_content",activity_content.getText().toString().trim());

                //파라미터 전송에 필요한 변수를 생성
                String lineEnd = "\r\n";
                //파일 업로드를 할 때는 boundary 값이 있어야 합니다.
                //랜덤하게 생성하는 것을 권장
                String boundary = UUID.randomUUID().toString();

                //업로드 옵션을 설정
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setConnectTimeout(30000);
                con.setUseCaches(false);
                con.setDoInput(true);
                con.setDoOutput(true);

                //파일 업로드 옵션 설정
                con.setRequestProperty("ENTYPE","multipart/form-data");
                con.setRequestProperty("Content-Type","multipart/form-data;boundary="+boundary);

                //문자열 파라미터를 전송
                String delimiter = "--"+boundary+lineEnd;
                StringBuffer postBataBuiler = new StringBuffer();
                for(int i=0;i<data.length;i=i+1){
                    postBataBuiler.append(delimiter);
                    postBataBuiler.append("Content-Disposition: form-data; name=\""+dataName[i]+"\""+ lineEnd + lineEnd + data[i] + lineEnd);
                }

                //업로드할 파일이 있는 경우에만 작성
                String fileName = "ball.png";
                if(fileName !=null){
                    postBataBuiler.append(delimiter);
                    postBataBuiler.append("Content-Disposition: form-data; name=\""+"activity_image"+"\";filename=\""+fileName+"\""+lineEnd);
                }
                //파라미터 전송
                DataOutputStream ds = new DataOutputStream(con.getOutputStream());
                ds.write(postBataBuiler.toString().getBytes());

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

                //서버로 부터의 응답 가져오기
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                while(true){
                    String line = br.readLine();
                    if(line == null){
                        break;
                    }
                    sb.append(line + "\n");
                }
                br.close();
                con.disconnect();

                //JSON 파싱
                JSONObject object = new JSONObject(sb.toString());
                boolean insert = object.getBoolean("insert");
                //핸들러에게 전송
                Message message = new Message();
                message.obj = insert;
                insertHandler.sendMessage(message);

            }catch (Exception e){
                Log.e("업로드 에러 에러",e.getMessage());
            }

        }
    }

    Handler insertHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            boolean insert = (boolean) msg.obj;
            if(insert == true){
                Toast.makeText(InsertActivity.this,"삽입 성공",Toast.LENGTH_LONG).show();
                //키보드 내리기
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(activity_subject.getWindowToken(),0);
                imm.hideSoftInputFromWindow(activity_type.getWindowToken(),0);
                imm.hideSoftInputFromWindow(activity_content.getWindowToken(),0);
            }else{
                Toast.makeText(InsertActivity.this,"삽입 실패",Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);
        activity_subject = (EditText)findViewById(R.id.activity_subject);
        activity_type = (EditText)findViewById(R.id.activity_type);
        activity_content = (EditText)findViewById(R.id.activity_content);
        btninsert = (Button) findViewById(R.id.btninsert);

        btninsert.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //유효성 검사
                if(activity_subject.getText().toString().trim().length() < 1){
                    Toast.makeText(InsertActivity.this,"제목을 입력해 주세요",Toast.LENGTH_LONG).show();
                    return;
                }
                if(activity_type.getText().toString().trim().length() < 1){
                    Toast.makeText(InsertActivity.this,"타입을 입력해 주세요",Toast.LENGTH_LONG).show();
                    return;
                }
                new InsertThread().start();
            }
        });
    }
}