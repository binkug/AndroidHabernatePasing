package com.example.androidportpolio;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

public class JobAdapter extends BaseAdapter {
    Context context;
    List<Map<String,Object>> list;
    //생성자
    public JobAdapter(Context context, List<Map<String,Object>> list){
        this.context = context;
        this.list = list;
    }

    //행의 개수를
    //이 메소드에서 리턴한 값만큼 아래 메소드들을 호출
    @Override
    public int getCount() {
        return list.size();
    }

    //행의 항목을 만들어주는 메소드
    @Override
    public Object getItem(int i) {
        Map<String,Object> map = list.get(i);
        return map.get("title").toString();
    }
    //각 행의 아이디를 설정하는 메소드
    @Override
    public long getItemId(int i) {
        return i;
    }
    //첫번째 매개변수는 행 번호
    //두번째 매개변수가 출력할 뷰
    //세번째 매개변수는 항목이 출력되는 AdapterView
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final  int pos = i;

        if(view == null){
            //icontext.xml 파일을 전개해서 뷰로 생성
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.icontext,viewGroup,false);
        }
        if (i%2==0){
            view.setBackgroundColor(Color.RED);
        }else{
            view.setBackgroundColor(Color.YELLOW);
        }

        //이미지 출력
        ImageView image = (ImageView) view.findViewById(R.id.image);
        int imageid = (Integer) list.get(i).get("image");
        image.setImageResource(imageid);

        //텍스트 뷰
        TextView title = (TextView) view.findViewById(R.id.title);
        String titleText = (String) list.get(i).get("title");
        title.setText(titleText);

        //버튼
        Button btn = (Button) view.findViewById(R.id.content);
        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = (String) list.get(pos).get("content");
                Toast.makeText(context,content,Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }
}
