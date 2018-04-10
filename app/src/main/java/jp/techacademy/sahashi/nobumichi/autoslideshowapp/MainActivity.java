package jp.techacademy.sahashi.nobumichi.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Timer;
import android.os.Handler;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //権限関連
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    //ボタン
    Button mButton1;
    Button mButton2;
    Button mButton3;
    //画像
    ArrayList<Uri> mImageArrayList = new ArrayList<Uri>();
    int mIntAllCnt = 0;
    int mIntCnt = -1;
    //タイマー
    Timer mTimer;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton1 = (Button) findViewById(R.id.button1);
        mButton2 = (Button) findViewById(R.id.button2);
        mButton3 = (Button) findViewById(R.id.button3);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                mButton1.setVisibility(View.VISIBLE);
                mButton2.setVisibility(View.VISIBLE);
                mButton3.setVisibility(View.VISIBLE);
                try {
                    getContentsInfo();
                } catch (InterruptedException e) {
                    Toast toast = Toast.makeText(
                            this, "予期せぬエラーが発生しました。アプリを終了します。", Toast.LENGTH_SHORT);
                    toast.show();
                    finish();
                }
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            mButton1.setVisibility(View.VISIBLE);
            mButton2.setVisibility(View.VISIBLE);
            mButton3.setVisibility(View.VISIBLE);
            try {
                getContentsInfo();
            } catch (InterruptedException e) {
                Toast toast = Toast.makeText(
                        this, "予期せぬエラーが発生しました。アプリを終了します。", Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }
        }

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(this);
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(this);
        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try{
            switch (requestCode) {
                case PERMISSIONS_REQUEST_CODE:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        mButton1.setVisibility(View.VISIBLE);
                        mButton2.setVisibility(View.VISIBLE);
                        mButton3.setVisibility(View.VISIBLE);
                        getContentsInfo();
                    } else {
                        Toast toast = Toast.makeText(
                                this, "権限が許可されなかった為、アプリを終了します。", Toast.LENGTH_SHORT);
                        toast.show();
                        finish();
                    }
                    break;
            }
        }
        catch (Exception e){
            Toast toast = Toast.makeText(
                    this, "予期せぬエラーが発生しました。アプリを終了します。", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        try{
            if (v.getId() == R.id.button1) {
                // 前
                if (mIntAllCnt > 0){
                    ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
                    --mIntCnt;
                    if (mIntCnt <  0) {
                        mIntCnt = mIntAllCnt - 1;
                    }
                    imageVIew.setImageURI(mImageArrayList.get(mIntCnt));
                }

            } else if (v.getId() == R.id.button2) {
                // 停止/再生
                if (mButton2.getText().toString().equals("停止")){
                    mButton1.setEnabled(true);
                    mButton3.setEnabled(true);
                    mButton2.setText("再生");

                    //タイマー停止
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }

                } else if (mButton2.getText().toString().equals("再生")){
                    mButton1.setEnabled(false);
                    mButton3.setEnabled(false);
                    mButton2.setText("停止");

                    // タイマーの作成
                    mTimer = new Timer();
                    // タイマーの始動
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mIntAllCnt > 0){
                                        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
                                        ++mIntCnt;
                                        if (mIntCnt > mIntAllCnt - 1) {
                                            mIntCnt = 0;
                                        }
                                        imageVIew.setImageURI(mImageArrayList.get(mIntCnt));
                                    }
                                }
                            });
                        }
                    }, 0, 2000);    // 最初に始動させるまで 0秒、ループの間隔を 2秒 に設定
                }

            } else if (v.getId() == R.id.button3) {
                // 次
                if (mIntAllCnt > 0){
                    ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
                    ++mIntCnt;
                    if (mIntCnt > mIntAllCnt - 1) {
                        mIntCnt = 0;
                    }
                    imageVIew.setImageURI(mImageArrayList.get(mIntCnt));
                }
            }
        }
        catch (Exception e){
            Toast toast = Toast.makeText(
                    this, "予期せぬエラーが発生しました。", Toast.LENGTH_SHORT);
            toast.show();

        }
    }

    private void getContentsInfo() throws InterruptedException {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (cursor.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                mImageArrayList.add(imageUri);
                mIntAllCnt++;

            } while (cursor.moveToNext());
        }
        cursor.close();
    }


}
