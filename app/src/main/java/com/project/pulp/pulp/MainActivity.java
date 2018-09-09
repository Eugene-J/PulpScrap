package com.project.pulp.pulp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Button btn;
    LinearLayout layout;
    myDBHelper myDBHelper;
    SQLiteDatabase sqLiteDatabase;
    String count; // 총 폴더의 갯수
    int countNum; // 총 폴더의 갯수
    int layoutSize = 2; //한 줄당 폴더 갯수
    int layoutCount; //레이아웃 갯수
    int mode = 1;
    int floderNum;
    String folderName;
    String imagePath=null;//갤러리에서 들고오는 사진경로

    public static Context mContext;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0,1,0,"갤러리로 정렬");
        menu.add(0,2,0,"텍스트로 정렬");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case 1 :
                //갤러리모드로 정렬
                layout.removeAllViewsInLayout();
                GallaryMode gallaryMode = new GallaryMode();
                gallaryMode.run();
                mode=1;
                return true;
            case 2 :
                //텍스트모드로 정렬
                layout.removeAllViewsInLayout();
                TextView title = new TextView(MainActivity.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                params.setMargins(10,10,10,30);
                title.setText("폴더 리스트");
                title.setTextSize(50);
                title.setLayoutParams(params);
                layout.addView(title);
                TextMode textMode = new TextMode();
                textMode.run();
                mode = 2;
                return true;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        myDBHelper = new myDBHelper(this);
        //레이아웃
        layout = (LinearLayout)findViewById(R.id.layout);
        //입력창
        final AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
        final AlertDialog add = ad.create();
        add.setTitle("새로 만들기"); //제목 설정
        add.setMessage("폴더명을 적어주세요");//내용 설정

        final EditText et = new EditText(MainActivity.this);//edittext삽입하기
        add.setView(et);

        add.setButton(DialogInterface.BUTTON_NEGATIVE, "닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();//닫기
            }
        });

        add.setButton(DialogInterface.BUTTON_POSITIVE, "설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Text 값 받아서 텍스트뷰에 넣기
                String value = et.getText().toString();
                dialogInterface.dismiss();//닫기

                //디비에 입력
                sqLiteDatabase = myDBHelper.getWritableDatabase();
                sqLiteDatabase.execSQL("insert into folder (subject) values"+"('"+value+"')");
                sqLiteDatabase.execSQL("create table IF NOT EXISTS scrap " +
                        "(subject INTEGER, num INTEGER, photo char(500), memo char(100));");


                //다시 정렬
                if(mode==2){
                    layout.removeAllViewsInLayout();
                    TextView title = new TextView(MainActivity.this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    params.setMargins(10,10,10,30);
                    title.setText("폴더 리스트");
                    title.setTextSize(50);
                    title.setLayoutParams(params);
                    layout.addView(title);
                    TextMode textMode = new TextMode();
                    textMode.run();
                }else {
                    layout.removeAllViewsInLayout();
                    GallaryMode gallaryMode = new GallaryMode();
                    gallaryMode.run();
                }

            }
        });
        //입력창

        //New버튼
        btn = (Button)findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add.show();
            }
        });

        //초기화면 갤러리모드로 정렬
        GallaryMode gallaryMode = new GallaryMode();
        gallaryMode.run();

    }//oncreate

    public class myDBHelper extends SQLiteOpenHelper {
        public myDBHelper(Context context){
            super(context,"pulp",null,1);
        }
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("create table IF NOT EXISTS folder(num INTEGER PRIMARY KEY AUTOINCREMENT,subject char(10));");
            sqLiteDatabase.execSQL("create table IF NOT EXISTS scrap " +
                    "(subject INTEGER references folder(num), num INTEGER, photo char(500), memo char(100));");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("drop table if exists folder");
            onCreate(sqLiteDatabase);
        }

        public int folderNum(String subject){
            sqLiteDatabase = getReadableDatabase();
            Cursor cursor;
            cursor = sqLiteDatabase.rawQuery("select num from folder where subject='\"+subject.getText().toString()+\"'", null);

            while (cursor.moveToNext()){
                count = cursor.getString(0);
            }
            int viewNum = Integer.parseInt(count);
            return viewNum;
        }

        public int maxNum (){
            sqLiteDatabase = getReadableDatabase();
            Cursor cursor;
            cursor = sqLiteDatabase.rawQuery("select max(num) from folder", null);
            int maxNum=0;
            while(cursor.moveToNext()){
                maxNum = cursor.getInt(0);
            }
            return maxNum;
        }

    }//end class

    public class GallaryMode {
        public void run(){
            //폴더 뿌려주기
            sqLiteDatabase = myDBHelper.getReadableDatabase();
            Cursor cursor;
            cursor = sqLiteDatabase.rawQuery("select count(*) as count from folder",null);
            while (cursor.moveToNext()){
                count = cursor.getString(0);
            }

            countNum = Integer.parseInt(count);
            //Log.v("coun", ""+countNum);
            if(countNum!=0) {
                layoutCount = countNum/layoutSize;
                layoutCount = (countNum % layoutSize) != 0 ? layoutCount + 1 : layoutCount;
                Log.v("lay", ""+layoutCount);
                for (int i = 0; i < layoutCount; i++) {

                    LinearLayout linearLayout = new LinearLayout(MainActivity.this); //폴더를 담는 리니어레이아웃
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
                            ,LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(10,10,10,10);
                    linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                    linearLayout.setLayoutParams(layoutParams);
                    int currentLayout = i + 1;
                    int startRow = (currentLayout-1)*layoutSize+1-1;//startRow-1해서 대입
                    cursor = sqLiteDatabase.rawQuery("select subject, num from folder order by num desc LIMIT "+startRow+","+layoutSize,null);

                    while (cursor.moveToNext()){

                        //폴더명 띄우기
                        folderName = cursor.getString(0);
                        floderNum = cursor.getInt(1);
                        TextView txt = new TextView(MainActivity.this);
                        txt.setText(folderName);
                        txt.setTextSize(30);
                        txt.setTypeface(Typeface.SANS_SERIF,Typeface.BOLD);

                        //폴더 이미지 넣기
                        ImageView imageView = new ImageView(MainActivity.this);

                        //폴더 띄우기
                        DisplayMetrics metrics = new DisplayMetrics();
                        WindowManager windowManager = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                        windowManager.getDefaultDisplay().getMetrics(metrics);
                        final int margin = metrics.widthPixels/20;
                        final int width = (metrics.widthPixels-margin)/2;
                        final int height = width;

                        RelativeLayout relativeLayout = new RelativeLayout(MainActivity.this);
                        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(width,height);
                        RelativeLayout.LayoutParams param2 = new RelativeLayout.LayoutParams(width,height);
                        RelativeLayout.LayoutParams param3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        param.setMargins(margin/4,margin/4,margin/4,margin/4);
                        param3.addRule(RelativeLayout.CENTER_IN_PARENT);
                        param2.setMargins(margin/4,margin/4,margin/4,margin/4);
                        relativeLayout.setLayoutParams(param);
                        imageView.setLayoutParams(param2);
                        txt.setLayoutParams(param3);
                        relativeLayout.setGravity(Gravity.CENTER);
                        relativeLayout.setTag(floderNum);

                        imagePath = folderImg(relativeLayout);

                        if (imagePath==null) {

                            Drawable image = getResources().getDrawable(R.drawable.pic2);
                            image.setAlpha(100);
                            imageView.setImageDrawable(image);
                            imageView.setBackgroundResource(R.drawable.image_border);
                            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                        } else {
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환
                            imageView.setImageBitmap(bitmap);
                        }

                        relativeLayout.addView(imageView);
                        relativeLayout.addView(txt);
                        linearLayout.addView(relativeLayout);

                        relativeLayout.setOnClickListener(
                                new pageNumClick()
                        );

                    }//while

                    layout.addView(linearLayout);

                }//for
            }//end if
            cursor.close();
            sqLiteDatabase.close();
        }
    }//end class

    public class TextMode{
        public void run(){
            sqLiteDatabase = myDBHelper.getReadableDatabase();
            Cursor cursor;
            cursor = sqLiteDatabase.rawQuery("select subject,num from folder order by num desc",null);
            while (cursor.moveToNext()){
                String folderName = cursor.getString(0);
                int floderNum2 = cursor.getInt(1);
                TextView txt = new TextView(MainActivity.this);
                txt.setText("  " + folderName);
                txt.setTextSize(30);
                txt.setTypeface(Typeface.SANS_SERIF,Typeface.NORMAL);
                txt.setPadding(30,30,30,30);
                txt.setBackgroundResource(R.drawable.textview_border);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                params.setMargins(10,10,10,50);
                txt.setLayoutParams(params);

                txt.setTag(floderNum2);
                txt.setOnClickListener(
                        new pageNumClick()
                );
                layout.addView(txt);

            }
        }
    }//end class




    public class pageNumClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), AlbumActivity.class);
            intent.putExtra("folderNum", (Integer)v.getTag());
            startActivity(intent);
        }
    }


    public String folderImg(RelativeLayout r){
        String imagePath=null;
        MainActivity m = new MainActivity();
        int folderNum = (Integer)r.getTag();

        Cursor cursor = sqLiteDatabase.rawQuery("select photo from scrap where num=(select max(num) from scrap where subject="+folderNum+") and subject="+folderNum, null);
        while(cursor.moveToNext()){
            imagePath=cursor.getString(0);
        }

        return imagePath;
    }

}
