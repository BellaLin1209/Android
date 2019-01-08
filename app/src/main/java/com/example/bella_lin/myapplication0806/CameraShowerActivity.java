package com.example.bella_lin.myapplication0806;

import java.io.IOError;
import java.io.IOException;
import java.text.NumberFormat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

/**
 * Fist Camera - CameraTakerActivity
 *
 * Developer: Bella Lin
 * Date: 2018/08/17
 * Description:
 * 自行撰寫影像辨識，利用HSV來大概辨識出基本色調。
 * 目前針對水果進行撰寫，可辨識出基本色調的水果。(如：蘋果.香蕉.芭樂.葡萄.橘子)
 *
 * HSV角度顏色表:
 * 0-60	    紅色
 * 60-120   黃色
 * 120-180	綠色
 * 180-240	青色
 * 240-300	藍色
 * 300-360	紅紫色
 **/


public class CameraShowerActivity extends Activity {

    String FILE_PATH, FOOD_PATH;
    ImageView showIMG;
    TextView txtData;

    Bitmap bmpOrigin, bmpSkin, bmpFood;
    int decrease = 4;
    int skinNum = 0, foodNum = 0;
    float fist = 0f;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 全螢幕顯示
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 不顯示標題列
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_food_camera_show);

        showIMG = (ImageView) findViewById(R.id.showIMG);
        txtData = (TextView) findViewById(R.id.txtData);
        // -------------------接收圖片路徑-------------------
        Bundle bundle = this.getIntent().getExtras();
        String path = bundle.getString("PATH");
        String path2 = bundle.getString("PATH2");
        FILE_PATH = CameraTakerActivity.SAVE_PICTURE_PATH + "/" + path + ".jpg";
        FOOD_PATH = CameraTakerActivity.SAVE_PICTURE_PATH + "/" + path2 + ".jpg";
        // ----------------------------------------------
        loadImage();// 載入圖片
        skin();
        food();



        //-- 有圖片intent傳值去InputActivity -----
        Intent intent = new Intent();
        intent.setClass(CameraShowerActivity.this, InputActivity.class);
        intent.putExtra("CemeraSuccess", true); //true0
        startActivity(intent);
        finish();
        //------------------------

        showIMG.setImageBitmap(bmpOrigin);// 顯示圖片到imageView

    }

    // 載入圖片
    private void loadImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = decrease;// 縮小圖片尺寸
        options.inPurgeable = true;// 讓java系統記憶體不足時先行回收部分的記憶體
        bmpOrigin = BitmapFactory.decodeFile(FILE_PATH, options);// 載入圖片到bmp
    }

    // 膚色偵測
    private void skin() {

        int R, G, B;
        int Y, Cb, Cr, T1, T2, T3, T4;
        int avgY = 0, avgCb = 0, avgCr = 0, avgT1 = 0, avgT2 = 0, avgT3 = 0, avgT4 = 0;
        int count = 0;
        int mArrayColor[] = null;
        int mArrayColorLengh = 0;
        int mBitmapWidth = bmpOrigin.getWidth();
        int mBitmapHeight = bmpOrigin.getHeight();

        try {
            bmpSkin = Bitmap.createBitmap(bmpOrigin.getWidth(),
                    bmpOrigin.getHeight(), Bitmap.Config.ARGB_4444);

//            System.out.println("mBitmapWidth:" + mBitmapWidth + ",mBitmapHeight:"
//                    + mBitmapHeight);

            mArrayColorLengh = mBitmapHeight * mBitmapWidth;
            mArrayColor = new int[mArrayColorLengh];

            for (int j = 0; j < mBitmapHeight; j++) {
                for (int i = 0; i < mBitmapWidth; i++) {

                    count = 0; //初始化
                    int color = bmpOrigin.getPixel(i, j);// 獲得Bitmap

                    // 圖片中每一個點的color顏色值
                    mArrayColor[count] = color;// 將顏色值存在一個陣列中 方便後面修改

                    R = Color.red(color);
                    G = Color.green(color);
                    B = Color.blue(color);

                    Y = (int) (R * 0.2989 + G * 0.5866 + B * 0.1145);// Y是亮度(Luminance)
                    Cb = (int) (0.5647 * (B - Y));// Cb是藍色色差
                    Cr = (int) (0.7132 * (R - Y));// Cr是紅色色差 Cr為正值時容易辨識成功

                    if (Y <= 128) {
                        T1 = -2 + ((256 - Y) / 16);// TEHA 1
                        T2 = 20 - ((256 - Y) / 16);// TEHA 1=2
                        T3 = 6;// TEHA 3
                        T4 = -8;// TEHA 4
                    } else {
                        T1 = 6;
                        T2 = 12;
                        T3 = 2 + (Y / 32);
                        T4 = -16 + (Y / 16);
                    }
                    avgY += Y;
                    avgCb += Cb;
                    avgCr += Cr;
                    avgT1 += T1;
                    avgT2 += T2;
                    avgT3 += T3;
                    avgT4 += T4;

                    if (Cr >= -2 * (Cb + 24) && Cr >= -1 * (Cb + 17)
                            && Cr >= -4 * (Cb + 32) && Cr >= 2.5 * (Cb + T1)
                            && Cr >= T3 && Cr >= -0.5 * (Cb - T4)
                            && Cr <= -1 * ((Cb - 220) / 6)
                            && Cr <= -1.34 * (Cb - T2)) {
//                       Log.d(TAG, " mArrayColor[" + count + "]" + mArrayColor[count]);
                        bmpSkin.setPixel(i, j, mArrayColor[count]);
                        skinNum++;
                    } else {
                        bmpSkin.setPixel(i, j, Color.BLACK);
                    }
                    count++;
                }
            }
        } catch (IOError E) {
            E.getStackTrace();
        }

//        //測試用
//        txtData.setText("avgY:" + String.valueOf(avgY / mArrayColorLengh)
//                + "\n" + "avgCb:" + String.valueOf(avgCb / mArrayColorLengh)
//                + "\n" + "avgCr:" + String.valueOf(avgCr / mArrayColorLengh)
//                + "\n" + "avgT1:" + String.valueOf(avgT1 / mArrayColorLengh)
//                + "\n" + "avgT2:" + String.valueOf(avgT2 / mArrayColorLengh)
//                + "\n" + "avgT3:" + String.valueOf(avgT3 / mArrayColorLengh)
//                + "\n" + "avgT4:" + String.valueOf(avgT4 / mArrayColorLengh)
//                + "\n" + "skinNum:" + String.valueOf(skinNum));

    }

    //食物辨識
    private void food() {
        int R, G, B;
        int count = 0;
        int guavaNum = 0, appleNum = 0, bananaNum = 0, grapeNum = 0, otherNum = 0;
        int mArrayColor[] = null;
        int mArrayColorLengh = 0;
        int mBitmapWidth = bmpOrigin.getWidth();
        int mBitmapHeight = bmpOrigin.getHeight();
        float[] HSV = new float[3];
        String fruit = "";

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = decrease;// 縮小圖片尺寸
        options.inPurgeable = true;// 讓系統記憶體不足時先行回收部分的記憶體
        bmpOrigin = BitmapFactory.decodeFile(FOOD_PATH, options);

        bmpFood = Bitmap.createBitmap(bmpOrigin.getWidth(),
                bmpOrigin.getHeight(), Bitmap.Config.ARGB_4444);

        mArrayColorLengh = mBitmapHeight * mBitmapWidth;
        mArrayColor = new int[mArrayColorLengh];

       Log.d(TAG, "food h=" + mBitmapHeight + " , w=" + mBitmapWidth);
        for (int j = 0; j < mBitmapHeight; j++) {
            for (int i = 0; i < mBitmapWidth; i++) {

                int color = bmpOrigin.getPixel(i, j);// 獲得Bitmap
                // 圖片中每一個點的color顏色值
                count = 0; //初始化
                mArrayColor[count] = color;// 將顏色值存在一個陣列中 方便後面修改

                R = Color.red(color);
                G = Color.green(color);
                B = Color.blue(color);
                Color.RGBToHSV(R, G, B, HSV);//HSV(Hue, Saturation, Value)

                //綠色
                if (HSV[0] >= 120 && HSV[0] <= 180 && HSV[1] <= 100 && HSV[2] <= 100) {
                    bmpFood.setPixel(i, j, mArrayColor[count]);
                    guavaNum++;
                }
                //紅色
                else if (HSV[0] >= 0 && HSV[0] <= 60 || HSV[0] >= 300
                        && HSV[0] <= 360 && HSV[1] <= 100 && HSV[2] <= 100) {
                    bmpFood.setPixel(i, j, mArrayColor[count]);
                    appleNum++;
                }
                //黃色
                else if (HSV[0] >= 60 && HSV[0] <= 120 && HSV[1] <= 100 && HSV[2] <= 100) {
                    bmpFood.setPixel(i, j, mArrayColor[count]);
                    bananaNum++;
                    guavaNum++;
                }
                //紫色
                else if (HSV[0] >= 300 && HSV[0] <= 360 && HSV[1] <= 100 && HSV[2] <= 100) {
                    bmpFood.setPixel(i, j, mArrayColor[count]);
                    grapeNum++;
                } else {
                    bmpFood.setPixel(i, j, Color.BLACK);
                    otherNum++;
                }

                count++;
            }
        }

        int[] colorNumArray = {appleNum, bananaNum, guavaNum, 0, 0, grapeNum, otherNum};//紅.黃.綠.青.藍.紅紫.其他
        int max = 0, iNum = 0;
        for (int i = 0; i < colorNumArray.length; i++) {
            if (colorNumArray[i] > max) {
                max = colorNumArray[i];
                iNum = i;
            }
        }

        switch (iNum) {
            case 0://紅
                foodNum = appleNum;
                fruit = "蘋果";
                break;
            case 1://黃
                foodNum = bananaNum;
                fruit = "香蕉";
                break;
            case 2://綠
                foodNum = guavaNum;
                fruit = "芭樂";
                break;
            case 3://青(淺藍色)
                foodNum = otherNum;
                fruit = "";
                Toast.makeText(getApplicationContext(),
                        "青色 但辨識不出請自行輸入",
                        Toast.LENGTH_SHORT).show();
                break;
            case 4://藍
                foodNum = otherNum;
                fruit = "";
                Toast.makeText(getApplicationContext(),
                        "藍色 但辨識不出請自行輸入",
                        Toast.LENGTH_SHORT).show();
                break;
            case 5://紅紫
                foodNum = grapeNum;
                fruit = "葡萄";
                break;
            case 6://其他(白.黑.灰等等)
                foodNum = otherNum;
                fruit = "";
                Toast.makeText(getApplicationContext(),
                        "雜色太多辨識不出請自行輸入",
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                foodNum = otherNum;
                fruit = "";
                Toast.makeText(getApplicationContext(),
                        "辨識不出請自行輸入",
                        Toast.LENGTH_SHORT).show();
                break;

        }

        Log.d(TAG, fruit + " , 分數: " + foodNum);
        fist = (float) (foodNum) / (float) (skinNum);


        if (fist > 10) {
            fist = 0;
        } else {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(1); // 小數後一位
           Log.d(TAG, "fist: " + fist);
            if (fist < 10) {
                fist = Float.valueOf(nf.format(fist));
            } else {
                fist = 1f;
            }

        }

        //記錄起來
        InputActivity.PREF_FoodRecord.edit().putString(InputActivity.prefFoodFist, String.valueOf(fist)).commit();
        InputActivity.PREF_FoodRecord.edit().putString(InputActivity.prefFoodName, fruit).commit();


//        txtData.setText(txtData.getText() + "\n" + "foodNum:"
//                + String.valueOf(foodNum) + "\n" + "fist:"
//                + String.valueOf(fist) + "\n" + "fruit: " + fruit);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        finish();
        super.onDestroy();
    }

}
