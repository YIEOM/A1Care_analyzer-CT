package isens.hba1c_analyzer;

import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;

/*
 * 
 * Object : CE Certificate Test SW
 * 
 */

public class HomeActivity extends Activity {

	final static byte NORMAL = 0,
					  DEVEL = 1, // Development
					  DEMO = 2, // Sales department
					  CE = 4, // Test Mode that TUVSUD require
					  ANALYZER_SW = CE;

	final static byte PP = 1,
			          ES = 2,
			          ANALYZER_DEVICE = PP;
	
	final static double MAX_TEMP = 39.7, // 36.7 max Temperature
						MIN_TEMP = 36.3; // 36.3 min Temperature
	final static int BLANK_PERIOD = 600/5; // Period/5sec
	
	final static byte NORMAL_OPERATION = 0,
					  DOOR_SENSOR_ERROR = 1,
					  CART_SENSOR_ERROR = 2,
					  FILTER_MOTOR_ERROR = 3,
					  SHAKING_MOTOR_ERROR = 4,
					  PHOTO_SENSOR_ERROR = 5,
					  LAMP_ERROR = 6,
					  FILTER_535nm_ERROR = 7,
					  FILTER_660nm_ERROR = 8,
					  FILTER_750nm_ERROR = 9,
					  CELL_TEMP_ERROR = 10,
					  AMBIENT_TEMP_ERROR = 11,
					  COMMUNICATION_ERROR = 12,
					  STOP_RESULT = 20,
					  tHb_LOW_ERROR = 21,
					  tHb_HIGH_ERROR = 22,
					  A1c_LOW_ERROR = 23,
					  A1c_HIGH_ERROR = 24;
	
	final static byte ERROR_DARK  = 1,
					  ERROR_535nm = 2,
					  ERROR_660nm = 4,
					  ERROR_750nm = 8;				  
	
	final static byte ACTION_ACTIVITY  = 1,
					  HOME_ACTIVITY    = 2,
					  COVER_ACTION_ESC = 3,
					  TEST_ACTIVITY    = 4,
					  TEST_ACTION_ESC  = 5;
	
	final static byte FILE_CLOSE 	= 0,
			  		  FILE_OPEN 	= 1,
			  		  FILE_NOT_OPEN = 2;
	
	final static byte NONE        = 0,
					  YEAR_UP     = 1,
					  YEAR_DOWN   = 2,
					  MONTH_UP    = 3,
					  MONTH_DOWN  = 4,
					  DAY_UP      = 5,
					  DAY_DOWN    = 6,
					  HOUR_UP     = 7,
					  HOUR_DOWN   = 8,
					  MINUTE_UP   = 9,
					  MINUTE_DOWN = 10;

	final static double MaxDark = 5000,
						MinDark = 3000,
						Max535  = 300000,
						Min535  = 100000,
						Max660  = 500000,
						Min660  = 200000,
						Max750  = 800000,
						Min750  = 400000;

	public DatabaseHander homeDatabase;
	public OperatorController homePopup;
	
	private RelativeLayout homeLinear;
	
	private View errorPopupView,
				 loginPopupView;
	private PopupWindow errorPopup,
						loginPopup;
	
	private EditText oIDEText,
					 passEText;
	
	private SoundPool mPool;
	
	private Button runBtn,
				   settingBtn,
				   recordBtn,
				   errorBtn,
				   loginBtn,
				   loginDBBtn,
				   checkBtn;
	
	public enum TargetIntent {Home, HbA1c, NA, Action, Run, Blank, Memory, Result, ResultError, Remove, Image, Date, Setting, SystemSetting, DataSetting, OperatorSetting, Time, Display, HIS, HISSetting, Export, Maintenance, FileSave, ControlFileLoad, PatientFileLoad, NextFile, PreFile, Adjustment, Sound, Calibration, Language, Correlation, Test, Temperature, Shaking, Photo, BarPrintLan, ExtScan, Network}
	
	private static TextView TimeText;
	private static ImageView deviceImage;
	
	public static boolean LoginFlag,
						  CheckFlag;
		
	public static byte ExternalDeviceBarcode = FILE_CLOSE,
					   ExternalDevicePrinter = FILE_CLOSE;
	
	public static byte NumofStable = 0;
	
	public boolean btnState = false;
	
	private int mWin;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.home);
		
		/* Error Pop-up window */
		homeLinear = (RelativeLayout)findViewById(R.id.homelinear);		
		errorPopupView = View.inflate(getApplicationContext(), R.layout.errorbtnpopup, null);
		errorPopup = new PopupWindow(errorPopupView, 800, 480, true);
		
		/* Log in Pop-up window */
		loginPopupView = View.inflate(getApplicationContext(), R.layout.loginpopup, null);
		loginPopup = new PopupWindow(loginPopupView, 800, 480, true);
		
		TimeText = (TextView) findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);
		
		/* */
		if(ANALYZER_SW != DEVEL) {
		
			oIDEText = (EditText) loginPopupView.findViewById(R.id.loginoid);
			passEText = (EditText) loginPopupView.findViewById(R.id.loginpass);
		
		}
		/* */
		
		mPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		mWin = mPool.load(this, R.raw.win, 1);
		
		HomeInit();
