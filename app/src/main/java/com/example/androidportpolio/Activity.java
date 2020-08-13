package com.example.androidportpolio;

import java.util.Date;

public class Activity {
    public int activity_num; //게시판 번호
    public String user_email; //유저 이메일
    public String activity_subject; //활동 제목
    public String activity_type; //활동 타입
    public String user_name;
    public Date activity_start_date_local; //활동 시작 시간
    public Date activity_elapsed_time; //활동 경과 시간
    public String activity_content;  //활동 내용
    public Double activity_distance; //활동거리
    public int activity_intensity; //활동 강도
    public String activity_image; //활동 이미지
    public int activity_elev_gain; //활동고도


    //ListView는 객체를 데이터로 주입하면 toString의 결과를 셀에 출력하기 때문입니다.
    @Override
    public String toString() {
        return activity_subject;
    }
}
