package isens.hba1c_analyzer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.R.string;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class LanguageActivity extends Activity {
	
	final static int KO = 0,
					 EN = 1;
	
	private Button escBtn,
				   leftBtn,
				   rightBtn;
	
	private static TextView TimeText;
	private static ImageView deviceImage;
	private TextView languageText;
	
	private String[] languageTable = new String[] {"ko", "en"};
	
	private int idx = 0;
	public boolean btnState = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.language);
		
		TimeText = (TextView) findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);
		
		languageText = (TextView) findViewById(R.id.languagetext);
		
		/*SystemSetting Activity activation*/
		escBtn = (Button)findViewById(R.id.escicon);
		escBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;

					escBtn.setEnabled(false);
					
					SetLocale();
					
					WhichIntent(TargetIntent.SystemSetting);
				}
			}
		});
		
		leftBtn = (Button)findViewById(R.id.leftbtn);
		leftBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;

					LanguageFront();
				}
			}
		});
			
		rightBtn = (Button)findViewById(R.id.rightbtn);
		rightBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;

					LanguageBack();
				}
			}
		});
		
		LanguageInit();
	}
	
	public void LanguageInit() {
		
		TimerDisplay.timerState = whichClock.LanguageClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		
		GetLanguage();
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

	public void GetLanguage() {
		
		int i;
		
		Locale systemLocale = getResources().getConfiguration().locale;
		String language = systemLocale.getLanguage();
		
		for(i = 0; i < languageTable.length; i++) {
			
			idx = i;
			
			if(languageTable[i].equals(language)) break;
		}
		
		LanguageDisplay();
	}
	
	public void LanguageDisplay() {
		
		switch(idx) {
		
		case KO	:
			languageText.setText(R.string.korean);
			break;
			
		case EN	:
			languageText.setText(R.string.english);
			break;
			
		default	:
			break;
		}
		
		btnState = false;
	}
	
	public void LanguageFront() {
		
		if(idx-- == 0) idx = languageTable.length - 1;
		
		LanguageDisplay();
	}
	
	private void LanguageBack() {
		
		if(idx++ == (languageTable.length - 1)) idx = 0;
		
		LanguageDisplay();
	}
	
	public void SetLocale() {
		
		try {
			
			Locale locale = new Locale(languageTable[idx]);
			
			Class amnClass = Class.forName("android.app.ActivityManagerNative");
			Object amn = null;
			Configuration config = null;
			
			Method methodGetDefault = amnClass.getMethod("getDefault");
			methodGetDefault.setAccessible(true);
			amn = methodGetDefault.invoke(amnClass);
			
			Method methodGetConfiguration = amnClass.getMethod("getConfiguration");
			methodGetConfiguration.setAccessible(true);
			config = (Configuration) methodGetConfiguration.invoke(amn);
			
			Class configClass = config.getClass();
			Field f = configClass.getField("userSetLocale");
			f.setBoolean(config, true);
			
			config.locale = locale;
			
			Method methodUpdateConfiguration = amnClass.getMethod("updateConfiguration", Configuration.class);
			methodUpdateConfiguration.setAccessible(true);
			methodUpdateConfiguration.invoke(amn, config);
		}
		catch(Exception e) {
			
		}
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
