package com.example.androidportpolio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XmlParsingActivity extends AppCompatActivity {
    TextView haniDisplay;

    //title 태그의 내용을 저장할 List
    ArrayList<String> titleList = new ArrayList<>();
    //link 태그의 내용을 저장할 List
    ArrayList<String> linkList = new ArrayList<>();

    //데이터를 다운로드 받아서 파싱할 스레드 클래
    class ThreadEx extends Thread{
        String xml;
        public void run(){
            //웹 서버에서 문자열 다운로드 받기
            try{
                //다운로드 받을 URL 생성
                URL url = new URL("http://www.hani.co.kr/rss/");

                //연결 객체 생성
                HttpURLConnection con =
                        (HttpURLConnection)url.openConnection();

                //연결 옵션을 설정
                con.setRequestMethod("GET");
                con.setConnectTimeout(30000);
                con.setUseCaches(false);
                con.setDoOutput(true);
                con.setDoInput(true);
                //파일을 업로드하는 코드가 있으면 설정을 추가

                //파라미터를 추가 - GET 일 때는 url에 바로 추가해도 됩니다.

                //다운로드 받기 - 문자열 : BufferedReader, 파일 : BuffredInputStream
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(
                                        con.getInputStream()));
                //문자열을 가지고 + 연산을 하면 메모리 낭비가 발생할 수 있어서
                //StringBuilder를 이용
                //문자열은 + 연산을 하면 현재 객체에 하는 것이 아니고 복사해서 수행
                StringBuilder sb = new StringBuilder();
                while(true){
                    String line = br.readLine();
                    if(line == null){
                        break;
                    }
                    //출력할 때 보기좋게 하기 위해서 \n을 추가
                    //실제 서비스를 할 때는 \n은 제거
                    sb.append(line + "\n");
                }
                //정리
                br.close();
                con.disconnect();
                //다운로드 받은 내용을 문자열로 변환
                xml = sb.toString();
                Log.e("xml", xml);

            }catch(Exception e){
                //이 예외가 보이면 권한 설정 부분과 URL을 확인
                Log.e("다운로드 예외", e.getMessage());
            }
            //파싱하는 부분
            try{
                if(xml != null){
                    //DOM 파싱을 위한 준비
                    DocumentBuilderFactory factory =
                            DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder =
                            factory.newDocumentBuilder();
                    InputStream inputStream =
                            new ByteArrayInputStream(
                                    xml.getBytes("utf-8"));
                    Document document = builder.parse(inputStream);
                    Element root = document.getDocumentElement();

                    //title 태그 전부 가져오기
                    NodeList titles = root.getElementsByTagName("title");
                    NodeList links = root.getElementsByTagName("link");
                    for(int i=1; i<titles.getLength(); i=i+1){
                        //각각의 태그에 접근해서 문자열을 추출해서 저장
                        Node title = titles.item(i);
                        Node text = title.getFirstChild();
                        titleList.add(text.getNodeValue());

                        Node link = links.item(i);
                        text = link.getFirstChild();
                        linkList.add(text.getNodeValue());
                    }
                    //핸들러에게 출력을 요청
                    Message message = new Message();
                    //전송할 데이터가 있으면 message.obj에 대입
                    handler.sendMessage(message);
                }
            }catch(Exception e){
                //이 예외가 보이면 파싱 알고리즘을 확인
                Log.e("파싱 예외", e.getMessage());
            }
        }
    }

    //파싱한 결과를 받아서 출력할 핸들러 객체
    Handler handler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message message){
            //titleList의 내용을 텍스트 뷰에 출력
            StringBuilder sb = new StringBuilder();
            for(String title : titleList){
                sb.append(title + "\n");
            }
            haniDisplay.setText(sb.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml_parsing);

        haniDisplay = (TextView)findViewById(R.id.hanidisplay);
    }

    @Override
    public void onResume(){
        super.onResume();
        //스레드 시작
        new ThreadEx().start();
    }
}