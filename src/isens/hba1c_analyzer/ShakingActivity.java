
package isens.hba1c_analyzer;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.RunActivity.AnalyzerState;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class ShakingActivity extends Activity {
	
	public SerialPort ShakingSerial;
	
	public Handler handler = new Handler();
	public TimerTask OneSecondPeriod;
	public Timer timer;
	
	public TextView titleText;
	
	public Button escBtn,
				  runBtn,
				  cancelBtn;
	
	public TextView testSubState,
					text1Title,
					text1,
					text2Title,
					text2;
		
	private RelativeLayout testSubLayout;
	private View errorPopupView;
	private PopupWindow errorPopup;
	
	public Button errorBtn;
	public TextView errorText;
	
	public static TextView TimeText;
	private static ImageView deviceImage;
				   
	public RunActivity.AnalyzerState photoState = AnalyzerState.MeasurePosition;
	
	public boolean isNormal = true;
	
	public String RData = "";
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.testsub);
		
		TimeText = (TextView)findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);
				
		titleText    = (TextView)findViewById(R.id.testsubtitle);
		testSubState = (TextView)findViewById(R.id.testsubstate);
		text1Title   = (TextView)findViewById(R.id.text1title);
		text1 		 = (TextView)findViewById(R.id.text1);
		text2Title   = (TextView)findViewById(R.id.text2title);
		text2        = (TextView)findViewById(R.id.text2);
		
		testSubLayout = (RelativeLayout)findViewById(R.id.testsublayout);		
		errorPopupView = View.inflate(getApplicationContext(), R.layout.errorbtnpopup, null);
		errorPopup = new PopupWindow(errorPopupView, 800, 480, true);
		
		/*System setting Activity activation*/
		escBtn = (Button)findViewById(R.id.escicon);
		escBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				escBtn.setEnabled(false);
				
				WhichIntent(TargetIntent.Test);
			}
		});
		
		runBtn = (Button)findViewById(R.id.runbtn);
		runBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				runBtn.setEnabled(false);
				cancelBtn.setEnabled(true);
				escBtn.setEnabled(false);
				
				TestStart();
			}
		});
		
		cancelBtn = (Button)findViewById(R.id.cancelbtn);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				cancelBtn.setEnabled(false);
				
				isNormal = false;
			}
		});
		
		errorBtn = (Button)errorPopupView.findViewById(R.id.errorbtn);
		errorBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
				
				TestCancel();
				
				cancelBtn.setEnabled(false);
			}
		});
		
		Init();
	}
	
	public void Init() {
		
		TimerDisplay.timerState = whichClock.ShakingClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		
		titleText.setText("SHAKIN MODE");
		testSubState.setTextColor(Color.parseColor("#04A458"));
		testSubState.setText("READY");
		text1Title.setText("RECEIVED");
		text2Title.setText("");
		
		cancelBtn.setEnabled(false);
		
		ShakingSerial = new SerialPort();
	}
	
