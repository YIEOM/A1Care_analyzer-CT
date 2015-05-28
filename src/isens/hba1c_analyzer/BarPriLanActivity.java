
package isens.hba1c_analyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import isens.hba1c_analyzer.ActionActivity.CartridgeInsert;
import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.RunActivity.AnalyzerState;
import isens.hba1c_analyzer.TCPServer.TCPAndroidServer;
import isens.hba1c_analyzer.TimerDisplay.whichClock;

public class BarPriLanActivity extends Activity {
	
	public SerialPort BPLSerial;
	public GpioPort BPLGpio;
	public TCPServer BPLTCP;
	
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
		
	public static EditText text3;
	public TextView text3Title;
	
	private RelativeLayout testSubLayout;
	private View errorPopupView;
	private PopupWindow errorPopup;
	
	public Button errorBtn;
	public TextView errorText;
	
	public static TextView TimeText;
	private static ImageView deviceImage;
				   
	public boolean isNormal = true;
	
	public Socket socket;
	
	public static boolean isFinish = false;
	
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
		text3Title   = (TextView)findViewById(R.id.text3title);
		text3        = (EditText)findViewById(R.id.text3);
		
//	 	editTextTitle = (TextView)findViewById(R.id.edittexttitle);
//		editText = (EditText)findViewById(R.id.edittext);
			
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
				ActionActivity.ESCButtonFlag = true;
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
		
		TimerDisplay.timerState = whichClock.BarPriLanClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		
		titleText.setText("BARCODE & PRINTER & LAN");
		testSubState.setTextColor(Color.parseColor("#04A458"));
		testSubState.setText("READY");
		text1Title.setText("BARCODE");
		text2Title.setText("ETHERNET");
		text3Title.setText("USB BARCODE");
		cancelBtn.setEnabled(false);
	}
	
//	public static boolean isNetworkAvailable(Context context) {
//		
//		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//		if (connectivity != null) {
//			NetworkInfo[] info = connectivity.getAllNetworkInfo();
//			
//			for(NetworkInfo n:info) {
//            	Log.w("isNetworkAvailable","all network : "+n.toString());
//
//            }
//			
//			if (info != null) {
//				for (int i = 0; i < info.length; i++) {
//					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
//						
//						Log.w("isNetworkAvailable","device : "+info[i].getTypeName());
//						return true;
//					}
//				}
//			}
//			
//			
//		}
//		return false;
//	}
	
	public void TestStart() {
		
		testSubState.setTextColor(Color.parseColor("#DC143C"));
		testSubState.setText("Measuring");

		isNormal = true;
		
		BPLSerial = new SerialPort();
		
		BPLTCP = new TCPServer();
		String ip = BPLTCP.GetDeviceIP(this, testSubLayout);
		
		Log.w("Test Start", "IP : " + ip);
		
		BPLTCP.DataTransmit(this, this);
		
		BarcodeScan mBarcodeScan = new BarcodeScan(this);
		mBarcodeScan.start();
		
		PrintResultData mPrintResultData = new PrintResultData();
		mPrintResultData.start();
	}

	public class BarcodeScan extends Thread {
		
		Activity activity;
		
		BarcodeScan(Activity activity) {
			
			this.activity = activity;
		}
		
		@Override
		public void run() {
		
			while(isNormal) {
							
				ActionActivity.ESCButtonFlag = false;
				ActionActivity.IsCorrectBarcode = false;	
				ActionActivity.BarcodeCheckFlag = false;
				
				SerialPort.BarcodeReadStart = false;
				SerialPort.BarcodeBufIndex = 0;
				
				Barcode.Barcode = "";

				BPLDisplay(activity, "", "");
								
				Log.w("Barcode", "run");
				
				BPLGpio = new GpioPort();
				BPLGpio.TriggerLow();
				SerialPort.Sleep(50);
				BPLGpio.TriggerHigh();
				
				while(!ActionActivity.BarcodeCheckFlag) {
				
					if(ActionActivity.ESCButtonFlag) break; // exiting if esc button is pressed
					SerialPort.Sleep(10);
				}
				
				if(!ActionActivity.ESCButtonFlag) { 
					
					if(ActionActivity.IsCorrectBarcode) {
						
						BPLDisplay(activity, Barcode.Barcode, "");
				
						SerialPort.Sleep(300);
						
					} else {
					
						isNormal = false;
						
						ErrorDisplay("Barcode Error");
					}	
				}
			} 
			
			Log.w("Barcode Scan", "finish");
			BPLTCP.TCPClose();
			TestRestore();
		}
	}

	public class PrintResultData extends Thread {
		
		@Override
		public void run() {
			
			while(isNormal) {
			
				isFinish = false;
				
				StringBuffer txData = new StringBuffer();
				
				txData.delete(0, txData.capacity());
				
				txData.append(Barcode.Barcode + " " + TimerDisplay.rTime[6]);
				
				Log.w("Print result data", "txData : " + txData.substring(0));
			
				BPLSerial.PrinterTxStart(SerialPort.PRINTRESULT, txData);
				
				SerialPort.Sleep(500);
				while(!isFinish) SerialPort.Sleep(10);
			}
		}
	}
	
