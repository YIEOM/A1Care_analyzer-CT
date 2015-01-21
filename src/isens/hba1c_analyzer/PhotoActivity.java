
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

public class PhotoActivity extends Activity {
	
	public SerialPort PhotoSerial;
	
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
		
	public RelativeLayout testSubLayout;
	public View errorPopupView;
	public PopupWindow errorPopup;
	
	public Button errorBtn;
	public TextView errorText;
	
	public static TextView TimeText;
	public static ImageView deviceImage;
				   
	public double darkValue[] = new double[2],
				  f535nmValue[] = new double[2],
				  absorbance;
	
	public RunActivity.AnalyzerState photoState = AnalyzerState.MeasurePosition;
	
	public boolean isNormal = true;
	
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
		
		TimerDisplay.timerState = whichClock.PhotoClock;
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		
		titleText.setText("PHOTO ACCURACY");
		testSubState.setTextColor(Color.parseColor("#04A458"));
		testSubState.setText("Ready");
		text1Title.setText("ADC VALUE");
		text2Title.setText("ABSORBANCE");
		
		cancelBtn.setEnabled(false);
		
		PhotoSerial = new SerialPort();
	}
	
//	public void TestStart() {
//		
//		testSubState.setTextColor(Color.parseColor("#DC143C"));
//		testSubState.setText("Measuring");
//		
//		for(int i = 0; i < 3; i++) {
//			
//			switch(photoState) {
//			
//			case MeasurePosition :
//				MotionInstruct(RunActivity.MEASURE_POSITION, SerialPort.CtrTarget.PhotoSet);			
//				BoardMessage(RunActivity.MEASURE_POSITION, AnalyzerState.FilterDark);
//				break;
//				
//			case FilterDark :
//				MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
//				BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.Filter535nm);
//				darkValue[0] = AbsorbanceMeasure(0.0);
//				break;
//				
//			case Filter535nm :
//				/* 535nm filter Measurement */
//				MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//				BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter535nm);
//				f535nmValue[0] = AbsorbanceMeasure(darkValue[0]);
//				break;
//				
//			default	:
//				break;
//			}
//		}
//		
//		if(photoState == AnalyzerState.Filter535nm) {
//		
//			PhotoMeasure mPhotoMeasure = new PhotoMeasure();
//			mPhotoMeasure.start();
//		
//		} else {
//			
//			// error
//		}
//	}
	
	public void TestStart() {
		
		testSubState.setTextColor(Color.parseColor("#DC143C"));
		testSubState.setText("Measuring");
	
		TimerDisplay.RXBoardFlag = true;
		Measurement mMeasurement = new Measurement();
		mMeasurement.start();
	}
	
	public class Measurement extends Thread {
		
		public void run() {
	
			for(int i = 0; i < 3; i++) {
				
				switch(photoState) {
				
				case MeasurePosition :
					MotionInstruct(RunActivity.MEASURE_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.MEASURE_POSITION, AnalyzerState.FilterDark);
					break;
					
				case FilterDark :
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.Filter535nm);
					darkValue[0] = AbsorbanceMeasure(0.0);
					break;
					
				case Filter535nm :
					/* 535nm filter Measurement */
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter535nm);
					f535nmValue[0] = AbsorbanceMeasure(darkValue[0]);
					break;
					
				default	:
					break;
				}
			}
			
			if(photoState == AnalyzerState.Filter535nm) {
			
				PhotoMeasure mPhotoMeasure = new PhotoMeasure();
				mPhotoMeasure.start();
			
			} else {
				
				// error
			}
		}
	}
	
	public class PhotoMeasure extends Thread {
		
		public void run() {
			
			do {
//				Log.w("Photo measure", "state : " + photoState);
				switch(photoState) {
				
//				case FilterDark :
//					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
//					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.Filter535nm);
//					darkValue[1] = AbsorbanceMeasure(0.0);
//					break;
					
				case Filter535nm :
					/* 535nm filter Measurement */
//					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.FilterDark);
					f535nmValue[1] = AbsorbanceMeasure(darkValue[0]);
					
					if(!AbsorbancetHandling()) {
					
						photoState = AnalyzerState.NoWorking;
						ErrorDisplay("Absorbance Error");
					
					} else {
						
						PhotoDisplay();
					}
					break;
					
				default	:
					break;
				}
				
				SerialPort.Sleep(10);
				
			} while(isNormal);
			
			TestCancel();
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
		photoState = AnalyzerState.FilterHome;
		
		for(int i = 0; i < 2; i++) {
			
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
	
	public void MotionInstruct(String str, SerialPort.CtrTarget target) { // Motion of system instruction
		
		PhotoSerial.BoardTx(str, target);
	}
	
	public synchronized double AbsorbanceMeasure(double offset) { // Absorbance measurement
		
		String rawValue;
		double douValue = 0;
		
		PhotoSerial.BoardTx("VH", SerialPort.CtrTarget.PhotoSet);
		
		rawValue = PhotoSerial.BoardMessageOutput();			
		
		while(rawValue.length() != 8) {
		
			rawValue = PhotoSerial.BoardMessageOutput();			
		
			SerialPort.Sleep(10);
		}	
	
		douValue = Double.parseDouble(rawValue);
			
		return (douValue - offset);
	}
	
	public void BoardMessage(String colRsp, AnalyzerState nextState) {
		
		String temp = "";
		
		while(true) {
			
			temp = PhotoSerial.BoardMessageOutput();
			
			if(colRsp.equals(temp)) {
				
				photoState = nextState;
				break;
			} 
			
			SerialPort.Sleep(10);
		}
	}
	
	public boolean AbsorbancetHandling() {
		
		boolean isPass = false;
		
		absorbance = -Math.log10(f535nmValue[1]/f535nmValue[0]); // 535nm
		
//		Log.w("Absorbance", "pre dark : " + darkValue[0]);
//		Log.w("Absorbance", "curr dark : " + darkValue[1]);
//		Log.w("Absorbance", "pre 535nm : " + f535nmValue[0]);
//		Log.w("Absorbance", "curr 535nm : " + f535nmValue[1]);
		
		if((absorbance > -0.0007) && (absorbance < 0.0007)) {
			
			isPass = true;
			
//			darkValue[0] = darkValue[1];
			f535nmValue[0] = f535nmValue[1];
//			darkValue[1] = 0.0;
			f535nmValue[1] = 0.0;
		}
		
//		Log.w("Absorbance", "absorbance : " + absorbance);
		
		return isPass;
	}
	
	public void ErrorDisplay(String str) {
		
		TextView errorText = (TextView) errorPopupView.findViewById(R.id.errortext);
		
		errorText.setText(str);
		
		new Thread(new Runnable() {
		    public void run() {    
		        runOnUiThread(new Runnable(){
		            public void run() {

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
	
//	public void TimerInit() {
//		
//		OneSecondPeriod = new TimerTask() {
//			
//			public void run() {
//				Runnable updater = new Runnable() {
//					public void run() {
//						
//						PhotoDisplay();
//					}
//				};
//				
//				handler.post(updater);		
//			}
//		};
//		
//		timer = new Timer();
//		timer.schedule(OneSecondPeriod, 0, 1000); // Timer period : 1sec
//	}
	
	public void PhotoDisplay() {
		
		new Thread(new Runnable() {
			public void run() {    
				runOnUiThread(new Runnable(){
					public void run() {
						
						DecimalFormat dfm = new DecimalFormat("0.0000");
						
						text1.setText(Double.toString(f535nmValue[0]));
						text2.setText(dfm.format(absorbance));
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