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
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

public class DataSettingActivity extends Activity {
	
	private Button backBtn,
		   		   homeBtn,
		   		   exportBtn;
		   		   
	private static TextView TimeText;
	private static ImageView deviceImage;
	
	private boolean btnState = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.datasetting);
		
		TimeText = (TextView) findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);
		
		DataSettingInit();
					
		/*Setting Activity activation*/
		backBtn = (Button)findViewById(R.id.backicon);
		backBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;

					backBtn.setEnabled(false);
					
					WhichIntent(TargetIntent.Setting);
				}
			}
		});
		
		/*Home Activity activation*/
		homeBtn = (Button)findViewById(R.id.homeicon);
		homeBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;

					homeBtn.setEnabled(false);

					WhichIntent(TargetIntent.Home);
				}
			}
		});
		
		/*Export Activity activation*/
		exportBtn = (Button)findViewById(R.id.exportbtn);
		exportBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				if(!btnState) {
					
					btnState = true;

					exportBtn.setEnabled(false);
				
					WhichIntent(TargetIntent.Export);
				}
			}
		});
	}
	
	public void DataSettingInit() {
		
		TimerDisplay.timerState = whichClock.DataSettingClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
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

	public void WhichIntent(TargetIntent Itn) { // Activity conversion
		
		switch(Itn) {
		
		case Home		:	
			Intent HomeIntent = new Intent(getApplicationContext(), HomeActivity.class);
			startActivity(HomeIntent);
			break;
			
		case Setting	:
			Intent SettingIntent = new Intent(getApplicationContext(), SettingActivity.class);
			startActivity(SettingIntent);
			break;
			
		case Export		:
			Intent ExportIntent = new Intent(getApplicationContext(), ExportActivity.class);
			startActivity(ExportIntent);
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