//	public void Trigger() {
//		
//		TimerTask triggerTimer = new TimerTask() {
//			
//			int cnt = 0;
//			
//			public void run() {
//				Runnable updater = new Runnable() {
//					public void run() {
//						
////						Log.w("Trigger", "run");
//						
//						if (cnt++ == 2) {
//							
//							cnt = 0;
//							BPLGpio = new GpioPort();
//							BPLGpio.TriggerLow();
//							SerialPort.Sleep(100);
//							BPLGpio.TriggerHigh();
//						}
//					}
//				};
//				
//				handler.post(updater);		
//			}
//		};
//		
//		timer = new Timer();
//		timer.schedule(triggerTimer, 0, 200); // Timer period : 1sec
//	}
	
	public void TestCancel() {
		
		errorPopup.dismiss();
		
		BPLTCP.TCPClose();
		TestRestore();
	}
	
	public void TestRestore() {
		
		new Thread(new Runnable() {
		    @Override
			public void run() {    
		        runOnUiThread(new Runnable(){
		            @Override
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
	}
	
	public void HHBBarcodeDisplay(final StringBuffer str) {
		
		new Thread(new Runnable() {
		    public void run() {    
		        runOnUiThread(new Runnable(){
		            public void run() {
		            	
		            	text3.setText(str.substring(0, str.length() - 1));
		            }
		        });
		    }
		}).start();
	}
	
	public void ErrorDisplay(String str) {
		
		TextView errorText = (TextView) errorPopupView.findViewById(R.id.errortext);
		
		errorText.setText(str);
		
		new Thread(new Runnable() {
		    @Override
			public void run() {    
		        runOnUiThread(new Runnable(){
		            @Override
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
		    @Override
			public void run() {    
		        runOnUiThread(new Runnable(){
		            @Override
					public void run() {
		            	
		            	TimeText.setText(TimerDisplay.rTime[3] + " " + TimerDisplay.rTime[4] + ":" + TimerDisplay.rTime[5]);
		            }
		        });
		    }
		}).start();	
	}

	public void ExternalDeviceDisplay() {
		
		new Thread(new Runnable() {
		    @Override
			public void run() {    
		        runOnUiThread(new Runnable(){
		            @Override
					public void run() {
		           
		            	if(HomeActivity.ExternalDeviceBarcode == HomeActivity.FILE_OPEN) deviceImage.setBackgroundResource(R.drawable.main_usb_c);
		            	else deviceImage.setBackgroundResource(R.drawable.main_usb);
		            }
		        });
		    }
		}).start();
	}
	
	public void BPLDisplay(final Activity activity, final String str1, final String str2) {
		
		new Thread(new Runnable() {
			@Override
			public void run() {    
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						
						text1 = (TextView)activity.findViewById(R.id.text1);
						text2 = (TextView)activity.findViewById(R.id.text2);
						text1.setText(str1);
						text2.setText(str2);
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
	
	@Override
	public void finish() {
		
		super.finish();
		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
}