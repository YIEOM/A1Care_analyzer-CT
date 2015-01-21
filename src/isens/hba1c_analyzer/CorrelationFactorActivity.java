package isens.hba1c_analyzer;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class CorrelationFactorActivity extends Activity {
	
	private Button escBtn;
	
	private EditText slopeEText, 
					 offsetEText;
	
	private static TextView TimeText;
	private static ImageView deviceImage;
	
	private boolean btnState = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.correlation);
		
		TimeText = (TextView)findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);
				
		slopeEText = (EditText) findViewById(R.id.slopeetext);
		offsetEText = (EditText) findViewById(R.id.offsetetext);
		
		/*System setting Activity activation*/
		escBtn = (Button)findViewById(R.id.escicon);
		escBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				if(!btnState) {
					
					btnState = true;
				
					escBtn.setEnabled(false);
					
					CorrelationSave(Float.valueOf(slopeEText.getText().toString()).floatValue(), Float.valueOf(offsetEText.getText().toString()).floatValue());
					
					WhichIntent(TargetIntent.SystemSetting);
				}
			}
		});
		
		CorrelationInit();
	}
	
	public void CorrelationInit() {
		
		TimerDisplay.timerState = whichClock.CorrelationClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		
		slopeEText.setText(Float.toString(RunActivity.CF_Slope));
		offsetEText.setText(Float.toString(RunActivity.CF_Offset));
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
	public void CorrelationSave(float slope, float offset) { // Saving number of user define parameter
		
		SharedPreferences correlationPref = getSharedPreferences("User Define", MODE_PRIVATE);
		SharedPreferences.Editor correlationedit = correlationPref.edit();
		
		correlationedit.putFloat("CF SlopeVal", slope);
		correlationedit.putFloat("CF OffsetVal", offset);
		correlationedit.commit();
		
		RunActivity.CF_Slope = slope;
		RunActivity.CF_Offset = offset;
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