package com.example.bella_lin.myapplication0806;

/**
 * Date:2018/08/06
 * Creator:Bella Lin
 * ProjectName:AppforDietManagement
 * Description:
 * 1.早、午、晚餐、其他，這四類分別呈現。
 * 2.另外再多一條曲線顯示單一天的總熱量
 * <p>
 * Date:2018/08/15
 * 完成：使用網路開放圖表分析資源-MPAndroidChart,此資源的圖表更加視覺化。
 **/


import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnalysisActivity extends MainActivity implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener, OnChartGestureListener, OnChartValueSelectedListener {

    // 變數宣告
    private static final String TAG = "AnalysisActivity";
    private Spinner mSpinMonth;
    private ListView mListMonth;
    private Switch mSwitch;
    private FrameLayout mViewChart, mViewList;
    SimpleAdapter listItemAdapter;
    ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>(1000);


    // 折線圖
    private LineChart mChart;
    private Typeface mTfLight;
    private Map<Integer, Float> mTotalCal = new LinkedHashMap<>(); //取得單日總熱量

    // 取得登入的使用者名
    private String mUserName = "";

    // SQLite
    private ArrayList<String[]> mRecordData = new ArrayList<>();
    private ArrayList<String[]> mRecordData2 = new ArrayList<>();

    //    private int mTotalCalore = 0;    // 計算單天總熱量用
    String dateY = "", dateM = "";//取得當下日期年及月

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);


        mUserName = getSharedPreferences(prefName, MODE_PRIVATE)
                .getString(prefAcc, "");

        // 取得時間
        getDate();

        // 宣告
        findViewById();

        // 取得SQLite資料
        callSql();


        // 折線圖設置
        setChart();

    }

    //宣告
    private void findViewById() {
        //宣告
        mViewChart = findViewById(R.id.view_chart);
        mViewChart.setVisibility(View.INVISIBLE);
        mViewList = findViewById(R.id.view_list);
        mViewList.setVisibility(View.VISIBLE);

        mSwitch = findViewById(R.id.switch_changeview);
        mSwitch.setOnCheckedChangeListener(this);
        mChart = findViewById(R.id.mChart);
        mSpinMonth = findViewById(R.id.spin_month);
        mSpinMonth.setOnItemSelectedListener(this);

        ArrayAdapter<String> cAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, getResources().getStringArray(R.array.analysis_month));
        cAdapter.setDropDownViewResource(R.layout.spinner_dropdown);//展開後的樣式
        mSpinMonth.setAdapter(cAdapter);
        mSpinMonth.setSelection(Integer.parseInt(dateM) - 1);
        final Animation anim = AnimationUtils.loadAnimation(this, R.anim.spinner_expand);
        mSpinMonth.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                arg0.startAnimation(anim);
                return false;
            }
        });

        mListMonth = findViewById(R.id.list_analysis_month);
        mListMonth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String postype = (String) listItem.get(position).get("item_type");
                final String postime = (String) listItem.get(position).get("item_time");
                final String posname = (String) listItem.get(position).get("item_name");
                final String poscal = (String) listItem.get(position).get("item_cal");
                final String posfist = (String) listItem.get(position).get("item_fist");
                final String posphoto = (String) listItem.get(position).get("item_photo");


                Log.d(TAG, "listItem: " + listItem.get(position));
//                Log.d(TAG, "intent: "+postype + ", " + postime + ", " + posname + ", " + poscal + ", " + posfist + ", " + posphoto);

                Intent i = new Intent();
                i.setClass(getApplicationContext(), AnalysisDetailActivity.class);

                i.putExtra("postype", postype);
                i.putExtra("postime", postime);
                i.putExtra("posname", posname);
                i.putExtra("poscal", poscal);
                i.putExtra("posfist", posfist);
                i.putExtra("posphoto", posphoto);
                startActivity(i);
            }
        });

    }

    // 折線圖設置
    private void setChart() {
        //動畫
        mTfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");

        // enable touch gestures
        mChart.setTouchEnabled(true);
        mChart.setDragDecelerationFrictionCoef(3f);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);


        // no description text
        mChart.getDescription().setEnabled(true);

        Description description = new Description();
        description.setTextColor(Color.BLACK);
        description.setText("單位：日 / 大卡      總熱量 單位:大卡");
        mChart.setDescription(description);
        mChart.setHighlightPerDragEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);
        mChart.animateX(4000);//動畫速度,值愈大越慢


        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        ChartMarkerView mv = new ChartMarkerView(AnalysisActivity.this, R.layout.view_chartmarker);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(mTfLight);
        l.setTextSize(11f);
        l.setTextColor(Color.BLACK);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
