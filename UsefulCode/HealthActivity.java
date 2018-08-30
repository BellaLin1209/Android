package com.example.health;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


/***
* 範例程式碼：
* 1. 點擊叫出dialog時間選擇器
* 2. 回存至雲端資料庫
****/



public class HealthActivity extends Activity {

	TextView tv_1, tv_2;
	TextView tv_date;
	EditText ed1;
	RadioGroup radioGroup1;
	RadioButton radio0, radio1, radio2;
	Button btn_submit;

	// 介紹內容
	String context1 = "有心臟血管疾病家族史；高階主管或生活壓力大者；不明原因胸痛胸悶者；長期高血脂；有不明原因之頸部、上背部、肩膀痠痛者； 長期高血壓、高血糖者；45歲以上關心身體健康者；體重過重者；預防腫瘤，想早期發現者";
	String context2 = "『尊爵套檢』綜合胃腸鏡、心血管重點檢查，另提供低輻射劑量肺部電腦斷層，更精確、更早期偵測檢查肺部有無腫瘤、肺炎、肉芽腫或纖維化等異常；冠狀動脈鈣化積分檢查，評估心臟冠狀動脈鈣化程度、心肌梗塞風險。並搭配專業營養師一對一營養諮詢，依個人身體狀況、飲食習慣，提供客製化飲食衛教及建議。";

	// 時間設定
	private Calendar c = null;
	String nowdate; // 取得當天日期

	// 存資料庫資料
	String Health_memberAcc = "nate_sun@touchlife.com.tw"; // 會員帳號
	String Health_memberPass = "aaaa"; // 會員密碼
	String Health_uid = "1";// 會員ID
	String Health_date = ""; // 日期
	String Health_time = ""; // 時間
	String Health_etc = ""; // 備註欄
	String Health_status = "0"; // 狀態

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_health);

		tv_1 = (TextView) findViewById(R.id.tv_1);
		tv_2 = (TextView) findViewById(R.id.tv_2);
		tv_date = (TextView) findViewById(R.id.date);
		radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
		radio0 = (RadioButton) findViewById(R.id.radio0);
		radio1 = (RadioButton) findViewById(R.id.radio1);
		radio2 = (RadioButton) findViewById(R.id.radio2);
		ed1 = (EditText) findViewById(R.id.ed1);
		btn_submit = (Button) findViewById(R.id.btn_submit);

		tv_1.setText(context1);
		tv_2.setText(context2);

		getdate();// 取得當天日期
		tv_date.setText(nowdate); // 預設成當天日期

		tv_date.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final Calendar c = Calendar.getInstance();
				onCreateDialog(tv_date).show(); // 開啟dialog日期選擇器
			}
		});

		btn_submit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Health_date = tv_date.getText().toString(); // 取得日期
				Health_etc = ed1.getText().toString(); // 取得備註欄

				// 取得時段
				switch (radioGroup1.getCheckedRadioButtonId()) {
				case R.id.radio0:
					Health_time = "0";// 早上
					break;
				case R.id.radio1:
					Health_time = "1";// 下午
					break;
				case R.id.radio2:
					Health_time = "2";// 晚上
					break;
				}

				/** 存入資料庫 **/
				Thread mThread = new Thread(runnable);
				mThread.start();

			}
		});

	}

	/** 取得時間開始 **/
	public void getdate() {
		// 設定格式 取得 年/月/日
		SimpleDateFormat ymd = new SimpleDateFormat("yyyy/MM/dd");// 設定格式
		Calendar c = Calendar.getInstance();
		nowdate = ymd.format(c.getTime()); // 取得當日期
	}

	/** 取得時間結束 **/

	/** 時間dialog設定 開始 **/
	protected Dialog onCreateDialog(final TextView tv) {
		Dialog dialog = null;
		c = Calendar.getInstance();

		dialog = new DatePickerDialog(this,
				new DatePickerDialog.OnDateSetListener() {

					public void onDateSet(DatePicker dp, int year, int month,
							int dayOfMonth) {

						String date2;
						if ((month + 1) < 10 && dayOfMonth < 10) {
							date2 = year + "/0" + (month + 1) + "/0"
									+ dayOfMonth;
							tv.setText(date2);
						} else if ((month + 1) < 10) {
							date2 = year + "/0" + (month + 1) + "/"
									+ dayOfMonth;
							tv.setText(date2);
						} else if (dayOfMonth < 10) {
							date2 = year + "/" + (month + 1) + "/0"
									+ dayOfMonth;
							tv.setText(date2);
						} else {
							date2 = year + "/" + (month + 1) + "/" + dayOfMonth;
							tv.setText(date2);
						}

					}

				}, c.get(Calendar.YEAR), // 傳入年
				c.get(Calendar.MONTH), // 船入月
				c.get(Calendar.DAY_OF_MONTH) // 傳入日

		);

		return dialog;
	}

	/** 時間dialog設定 結束 **/

	/** 存入資料庫 開始 **/
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			default:
			case 0:
				Toast t = Toast.makeText(getApplicationContext(),
						"預約失敗!請再試一次!", Toast.LENGTH_SHORT);
				t.show();
				break;
			case 1:
				Toast t2 = Toast.makeText(getApplicationContext(), "預約完成!",
						Toast.LENGTH_SHORT);
				t2.show();
				break;
			}
		}
	};

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			String UrlPath = null;
			UrlPath = String.format("http://114.32.24.244:8085/O2O/");
			String httpUrl = UrlPath + "HealthAPI.php";
			String content;
			String result = null;
			URL url = null;

			try {
				url = new URL(httpUrl);
			} catch (MalformedURLException e1) {
				mHandler.obtainMessage(0).sendToTarget();
				return;
			}
			if (url != null) {
				try {
					content = "memberAcc="
							+ URLEncoder.encode(Health_memberAcc, "UTF-8")
							+ "&" + "memberPass="
							+ URLEncoder.encode(Health_memberPass, "UTF-8")
							+ "&" + "uid="
							+ URLEncoder.encode(Health_uid, "UTF-8") + "&"
							+ "date=" + URLEncoder.encode(Health_date, "UTF-8")
							+ "&" + "time="
							+ URLEncoder.encode(Health_time, "UTF-8") + "&"
							+ "etc=" + URLEncoder.encode(Health_etc, "UTF-8")
							+ "&" + "status="
							+ URLEncoder.encode(Health_status, "UTF-8");

					// content =
					// "memberAcc=nate_sun@touchlife.com.tw&memberPass=70c881d4a26984ddce795f6f71817c9cf4480e79&uid=1&date=2016/02/28&time=0&etc=Aaaa&status=1";
					HttpURLConnection urlConn = (HttpURLConnection) url
							.openConnection();
					urlConn.setDoOutput(true);
					urlConn.setDoInput(true);
					urlConn.setRequestMethod("POST");
					urlConn.setUseCaches(false);
					urlConn.setInstanceFollowRedirects(true);
					urlConn.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded");
					urlConn.connect();
					DataOutputStream out = new DataOutputStream(
							urlConn.getOutputStream());
					out.writeBytes(content);
					out.flush();
					out.close();
					InputStreamReader in = new InputStreamReader(
							urlConn.getInputStream());
					BufferedReader buffer = new BufferedReader(in);
					String inputLine = null;
					result = "";
					while (((inputLine = buffer.readLine()) != null)) {
						result += inputLine;
					}
					in.close();
					Log.e("result", result);
					mHandler.obtainMessage(1).sendToTarget();
				} catch (IOException e) {
					mHandler.obtainMessage(0).sendToTarget();
					return;
				}
			}
		}
	};
	/** 存入資料庫 結束 **/

}