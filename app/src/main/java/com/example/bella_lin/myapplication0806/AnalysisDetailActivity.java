package com.example.bella_lin.myapplication0806;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;

public class AnalysisDetailActivity extends AppCompatActivity {

    private String TAG = "AnalysisDetailActivity";
    private TextView mType, mTime, mName, mCal, mFist;
    private ImageView mPhoto;

    private Button mBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_detail);

        // 接intent資料
        Bundle bundle = getIntent().getExtras();
        String postype = bundle.getString("postype");
        String postime = bundle.getString("postime");
        String posname = bundle.getString("posname");
        String poscal = bundle.getString("poscal");
        String posfist = bundle.getString("posfist");
        String posphoto = bundle.getString("posphoto");

        Log.d(TAG, "posphoto: " + posphoto);

        mType = findViewById(R.id.tv_detail_type);
        mTime = findViewById(R.id.tv_detail_title);
        mName = findViewById(R.id.tv_detail_name);
        mCal = findViewById(R.id.tv_detail_calorie);
        mFist = findViewById(R.id.tv_detail_fist);
        mPhoto = findViewById(R.id.im_detail_photo);
        mBtn = findViewById(R.id.btn_detail_back);


        mType.setText(postype);
        mTime.setText(postime);
        mName.setText(posname);
        mCal.setText(poscal);
        mFist.setText(posfist);

        File imgFile = new File(posphoto);

        if(imgFile.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            mPhoto.setImageURI(Uri.fromFile(imgFile));

        }




        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }
}
