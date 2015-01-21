package isens.hba1c_analyzer;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class AdjustmentFactorActivity extends Activity {
	
	public CustomKeyboard mCustomKeyboard;
	
	private Button escBtn;
	
	private EditText slopeEText, 
					 offsetEText;
		
	private static TextView TimeText;
	private static ImageView deviceImage;
	
	private boolean btnState = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.adjustment);
		
		TimeText = (TextView)findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);
				
		slopeEText = (EditText) findViewById(R.id.slopeetext);
		offsetEText = (EditText) findViewById(R.id.offsetetext);
		
		if(HomeActivity.ANALYZER_SW == HomeActivity.DEVEL) {
		
			/* */
			
			mCustomKeyboard = new CustomKeyboard(this, R.id.customkeyboard, R.layout.numkeyboard);
			mCustomKeyboard.RegisterEditText(R.id.offsetetext);
		
			/* */
		}
		
		/*System setting Activity activation*/
		escBtn = (Button)findViewById(R.id.escicon);
		escBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				if(!btnState) {
					
					btnState = true;
				
					escBtn.setEnabled(false);
					
					AdjustmentSave(Float.valueOf(slopeEText.getText().toString()).floatValue(), Float.valueOf(offsetEText.getText().toString()).floatValue());
					
					WhichIntent(TargetIntent.SystemSetting);
				}
			}
		});
		
		AdjustmentInit();
	}
	
	public void AdjustmentInit() {
		
		TimerDisplay.timerState = whichClock.AdjustmentClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		
		slopeEText.setText(Float.toString(RunActivity.AF_Slope));
		offsetEText.setText(Float.toString(RunActivity.AF_Offset));
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
	
	public void AdjustmentSave(float slope, float offset) { // Saving number of user define parameter
		
		SharedPreferences adjustmentPref = getSharedPreferences("User Define", MODE_PRIVATE);
		SharedPreferences.Editor adjustmentedit = adjustmentPref.edit();
		
		adjustmentedit.putFloat("AF SlopeVal", slope);
		adjustmentedit.putFloat("AF OffsetVal", offset);
		adjustmentedit.commit();
		
		RunActivity.AF_Slope = slope;
		RunActivity.AF_Offset = offset;
	}
	
	public void WhichIntent(TargetIntent Itn) { // Activity conversion
		
		switch(Itn) {
		
		case SystemSetting	:
			Intent SystemSettingIntent = new Intent(getApplicationContext(), SystemSettingActivity.class);
			startActivity(SystemSettingIntent);
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