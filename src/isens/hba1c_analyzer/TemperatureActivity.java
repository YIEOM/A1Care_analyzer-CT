package isens.hba1c_analyzer;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
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

public class TemperatureActivity extends Activity {
	
	private Temperature TemperatureTemp;
	
	private Button escBtn,
				   setBtn,
				   readBtn;	
	
	private TextView tmptext;
	
	private EditText tmpEText;
		
	public static TextView TimeText;
	private static ImageView deviceImage;
	
		protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.temperature);
		
		TimeText = (TextView)findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);
		
		tmptext =  (TextView)findViewById(R.id.tmptext);
		
		tmpEText = (EditText) findViewById(R.id.tmpetext);
		
		
		/*System setting Activity activation*/
		escBtn = (Button)findViewById(R.id.escicon);
		escBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				escBtn.setEnabled(false);
				
				WhichIntent(TargetIntent.SystemSetting);
			}
		});
		
		setBtn = (Button)findViewById(R.id.setbtn);
		setBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				setBtn.setEnabled(false);
				
				TmpSave(Float.valueOf(tmpEText.getText().toString()).floatValue());
			}
		});
		
		readBtn = (Button)findViewById(R.id.readbtn);
		readBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				readBtn.setEnabled(false);
				
				TmpDisplay();
			}
		});
		
		TemperatureInit();
	}
	
	public void TemperatureInit() {
		
		TimerDisplay.timerState = whichClock.TemperatureClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		
		tmpEText.setText(Float.toString(Temperature.InitTmp));
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

	public void TmpSave(float tmp) {
		
		SharedPreferences temperaturePref = getSharedPreferences("Temperature", MODE_PRIVATE);
		SharedPreferences.Editor temperatureedit = temperaturePref.edit();
		
		temperatureedit.putFloat("Cell Block", tmp);
		temperatureedit.commit();
		
		Temperature.InitTmp = tmp;
		
		TemperatureTemp = new Temperature();
		TemperatureTemp.TmpInit();
		
		setBtn.setEnabled(true);
	}
	
	public void TmpDisplay() {
		
		DecimalFormat tmpdfm = new DecimalFormat("0.0");
		double tmpDouble;
		
		TemperatureTemp = new Temperature();
		tmpDouble = TemperatureTemp.CellTmpRead();
		
		tmptext.setText(tmpdfm.format(tmpDouble));
		
		readBtn.setEnabled(true);
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