//		
		/*Test Activity activation*/
		runBtn = (Button)findViewById(R.id.runbtn);
		runBtn.setOnClickListener(new View.OnClickListener() { 
		
			public void onClick(View v) {
			
				if(!btnState) {
		
					btnState = true;
					
					runBtn.setEnabled(false);
				
					WhichIntent(TargetIntent.Blank);
				}
			}
		});
		
		/*Setting Activity activation*/
		settingBtn = (Button)findViewById(R.id.settingbtn);
		settingBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
				
				if((ANALYZER_SW != DEMO) && (ANALYZER_SW != CE)) {
				
					if(!btnState) {
						
						btnState = true;
	
						settingBtn.setEnabled(false);
						
						WhichIntent(TargetIntent.Setting);				
					}
				}
			}
		});
		
		/*Memory Activity activation*/
		recordBtn = (Button)findViewById(R.id.recordbtn);
		recordBtn.setOnClickListener(new View.OnClickListener() { 
		
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
					
					recordBtn.setEnabled(false);
				
					WhichIntent(TargetIntent.Memory);
				}
			}
		});
		
		/*Error Pop up activation*/
		errorBtn = (Button)errorPopupView.findViewById(R.id.errorbtn);
		errorBtn.setOnClickListener(new View.OnClickListener() { 
		
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
					
					errorBtn.setEnabled(false);
				
					errorPopup.dismiss();
				
					if(ANALYZER_SW != DEVEL) {
						
						Login();
					
					} else {
						
						btnState = false;	
					}
				}
			}
		});
		
		/*Log in Pop up activation*/
		loginBtn = (Button)loginPopupView.findViewById(R.id.loginbtn);
		loginBtn.setOnClickListener(new View.OnClickListener() { 
		
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;
					
					LoginCheck();
				}
			}
		});
		
		loginDBBtn = (Button)loginPopupView.findViewById(R.id.logindbbtn);
		loginDBBtn.setOnClickListener(new View.OnClickListener() { 
			
			public void onClick(View v) {
				
			}
		});
		
		checkBtn = (Button)loginPopupView.findViewById(R.id.checkbtn);
		checkBtn.setOnClickListener(new View.OnClickListener() { 
			
			public void onClick(View v) {
	
				if(!btnState) {
					
					btnState = true;
				
					if(CheckFlag) {
						
						CheckFlag = false;
						checkBtn.setBackgroundResource(R.drawable.login_checkbox);
						
					} else {
						
						CheckFlag = true;
						checkBtn.setBackgroundResource(R.drawable.login_checkbox_check);
					}
					
					btnState = false;
				}
			}
		});
	}
	
	public void HomeInit() {
		
		int state;
		
		TimerDisplay.timerState = whichClock.HomeClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		
		mPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
		      public void onLoadComplete(SoundPool mPool, int sampleId, int status) {

		  		mPool.play(mWin, 1, 1, 0, 0, 1); // playing sound
		      }
		});
		
		Intent itn = getIntent();
		state = itn.getIntExtra("System Check State", 0);
		
		if(ANALYZER_SW != DEVEL) {
		
		if(state == 0) {
			
//			Login();
			
		} else {
		
			ErrorPopup((byte) state);
		}
		
		} else {

//			Login();
			
			if(state != 0) {
				
				ErrorPopup((byte) state);
			}
		}
	}
	
	public void CurrTimeDisplay() { // displaying current time
		
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

	public void Login() {
		
		if(LoginFlag) {
			
			LoginFlag = false;
			
			if(ANALYZER_SW != DEVEL) {
				
				LoginPopup();
				
			} else {
				
				homePopup = new OperatorController(this, getApplicationContext(), R.id.homelinear);
				homePopup.LoginDisplay();
			}
	
//			Log.w("Log in", "log");
		}
		
		btnState = false;
	}
	
	public void LoginPopup() {
		
		new Thread(new Runnable() {
		    public void run() {    
		        runOnUiThread(new Runnable(){
		            public void run() {
		            	homeLinear.post(new Runnable() {
		        			public void run() {
				        
		        				passEText.setTypeface(Typeface.DEFAULT);
				            	PasswordDisplay();
				            	
				            	loginPopup.showAtLocation(homeLinear, Gravity.CENTER, 0, 0);
								loginPopup.setAnimationStyle(0);
							}
		        		});
		            }
		        });
		    }
		}).start();
	}
	
	public void LoginCheck() {
		
		if(ANALYZER_SW != DEVEL) {
			
//			Log.w("Login", "ID : " +oIDEText.getText() + "PW : " + passEText.getText());
			if(oIDEText.getText().toString().equals("Guest") & passEText.getText().toString().equals("0000")) {
				
				loginPopup.dismiss();
				
				CheckFlagSave();
				
			} else {
							
			}
			
		} else {
			
			/*   */
			String password;
			String id = oIDEText.getText().toString();
			
			if(id.equals("")) {
				
				loginPopup.dismiss();
				
			} else {
			
				homeDatabase = new DatabaseHander(this);
				homeDatabase.getWritableDatabase();
				
				if(homeDatabase.CheckIDDuplication(id)) {
					password = homeDatabase.GetPassword(id);
					
					Log.w("Login Check", "password : " + password);
					
					if(passEText.getText().toString().equals(password)) {
						
						loginPopup.dismiss();
						
						CheckFlagSave();
						
						homeDatabase.UpdateLastLogIn(id);
						
					} else {
								
						ErrorDisplay(getString(R.string.w006));
					}
				} else {
					
					ErrorDisplay(getString(R.string.w005));
				}
			}
			/*   */	
		}
		
		btnState = false;
	}
	
	public void PasswordDisplay() {
		
		homeDatabase = new DatabaseHander(this);
		String id = homeDatabase.GetLastLoginID();
		String password = homeDatabase.GetPassword(id);		
			
		oIDEText.setText("Guest");
		passEText.setText("0000");
		
		if(CheckFlag) {
		
			checkBtn.setBackgroundResource(R.drawable.login_checkbox_check);
		
			if(ANALYZER_SW != DEVEL) {
				
				oIDEText.setText("Guest");
				passEText.setText("0000");
			
			} else {
				
				oIDEText.setText(id);
				passEText.setText(password);
			}
			
//			Log.w("Password Display", "" + CheckFlag);
			
		} else {
			
			checkBtn.setBackgroundResource(R.drawable.login_checkbox);
	
//			Log.w("Password Display", "" + CheckFlag);
		}
	}
	
	public void ErrorDisplay(String str) {
		
		TextView errorText = (TextView) errorPopup.getContentView().findViewById(R.id.errortext);
		
		errorText.setText(str);
		
		errorPopup.showAtLocation(homeLinear, Gravity.CENTER, 0, 0);
		errorPopup.setAnimationStyle(0);
		
		errorBtn.setEnabled(true);
	}
	
	public void ErrorPopup(final byte error) {
		
    	homeLinear.post(new Runnable() {
			public void run() {
        
				errorPopup.showAtLocation(homeLinear, Gravity.CENTER, 0, 0);
				errorPopup.setAnimationStyle(0);
				
				TextView errorText = (TextView) errorPopup.getContentView().findViewById(R.id.errortext);
				
				switch(error) {
				
				case FILTER_MOTOR_ERROR		:
					errorText.setText(R.string.e212);
					break;
					
				case SHAKING_MOTOR_ERROR	:
					errorText.setText(R.string.e211);
					break;
					
				case PHOTO_SENSOR_ERROR		:
					errorText.setText(R.string.e231);
					break;
				
				case LAMP_ERROR				:
					errorText.setText(R.string.e232);
					break;
				
				case FILTER_535nm_ERROR		:
					errorText.setText(R.string.e233);
					break;
				
				case FILTER_660nm_ERROR		:
					errorText.setText(R.string.e234);
					break;
				
				case FILTER_750nm_ERROR		:
					errorText.setText(R.string.e235);
					break;
					
				case CELL_TEMP_ERROR		:
					errorText.setText(R.string.e222);
					break;
				
				case AMBIENT_TEMP_ERROR		:
					errorText.setText(R.string.e221);
					break;
					
				case COMMUNICATION_ERROR	:
					errorText.setText(R.string.e241);
					break;
				}
			}
		});
	}
	
	public void CheckFlagSave() { // Saving number of user define parameter
		
		SharedPreferences loginPref = getSharedPreferences("Log in", MODE_PRIVATE);
		SharedPreferences.Editor loginedit = loginPref.edit();
		
		loginedit.putBoolean("Check Box", CheckFlag);
		loginedit.commit();
	}
	
	public void WhichIntent(TargetIntent Itn) { // Activity conversion
		
		switch(Itn) {
		
		case Action		:				
			Intent ActionIntent = new Intent(getApplicationContext(), ActionActivity.class);
			startActivity(ActionIntent);
			break;
			
		case Blank		:
			if(ANALYZER_SW != CE) {
			
				
				Intent BlankIntent = new Intent(getApplicationContext(), BlankActivity.class); // Change to BLANK Activity
				startActivity(BlankIntent);
			
			
			} else {
				
				
				Intent TestIntent = new Intent(getApplicationContext(), TestActivity.class); // Change to TEST Activity
				startActivity(TestIntent);
				
				
			}
			break;
			
		case Memory		:			
			Intent memoryIntent = new Intent(getApplicationContext(), MemoryActivity.class); // Change to MEMORY Activity
			startActivity(memoryIntent);
			break;
			
		case Setting	:
			if(ANALYZER_SW != DEVEL) {
				
				Intent SettingIntent = new Intent(getApplicationContext(), SystemSettingActivity.class); // Change to SETTING Activity
				startActivity(SettingIntent);
					
			} else {
				
				/* */
				Intent SettingIntent = new Intent(getApplicationContext(), SettingActivity.class); // Change to SETTING Activity
				startActivity(SettingIntent);
				/* */
			}
			break;
			
		default			:	
			break;
		}
		
		finish();
	}
	
	public void finish() {
		
		super.finish();
		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
}