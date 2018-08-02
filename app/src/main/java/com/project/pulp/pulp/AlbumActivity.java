package com.project.pulp.pulp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AlbumActivity extends AppCompatActivity {


    // activity_swipe.xml 뷰 페이지 변수 선언
    ViewPager pager;

    // 변수값 받아오기
    TextView albumMemo;

    // SQLite 변수 생성
    SQLiteDatabase sqliteDatabase;
    DBHelper dbHelper;

    // DB 컬럼 개수
    String count;
    int countNum, swipeNum;

    // Intent 받아온 num 값
    int folderNum=3;

    // DB maxNum 구하는 메소드
    String stMaxNum;
    int MaxNum;

    Cursor cursor;

    //버튼 3가지
    ImageView btnplus, btnminus, btnlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // activity_swipe.xml 뷰 페이지 변수 값 받기
        pager = (ViewPager) findViewById(R.id.pager);

        Intent intent = getIntent();
        /*folderNum = intent.getIntExtra("folderNum", 1);*/
        Log.v("folderNum", folderNum+"");

        // 툴바 생성
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#ffff33")); //제목의 칼라
        toolbar.setSubtitle("PULP"); //부제목 넣기
        toolbar.setNavigationIcon(R.mipmap.ic_launcher); //제목앞에 아이콘 넣기
        setSupportActionBar(toolbar); //툴바를 액션바와 같게 만들어 준다.

        // DB객체 생성
        dbHelper = new DBHelper(this);

        // DB 개수 구하기
        sqliteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor;
        cursor = sqliteDatabase.rawQuery("select count(*) from scrap where subject="+folderNum+"",null);

        while (cursor.moveToNext()){
            countNum = cursor.getInt(0);
        }
        if(countNum==0){
            Intent insertMemo = new Intent(getApplicationContext(), PlusActivity.class);
            //insertMemo.putExtra("folderNum", 3);
            startActivity(insertMemo);
        }

        //ViewPager에 설정할 Adapter 객체 생성
        //ListView에서 사용하는 Adapter와 같은 역할.
        //다만. ViewPager로 스크롤 될 수 있도록 되어 있다는 것이 다름
        //PagerAdapter를 상속받은 CustomAdapter 객체 생성
        //CustomAdapter에게 LayoutInflater 객체 전달
        CustomAdapter adapter = new CustomAdapter(getLayoutInflater());

        //ViewPager에 Adapter 설정
        pager.setAdapter(adapter);

        btnplus=(ImageView) findViewById(R.id.btnplus);
        btnminus=(ImageView) findViewById(R.id.btnminus);
        btnlist=(ImageView) findViewById(R.id.btnlist);

        btnplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentGo= new Intent(getApplicationContext(), PlusActivity.class);
                startActivity(intentGo);
            }
        });

    } // end of onCreate 메소드

    // 생성한 툴바에 메뉴 넣어주기
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album, menu);
        return true;
    }


    // 메뉴 기능 넣어주기
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                Toast.makeText(this, "환경설정을 눌렀습니다.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_quit:
                Toast.makeText(this, "나가기를 눌렀습니다.", Toast.LENGTH_SHORT).show();
                break;
        }

        /* if 문으로 구현
        if (id == R.id.action_settings) {
            return true;
        } */

        return super.onOptionsItemSelected(item);
    } // end of Menu Option 설정

    public class CustomAdapter extends PagerAdapter {
        LayoutInflater inflater;

        public CustomAdapter(LayoutInflater inflater) {
            //전달 받은 LayoutInflater를 멤버변수로 전달
            this.inflater=inflater;
        }

        //PagerAdapter가 가지고 잇는 View의 개수를 리턴
        //보통 보여줘야하는 이미지 배열 데이터의 길이를 리턴
        @Override
        public int getCount() {
            return countNum; //이미지 개수 리턴(그림이 10개라서 10을 리턴)
        }

        //ViewPager가 현재 보여질 Item(View객체)를 생성할 필요가 있는 때 자동으로 호출
        //쉽게 말해, 스크롤을 통해 현재 보여져야 하는 View를 만들어냄.
        //첫번째 파라미터 : ViewPager
        //두번째 파라미터 : ViewPager가 보여줄 View의 위치(가장 처음부터 0,1,2,3...)
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view=null;
            String albuMemo=null;

            //새로운 View 객체를 Layoutinflater를 이용해서 생성
            //만들어질 View의 설계는 res폴더>>layout폴더>>viewpater_childview.xml 레이아웃 파일 사용
            view= inflater.inflate(R.layout.fragment_album, null);

            cursor = sqliteDatabase.rawQuery("select memo from scrap where num=((select max(num) from scrap)-"+position+") and subject="+folderNum+"", null);
            while (cursor.moveToNext()){
                albuMemo = cursor.getString(0);
            }
            //만들어진 View안에 있는 ImageView 객체 참조
            //위에서 inflated 되어 만들어진 view로부터 findViewById()를 해야 하는 것에 주의.
            ImageView img= (ImageView)view.findViewById(R.id.img_viewpager_childimage);
            TextView albumMemo = (TextView)view.findViewById(R.id.albumMemo);
            albumMemo.setText(albuMemo);

            //ImageView에 현재 position 번째에 해당하는 이미지를 보여주기 위한 작업
            //현재 position에 해당하는 이미지를 setting
            img.setImageResource(R.drawable.gametitle_01+position);



            //ViewPager에 만들어 낸 View 추가
            container.addView(view);

            //Image가 세팅된 View를 리턴

            return view;
        }

        //화면에 보이지 않은 View는파쾨를 해서 메모리를 관리함.
        //첫번째 파라미터 : ViewPager
        //두번째 파라미터 : 파괴될 View의 인덱스(가장 처음부터 0,1,2,3...)
        //세번째 파라미터 : 파괴될 객체(더 이상 보이지 않은 View 객체)
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //ViewPager에서 보이지 않는 View는 제거
            //세번째 파라미터가 View 객체 이지만 데이터 타입이 Object여서 형변환 실시
            container.removeView((View)object);
        }

        //instantiateItem() 메소드에서 리턴된 Ojbect가 View가  맞는지 확인하는 메소드
        @Override
        public boolean isViewFromObject(View v, Object obj) {
            return v==obj;
        }
    }


    // SQLite 메소드 설정
    public class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context){
            super(context,"pulp",null,1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            /* sqLiteDatabase.execSQL("create table IF NOT EXISTS scrap " +
                    "(subject INTEGER, num INTEGER PRIMARY KEY AUTOINCREMENT, photo BLOB, memo char(100));"); */
            sqLiteDatabase.execSQL("create table IF NOT EXISTS scrap " +
                    "(subject INTEGER, num INTEGER, photo char(500), memo char(100));");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("drop table if exists scrap");
            onCreate(sqLiteDatabase);
        }

        // num 최대값 구하는 메소드
        public int getMaxNum(){
            sqliteDatabase = getReadableDatabase();
            Cursor cursor;
            cursor = sqliteDatabase.rawQuery("select max(num) from scrap where subject="+folderNum+"", null);

            while (cursor.moveToNext()){
                stMaxNum = cursor.getString(0);
            }

            int MaxNum = Integer.parseInt(stMaxNum);

            if (MaxNum==0){
                MaxNum=1;
                return MaxNum;
            } else {
                MaxNum=+1;
                return MaxNum;
            }

        }


        // DB insert 메소드
        public void insertAlbum(String albuMemo, int maxNum){
            sqliteDatabase = getWritableDatabase();
            sqliteDatabase.execSQL("insert into scrap (subject, num, memo) values ("+folderNum+", "+maxNum+", '"+albuMemo+"');");
            Log.v("albuMemo", albuMemo);
        }


        // 처음에 DB 뿌려주는 메소드
        public Cursor getAlbum(int sectionNumber) {

            // 읽기가 가능하게 DB 열기
            sqliteDatabase = getReadableDatabase();
            Cursor cursor;

            // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
            cursor = sqliteDatabase.rawQuery("select memo from scrap where num=((select max(num) from scrap)-"+sectionNumber+") and subject="+folderNum+"", null);

            return cursor;

        }

        // DB 개수 구하는 메소드
        public int getCount(){

            // DB 개수 구하기
            sqliteDatabase = getReadableDatabase();
            Cursor cursor;
            cursor = sqliteDatabase.rawQuery("select count(*) from scrap where subject="+folderNum+"",null);

            while (cursor.moveToNext()){
                count = cursor.getString(0);
            }
            int viewNum = Integer.parseInt(count);
            return viewNum;

        }

    } // end of 디비 설정


} // ★ CLASS ★
