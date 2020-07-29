package com.example.androidportpolio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HtmlParsingActivity extends AppCompatActivity {
    TextView display;

    class ThreadEx extends Thread{
        StringBuilder sb = new StringBuilder();
        public void run(){
            try{
                URL url = new URL("https://finance.naver.com/");
                HttpURLConnection con =
                        (HttpURLConnection)url.openConnection();
                con.setUseCaches(false);
                con.setConnectTimeout(30000);

                //문자열 읽는 객체를 생성
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(
                                        con.getInputStream(),"EUC-KR"));

                while(true){
                    String line = br.readLine();
                    if(line == null){
                        break;
                    }
                    sb.append(line + "\n");
                }
                br.close();
                con.disconnect();
            }catch(Exception e){
                Log.e("다운로드 예외", e.getMessage());
            }
            try{
                //HTML 파싱
                //html을 전부 펼쳐서 DOM 객체로 생성
                Document doc = Jsoup.parse(sb.toString());
                //원하는 항목 가져오기
                Elements elements = doc.select("a");
                String result = "";
                for(Element element : elements){
                    result += element.text();
                    result += ":" +
                            element.attr("href");
                    result += "\n";
                }
                //출력하기 위해서 핸들러를 호출
                Message message = new Message();
                message.obj = result;
                handler.sendMessage(message);

            }catch(Exception e){
                Log.e("파싱 예외", e.getMessage());
            }
        }
    }

    Handler handler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message message){
            String result = (String)message.obj;
            display.setText(result);
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html_parsing);

        display = (TextView)findViewById(R.id.display);

    }
    @Override
    public void onResume(){
        super.onResume();
        new ThreadEx().start();
    }
}