//	public void TestStart() {
//		
//		testSubState.setTextColor(Color.parseColor("#DC143C"));
//		testSubState.setText("Measuring");
//		
//		for(int i = 0; i < 2; i++) {
//			
//			switch(photoState) {
//			
//			case MeasurePosition :
//				MotionInstruct(RunActivity.MEASURE_POSITION, SerialPort.CtrTarget.PhotoSet);			
//				BoardMessage(RunActivity.MEASURE_POSITION, AnalyzerState.Step1Shaking);
//				break;
//				
//			case Step1Shaking :
//				MotionInstruct("MI", SerialPort.CtrTarget.PhotoSet);
//
//				ReceivedData mReceivedData = new ReceivedData();
//				mReceivedData.start();
//				break;
//				
//			default	:
//				break;
//			}
//		}
//	}

	public void TestStart() {
		
		testSubState.setTextColor(Color.parseColor("#DC143C"));
		testSubState.setText("Measuring");
		
		TimerDisplay.RXBoardFlag = true;
		Shaking mShaking = new Shaking();
		mShaking.start();
	}
	
	public class Shaking extends Thread {
		
		public void run() {
			
			for(int i = 0; i < 2; i++) {
				
				switch(photoState) {
				
				case MeasurePosition :
					MotionInstruct(RunActivity.MEASURE_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.MEASURE_POSITION, AnalyzerState.Step1Shaking);
					break;
					
				case Step1Shaking :
					MotionInstruct("MI", SerialPort.CtrTarget.PhotoSet);

					ReceivedData mReceivedData = new ReceivedData();
					mReceivedData.start();
					break;
					
				default	:
					break;
				}
			}
		}
	}
	
	public class ReceivedData extends Thread {
		
		public void run() {
			
			int time = 0;
			
			while(isNormal) {
				
				RData = ShakingSerial.BoardMessageOutput();
						
				Log.w("Received Data", "RData : " + RData);
				
				if(RData.equals("MI")) {
					
					time = 0;
					
					ShakingDisplay();
				}
				
				if(time++ > 50) { // 5 seconds
					
					ErrorDisplay("Shaking Error");
				}
				
				SerialPort.Sleep(100);
			}
			
			TestCancel();
		}
	}
	
	public void MotionInstruct(String str, SerialPort.CtrTarget target) { // Motion of system instruction
		
		ShakingSerial.BoardTx(str, target);
	}
	
	public void BoardMessage(String colRsp, AnalyzerState nextState) {
		
		String temp = "";
		
		while(true) {
			
			temp = ShakingSerial.BoardMessageOutput();
			
			if(colRsp.equals(temp)) {
				
				photoState = nextState;
				break;
			} 
			
			SerialPort.Sleep(100);
		}
	}

	public void TestCancel() {
		
		if(isNormal) {

			isNormal = false;
			errorPopup.dismiss();
	
		} else TestRestore();
	}
	
	public void TestRestore() {
		
		isNormal = true;
		
		MotionInstruct(RunActivity.MOTOR_STOP, SerialPort.CtrTarget.MotorStop);
		BoardMessage(RunActivity.MOTOR_STOP, AnalyzerState.FilterHome);
		
		for(int i = 0; i < 3; i++) {
			
			switch(photoState) {
			
			case FilterHome :
				MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
				BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.CartridgeHome);
				break;
			
			case CartridgeHome :
				MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);
				BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NormalOperation);
				break;
				
			default	:
				break;
			}
		}
		
		TimerDisplay.RXBoardFlag = false;
		SerialPort.Sleep(1000);
		
		new Thread(new Runnable() {
		    public void run() {    
		        runOnUiThread(new Runnable(){
		            public void run() {
		            	
		            	testSubState.setTextColor(Color.parseColor("#04A458"));
		            	testSubState.setText("READY");
		            	
		            	text1.setText("");
						text2.setText("");

						runBtn.setEnabled(true);
						escBtn.setEnabled(true);
		            }
		        });
		    }
		}).start();
		
		photoState = AnalyzerState.MeasurePosition;
	}
	
	public void ErrorDisplay(final String str) {
		
		new Thread(new Runnable() {
		    public void run() {    
		        runOnUiThread(new Runnable(){
		            public void run() {

		            	TextView errorText = (TextView) errorPopupView.findViewById(R.id.errortext);
		        		
		        		errorText.setText(str);
		        		
		        		errorPopup.showAtLocation(testSubLayout, Gravity.CENTER, 0, 0);
		        		errorPopup.setAnimationStyle(0);
		        		
		        		errorBtn.setEnabled(true);
		            }
		        });
		    }
		}).start();
	}
	
	public void CurrTimeDisplay() {
		
		new Thread(new Runnable() {
		    public void run() {    
		        runOnUiThread(new Runnable(){
		            public void run() {
		            	
		            	TimeText.setText(TimerDisplay.rTime[3] + " " + TimerDisplay.rTime[4] + ":" + TimerDisplay.rTime[5]);
		            }
		        });
		    }
		}).start();	
	}

	public void ExternalDeviceDisplay() {
		
		new Thread(new Runnable() {
		    public void run() {    
		        runOnUiThread(new Runnable(){
		            public void run() {
		           
		            	if(HomeActivity.ExternalDeviceBarcode == HomeActivity.FILE_OPEN) deviceImage.setBackgroundResource(R.drawable.main_usb_c);
		            	else deviceImage.setBackgroundResource(R.drawable.main_usb);
		            }
		        });
		    }
		}).start();
	}

	public void ShakingDisplay() {
		
		new Thread(new Runnable() {
			public void run() {    
				runOnUiThread(new Runnable(){
					public void run() {
						
						text1.setText(RData);
						text2.setText("");
					}
				});
			}
		}).start();
	}
	
	public void WhichIntent(TargetIntent Itn) { // Activity conversion
		
		switch(Itn) {
		
		case Test	:
			Intent TestIntent = new Intent(getApplicationContext(), TestActivity.class);
			startActivity(TestIntent);
			break;
			
		default		:	
			break;			
		}
		
		finish();
	}
	
	public void finish() {
		
		super.finish();
		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
}