//        l.setYOffset(11f);

        //x軸
        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);//分隔線-以10為單位
        xAxis.setDrawGridLines(true);
        xAxis.setTypeface(mTfLight);
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1);//Y與X對其格線
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED); //X軸 標題上下皆顯示

        //y軸
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setGranularity(1);//Y與X對其格線
        leftAxis.enableAxisLineDashedLine(10f, 10f, 0f);
        leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(Color.BLACK);//ColorTemplate.getHoloBlue()
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(false);
//
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setGranularity(1);//Y與X對其格線
        rightAxis.enableAxisLineDashedLine(10f, 10f, 0f);
        rightAxis.setTypeface(mTfLight);
        rightAxis.setTextColor(getResources().getColor(R.color.redPink));
        rightAxis.setDrawGridLines(false);
        rightAxis.setGranularityEnabled(false);
    }


    //呼叫SQL資料
    private void callSql() {
        // TODO Auto-generated method stub

        Log.d(TAG, "call date =" + dateY + "-" + dateM + "-");

        //搜尋db資料庫
        mRecordData = sqldb.selectRecord(mUserName, dateY + "-" + dateM + "-","ASC");
        Log.d(TAG, "mRecordData size=" + mRecordData.size());

        //搜尋db資料庫
        mRecordData2 = sqldb.selectRecord(mUserName, dateY + "-" + dateM + "-","DESC");

        if (mRecordData.size() == 0 && mRecordData2.size()==0) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "查無資料",
                    Toast.LENGTH_SHORT);
            t.show();
            // 清空折線圖設置
            mChart.clear();
            //清空列表清單
            listItem.clear();
            mListMonth.setAdapter(null);
        } else {
            // 加入數值到list清單中
            setListData();
            // 折線圖設置
            setChart();
            // 加入數值到折線圖
            setChartData();
        }
    }

    // list清單
    private void setListData() {
        //清空列表清單
        listItem.clear();
        mListMonth.setAdapter(null);

        for (int i = 0; i < mRecordData2.size(); i++) {

            HashMap<String, Object> items = new HashMap<String, Object>();
            // 對應顯示
            items.put("item_id", mRecordData2.get(i)[0]);
            items.put("item_user", mRecordData2.get(i)[1]);
            items.put("item_name", mRecordData2.get(i)[2]);
            items.put("item_type", mRecordData2.get(i)[3]);
            items.put("item_time", mRecordData2.get(i)[4]);
            items.put("item_cal", mRecordData2.get(i)[5]);
            items.put("item_fist", mRecordData2.get(i)[6]);
            items.put("item_photo", mRecordData2.get(i)[7]);
            listItem.add(items);

//            Log.d(TAG, "item_:　" + items.get("item_id")+ "," +items.get("item_user")+ "," + items.get("item_name")+ "," +  items.get("item_type") + "," +  items.get("item_time") + "," +  items.get("item_cal") + "," +  items.get("item_fist")+ "," +  items.get("item_photo"));


            listItemAdapter = new SimpleAdapter(this, listItem,
                    R.layout.view_listview, new String[]{"item_type",
                    "item_time", "item_name", "item_cal"}, new int[]{R.id.tv_list_type,
                    R.id.tv_list_time, R.id.tv_list_name, R.id.tv_list_cal});

        }
        mListMonth.setAdapter(listItemAdapter);


    }


    /**
     * 折線圖設置開始
     **/
    private void setChartData() {


        mTotalCal.clear();
        ArrayList<Entry> yVals1 = new ArrayList<Entry>(); //早
        ArrayList<Entry> yVals2 = new ArrayList<Entry>(); //中
        ArrayList<Entry> yVals3 = new ArrayList<Entry>(); //晚
        ArrayList<Entry> yVals4 = new ArrayList<Entry>(); //其他
        ArrayList<Entry> yVals5 = new ArrayList<Entry>(); //總和

        //type arrays item
        String[] typeArr = getResources().getStringArray(R.array.input_classifyArray);

        for (int i = 0; i < mRecordData.size(); i++) {

            Log.d(TAG, "ID: " + mRecordData.get(i)[0]);
            Log.d(TAG, "USER: " + mRecordData.get(i)[1]);
            Log.d(TAG, "名稱: " + mRecordData.get(i)[2]);
            Log.d(TAG, "分類: " + mRecordData.get(i)[3]);
            Log.d(TAG, "時間: " + mRecordData.get(i)[4]);
            Log.d(TAG, "卡路里: " + mRecordData.get(i)[5]);
            Log.d(TAG, "份量: " + mRecordData.get(i)[6]);
            Log.d(TAG, "圖片: " + mRecordData.get(i)[7]);


            String temps[] = (mRecordData.get(i)[4].toString()).split("-");//取得資料的日期(年-月-日)
            int dataDay = Integer.valueOf(temps[2]); //取得資料的當日(日)
            Float dataCal = Float.valueOf(mRecordData.get(i)[5]);//取得資料的卡路里
            String dataType = mRecordData.get(i)[3];

            // 根據類別分別加入其折線資料值vals
            if (dataType.equals(typeArr[0])) {
                //早餐
                yVals1.add(new Entry(dataDay, dataCal));
            } else if (dataType.equals(typeArr[1])) {
                //午餐
                yVals2.add(new Entry(dataDay, dataCal));
            } else if (dataType.equals(typeArr[2])) {
                //晚餐
                yVals3.add(new Entry(dataDay, dataCal));
            } else if (dataType.equals(typeArr[3])) {
                //其他
                yVals4.add(new Entry(dataDay, dataCal));
            } else {
                //不屬於類別列表內的選項一律加入其他
                yVals4.add(new Entry(dataDay, dataCal));
            }

            //計算單日總和
            getDayTotalCal(dataDay, dataCal);
        }

        //單日總和
        for (int key : mTotalCal.keySet()) {
            float value = mTotalCal.get(key);
//            Log.d(TAG, "key: " + key + " value: " + mTotalCal.get(key));
            yVals5.add(new Entry(key, value)); //卡路里
        }


        LineDataSet set1, set2, set3, set4, set5;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set2 = (LineDataSet) mChart.getData().getDataSetByIndex(1);
            set3 = (LineDataSet) mChart.getData().getDataSetByIndex(2);
            set4 = (LineDataSet) mChart.getData().getDataSetByIndex(3);
            set5 = (LineDataSet) mChart.getData().getDataSetByIndex(4);

            set1.setValues(yVals1);
            set2.setValues(yVals2);
            set3.setValues(yVals3);
            set4.setValues(yVals4);
            set5.setValues(yVals5);

            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(yVals1, typeArr[0]);
            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
            set1.setColor(getResources().getColor(R.color.lightYellow));
            set1.setCircleColor(getResources().getColor(R.color.yellow));
//            set1.setValueTextColor(Color.BLACK);
            set1.setLineWidth(2f);
            set1.setCircleRadius(3f);
            set1.setFillAlpha(65);
            set1.setFillColor(ColorTemplate.getHoloBlue());
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(false);
            //set1.setFillFormatter(new MyFillFormatter(0f));
            //set1.setDrawHorizontalHighlightIndicator(false);
            //set1.setVisible(false);
            //set1.setCircleHoleColor(Color.WHITE);

            // create a dataset and give it a type
            set2 = new LineDataSet(yVals2, typeArr[1]);
            set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set2.setColor(getResources().getColor(R.color.blue));
            set2.setCircleColor(getResources().getColor(R.color.darkBlue));
//            set2.setValueTextColor(Color.BLACK);
            set2.setLineWidth(2f);
            set2.setCircleRadius(3f);
            set2.setFillAlpha(65);
            set2.setFillColor(Color.RED);
            set2.setDrawCircleHole(false);
            set2.setHighLightColor(Color.rgb(244, 117, 117));
            //set2.setFillFormatter(new MyFillFormatter(900f));

            set3 = new LineDataSet(yVals3, typeArr[2]);
            set3.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set3.setColor(getResources().getColor(R.color.lightBlue));
            set3.setCircleColor(getResources().getColor(R.color.green));
//            set3.setValueTextColor(Color.BLACK);
            set3.setLineWidth(2f);
            set3.setCircleRadius(3f);
            set3.setFillAlpha(65);
            set3.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            set3.setDrawCircleHole(false);
            set3.setHighLightColor(Color.rgb(244, 117, 117));

            set4 = new LineDataSet(yVals4, typeArr[3]);
            set4.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set4.setColor(getResources().getColor(R.color.lightPink));
            set4.setCircleColor(getResources().getColor(R.color.lightPinkOrange));
            set4.setLineWidth(2f);
            set4.setCircleRadius(3f);
            set4.setFillAlpha(65);
            set4.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            set4.setDrawCircleHole(false);
            set4.setHighLightColor(Color.rgb(244, 117, 117));

            set5 = new LineDataSet(yVals5, "單日總熱量");
            set5.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set5.setColor(getResources().getColor(R.color.lightRedPink));
            set5.setCircleColor(getResources().getColor(R.color.pink));
            set5.setLineWidth(2f);
            set5.setCircleRadius(3f);
            set5.setFillAlpha(65);
            set5.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            set5.setDrawCircleHole(false);
            set5.setHighLightColor(Color.rgb(244, 117, 117));

            // create a data object with the datasets
            LineData data = new LineData(set1, set2, set3, set4, set5);
            data.setValueTextColor(Color.BLACK);
            data.setValueTextSize(9f);

            // set data
            mChart.setData(data);
        }


    }

    //取得單日總和卡路里
    private void getDayTotalCal(int dataDay, Float dataCal) {

        Float mapCal = mTotalCal.get(dataDay);//查當天的熱量
        Log.d(TAG, dataDay + ", " + dataCal + "; mapCal= " + mapCal);


        if (mapCal != null) {
            Float mapCalAdd = mapCal + dataCal;
            mTotalCal.put(dataDay, mapCalAdd);
//            Log.d(TAG, dataDay + "原本value: " + mapCal + " 加上: " + dataCal + " 等於:" + mapCalAdd);
        } else {
            mTotalCal.put(dataDay, dataCal);
//            Log.d(TAG, dataDay + "原本value: " + mapCal + " 加上: " + dataCal + " 目前是: " + mTotalCal.get(dataDay));
        }
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);
        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOWHIGH", "low: " + mChart.getLowestVisibleX() + ", high: " + mChart.getHighestVisibleX());
        Log.i("MIN MAX", "xmin: " + mChart.getXChartMin() + ", xmax: " + mChart.getXChartMax() + ", ymin: " + mChart.getYChartMin() + ", ymax: " + mChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    /** 折線圖設置結束 **/




    // 取得時間
    public void getDate() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM");
        String dateYM = sDateFormat.format(new java.util.Date());
        String temps[] = dateYM.split("-");
        dateY = temps[0];//年
        dateM = temps[1];//月
    }


    /**
     * spinner select
     **/
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                dateM = "01";
                callSql(); //更新資料庫顯示
                break;
            case 1:
                dateM = "02";
                callSql(); //更新資料庫顯示
                break;
            case 2:
                dateM = "03";
                callSql(); //更新資料庫顯示
                break;
            case 3:
                dateM = "04";
                callSql(); //更新資料庫顯示
                break;
            case 4:
                dateM = "05";
                callSql(); //更新資料庫顯示
                break;
            case 5:
                dateM = "06";
                callSql(); //更新資料庫顯示
                break;
            case 6:
                dateM = "07";
                callSql(); //更新資料庫顯示
                break;
            case 7:
                dateM = "08";
                callSql(); //更新資料庫顯示
                break;
            case 8:
                dateM = "09";
                callSql(); //更新資料庫顯示
                break;
            case 9:
                dateM = "10";
                callSql(); //更新資料庫顯示
                break;
            case 10:
                dateM = "11";
                callSql(); //更新資料庫顯示
                break;
            case 11:
                dateM = "12";
                callSql(); //更新資料庫顯示
                break;
            default:
                break;
        }


    }


    // Switch開關監聽
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

        mViewChart.setVisibility(View.VISIBLE);
        mViewList.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        switch (compoundButton.getId()) {

            case R.id.switch_changeview:
                if (compoundButton.isChecked()) {
                    mViewChart.setVisibility(View.VISIBLE);
                    mViewList.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "顯示折線圖", Toast.LENGTH_SHORT).show();
                } else {
                    mViewChart.setVisibility(View.INVISIBLE);
                    mViewList.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "顯示清單", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
