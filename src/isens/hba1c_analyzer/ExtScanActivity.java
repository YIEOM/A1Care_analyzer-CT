
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

public class ExtScanActivity extends Activity {
	
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
				
				TestStart();
			}
		});
		
		cancelBtn = (Button)findViewById(R.id.cancelbtn);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				cancelBtn.setEnabled(false);
				runBtn.setEnabled(true);
				
				TestCancel();
			}
		});
		
		errorBtn = (Button)errorPopupView.findViewById(R.id.errorbtn);
		errorBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
				
				TestRestore();
			}
		});
		
		Init();
	}
	
	public void Init() {
		
		TimerDisplay.timerState = whichClock.ExtScanClock;		
		CurrTimeDisplay();
		
		titleText.setText("");
		testSubState.setTextColor(Color.parseColor("#04A458"));
		testSubState.setText("READY");
		testSubState.setText("");
		text1Title.setText("");
		text2Title.setText("");
	}
	
	public void TestStart() {
		
		
	}

	public void TestCancel() {
		
		
	}
	
	public void TestRestore() {
		
		
	}
	
	public void ErrorDisplay(String str) {
		
		TextView errorText = (TextView) errorPopupView.findViewById(R.id.errortext);
		
		errorText.setText(str);
		
		errorPopup.showAtLocation(testSubLayout, Gravity.CENTER, 0, 0);
		errorPopup.setAnimationStyle(0);
		
		errorBtn.setEnabled(true);
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
	
	public void TimerInit() {
		
		OneSecondPeriod = new TimerTask() {
			
			public void run() {
				Runnable updater = new Runnable() {
					public void run() {
						
						PhotoDisplay();
					}
				};
				
				handler.post(updater);		
			}
		};
		
		timer = new Timer();
		timer.schedule(OneSecondPeriod, 0, 1000); // Timer period : 1sec
	}
	
	public void PhotoDisplay() {
		
		
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