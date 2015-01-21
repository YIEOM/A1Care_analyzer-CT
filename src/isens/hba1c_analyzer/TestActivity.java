package isens.hba1c_analyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Calendar;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class TestActivity extends Activity {
	
	final static int NORMAL = 0,
					 PHOTO_TEMPERATURE = 6,
					 LAB_VIEW = 7,
					 TEMPERATURE = 8,
					 PHOTO = 9,
					 PHOTO_ABSORBANCE = 10,
					 FILE_SAVE = 20,
					 STABLE = 11;
	
//	final static int NUMBER_PHOTO_TEMP = 40;
	
	public TimerDisplay SystemSettingTimer;
	
	private Button escBtn,
				   shakingBtn,
				   photoBtn,
				   barPrintLanBtn,
				   stableBtn,
				   netBtn;

	public TextView versionText;
	
	public static TextView TimeText;
	private static ImageView deviceImage;
	
	public static int WhichTest,
					  NumofPhotoTemp;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.test);
		
		versionText = (TextView)findViewById(R.id.versiontext);
		
		TimeText = (TextView) findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);
				
		TestInit();
		
		/*Home Activity activation*/
		escBtn = (Button)findViewById(R.id.escicon);
		escBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
			
				escBtn.setEnabled(false);
				
				WhichIntent(TargetIntent.Home);
			}
		});
		
		shakingBtn = (Button)findViewById(R.id.shakingbtn);
		shakingBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
			
				shakingBtn.setEnabled(false);
				
				WhichIntent(TargetIntent.Shaking);
			}
		});
		
		photoBtn = (Button)findViewById(R.id.photobtn);
		photoBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
				
				photoBtn.setEnabled(false);
				
				WhichIntent(TargetIntent.Photo);
			}
		});
		
		barPrintLanBtn = (Button)findViewById(R.id.barprintlanbtn);
		barPrintLanBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
				
				barPrintLanBtn.setEnabled(false);
				
				WhichIntent(TargetIntent.BarPrintLan);
			}
		});
		
		stableBtn = (Button)findViewById(R.id.stablebtn);
		stableBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
				
//				stableBtn.setEnabled(false);
//				
//				WhichTest = STABLE;
//				
//				WhichIntent(TargetIntent.Blank);
			}
		});
		
		netBtn = (Button)findViewById(R.id.netbtn);
		netBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
				
				netBtn.setEnabled(false);
				
				WhichIntent(TargetIntent.Network);
			}
		});
	}
	
	public void TestInit() {
		
		TimerDisplay.timerState = whichClock.TestClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		
		PackageInfo pi = null;

		try {

			pi = getPackageManager().getPackageInfo(getPackageName(), 0);

		} catch (NameNotFoundException e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		}

		String verSion = pi.versionName;
		
		versionText.setText(verSion);
	}
	
	public void CurrTimeDisplay() {
		
		new Thread(new Runnable() {
		    public void run() {    
		        runOnUiThread(new Runnable(){
		            public void run() {
		            	
//		            	Log.w("SettingTimeDisplay", "run");
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
	
	public void WhichIntent(TargetIntent Itn) { // Activity conversion
		
		switch(Itn) {
		
		case Home			:				
			Intent HomeIntent = new Intent(getApplicationContext(), HomeActivity.class);
			startActivity(HomeIntent);
			break;
					
		case Blank			:				
			Intent BlankIntent = new Intent(getApplicationContext(), BlankActivity.class);
			startActivity(BlankIntent);
			break;
			
		case Shaking		:				
			Intent ShakingIntent = new Intent(getApplicationContext(), ShakingActivity.class);
			startActivity(ShakingIntent);
			break;
		
		case Photo			:				
			Intent PhotoIntent = new Intent(getApplicationContext(), PhotoActivity.class);
			startActivity(PhotoIntent);
			break;
		
		case BarPrintLan	:				
			Intent BarPriLanIntent = new Intent(getApplicationContext(), BarPriLanActivity.class);
			startActivity(BarPriLanIntent);
			break;
		
		case ExtScan		:				
			Intent ExtScanIntent = new Intent(getApplicationContext(), ExtScanActivity.class);
			startActivity(ExtScanIntent);
			break;
		
		case Network		:				
			Intent NetworkIntent = new Intent(getApplicationContext(), EthernetActivity.class);
			startActivity(NetworkIntent);
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