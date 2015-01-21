package isens.hba1c_analyzer;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MemoryActivity extends Activity {

	final static byte CONTROL = 1,
					  PATIENT = 2;
	
	public static TextView TimeText;
	private static ImageView deviceImage;
	
	private Button patientBtn,
				   controlBtn,
				   backIcon;
	
	public static int DataPage;
	
	public boolean btnState = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.memory);
		
		TimeText = (TextView) findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);				
		
		MemoryInit();

		/*Patient Test Activity activation*/
		patientBtn = (Button)findViewById(R.id.patientbtn);
		patientBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				if(!btnState) {
					
					btnState = true;
					
					patientBtn.setEnabled(false);
					
					WhichIntent(TargetIntent.PatientFileLoad);
				}
			}
		});
		
		/*Control Test Activity activation*/
		controlBtn = (Button)findViewById(R.id.controlbtn);
		controlBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				if(!btnState) {
					
					btnState = true;
					
					controlBtn.setEnabled(false);
				
					WhichIntent(TargetIntent.ControlFileLoad);
				}
			}
		});
	
		/*Home Activity activation*/
		backIcon = (Button)findViewById(R.id.backicon);
		backIcon.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				if(!btnState) {
					
					btnState = true;
					
					backIcon.setEnabled(false);
				
					WhichIntent(TargetIntent.Home);
				}
			}
		});
	}	
	
	public void MemoryInit() {
		
		TimerDisplay.timerState = whichClock.MemoryClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		
		DataPage = 0;
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
		
		case Home				:
			Intent HomeIntent = new Intent(getApplicationContext(), HomeActivity.class);
			startActivity(HomeIntent);
			break;
			
		case ControlFileLoad	:
			Intent ControlFileLoadIntent = new Intent(getApplicationContext(), FileLoadActivity.class);
			ControlFileLoadIntent.putExtra("DataCnt", RemoveActivity.ControlDataCnt); // delivering recent data number
			ControlFileLoadIntent.putExtra("DataPage", DataPage);
			ControlFileLoadIntent.putExtra("Type", (int) CONTROL);
			startActivity(ControlFileLoadIntent);
			break;
			
		case PatientFileLoad	:
			Intent PatientFileLoadIntent = new Intent(getApplicationContext(), FileLoadActivity.class);
			PatientFileLoadIntent.putExtra("DataCnt", RemoveActivity.PatientDataCnt); // delivering recent data number
			PatientFileLoadIntent.putExtra("DataPage", DataPage);
			PatientFileLoadIntent.putExtra("Type", (int) PATIENT);
			startActivity(PatientFileLoadIntent);
			break;
			
		default					:
			break;			
		}
		
		finish();		
	}
	
	public void finish() {
		
		super.finish();
//		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
}
