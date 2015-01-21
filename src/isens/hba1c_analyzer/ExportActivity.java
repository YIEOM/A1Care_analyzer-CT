package isens.hba1c_analyzer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ExportActivity extends Activity {

	private UsbManager mUSBManager;
	private Reader mNFCReader;
	
	public static TextView TimeText;
	private static ImageView deviceImage;
	public TextView explain;
	
	private Button nextBtn;
	private Button backIcon;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.export);
		
		TimeText = (TextView) findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);				
		explain = (TextView) findViewById(R.id.explain);
		
		MemoryInit();

		/*Load to data activation*/
		nextBtn = (Button)findViewById(R.id.nextbtn);
		nextBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				nextBtn.setEnabled(false);
				
			}
		});

		/*Memory Activity activation*/
		backIcon = (Button)findViewById(R.id.backicon);
		backIcon.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				backIcon.setEnabled(false);
				
				WhichIntent();
			}
		});
	}	
	
	private void MemoryInit() {
		
		TimerDisplay.timerState = whichClock.ExportClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		
		Check();
		
		SerialPort.Sleep(300);
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

	public void WhichIntent() { // Activity conversion
			
		Intent CancleIntent = new Intent(getApplicationContext(), DataSettingActivity.class);
		startActivity(CancleIntent);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		finish();
	}
	
	public void Check() {
		
//		UsbManager manager =(UsbManager) getSystemService(Context.USB_SERVICE);
//		UsbAccessory[] accessoryList = manager.getAccessoryList();
//		
//		Log.w("check", "accessory : " + accessoryList);
	}
	
	public void finish() {
		
		super.finish();
		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
}
