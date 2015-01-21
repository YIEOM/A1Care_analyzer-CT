package isens.hba1c_analyzer;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DateActivity extends Activity {

	private TimerDisplay DateTimer;
	
	public Handler handler = new Handler();
	public TimerTask oneHundredmsPeriod;
	
	public Timer timer;
	
	private TextView yearText,
					 monthText,
					 dayText;
	
	private Calendar c;
	
	private Button escBtn,
				   yPlusBtn,
				   yMinusBtn,
				   mPlusBtn,
				   mMinusBtn,
				   dPlusBtn,
				   dMinusBtn;
	
	private static TextView TimeText;
	private static ImageView deviceImage;
	
	public static int WhichDate = 0;
	
	private int year,
				month,
				day;
	
	private boolean btnState = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.date);
		
		TimeText  = (TextView) findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);
		yearText  = (TextView) findViewById(R.id.yeartext);
		monthText = (TextView) findViewById(R.id.monthtext);
		dayText   = (TextView) findViewById(R.id.daytext);
		
		/*Home Activity activation*/
		escBtn = (Button)findViewById(R.id.escicon);
		escBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
				
					escBtn.setEnabled(false);
					
					DateSave();
					
					WhichIntent(TargetIntent.SystemSetting);
				}
			}
		});
		
		yPlusBtn = (Button) findViewById(R.id.yplusbtn);
		yPlusBtn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch(event.getAction()) {
				
				case MotionEvent.ACTION_DOWN	:
					if(!btnState) {
						
						btnState = true;
						
						DateChange(HomeActivity.YEAR_UP);
					}
					break;

				case MotionEvent.ACTION_UP		:
					if(timer != null) timer.cancel();
					break;
				}
				// TODO Auto-generated method stub
				return false;
			}
		});

		yPlusBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
			
				TimerInit(HomeActivity.YEAR_UP);
				
				return true;
			}
		});
		
		yMinusBtn = (Button) findViewById(R.id.yminusbtn);
		yMinusBtn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch(event.getAction()) {
				
				case MotionEvent.ACTION_DOWN	:
					if(!btnState) {
						
						btnState = true;
						
						DateChange(HomeActivity.YEAR_DOWN);
					}
					break;

				case MotionEvent.ACTION_UP		:
					if(timer != null) timer.cancel();
					break;
				}
				// TODO Auto-generated method stub
				return false;
			}
		});

		yMinusBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
			
				TimerInit(HomeActivity.YEAR_DOWN);
				
				return true;
			}
		});
		
		mPlusBtn = (Button) findViewById(R.id.mplusbtn);
		mPlusBtn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch(event.getAction()) {
				
				case MotionEvent.ACTION_DOWN	:
					if(!btnState) {
						
						btnState = true;
						
						DateChange(HomeActivity.MONTH_UP);
					}
					break;

				case MotionEvent.ACTION_UP		:
					if(timer != null) timer.cancel();
					break;
				}
				// TODO Auto-generated method stub
				return false;
			}
		});

		mPlusBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
			
				TimerInit(HomeActivity.MONTH_UP);
				
				return true;
			}
		});
		
		mMinusBtn = (Button) findViewById(R.id.mminusbtn);
		mMinusBtn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch(event.getAction()) {
				
				case MotionEvent.ACTION_DOWN	:
					if(!btnState) {
						
						btnState = true;
						
						DateChange(HomeActivity.MONTH_DOWN);
					}
					break;

				case MotionEvent.ACTION_UP		:
					if(timer != null) timer.cancel();
					break;
				}
				// TODO Auto-generated method stub
				return false;
			}
		});

		mMinusBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
			
				TimerInit(HomeActivity.MONTH_DOWN);
				
				return true;
			}
		});
		
		dPlusBtn = (Button) findViewById(R.id.dplusbtn);
		dPlusBtn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch(event.getAction()) {
				
				case MotionEvent.ACTION_DOWN	:
					if(!btnState) {
						
						btnState = true;
						
						DateChange(HomeActivity.DAY_UP);
					}
					break;

				case MotionEvent.ACTION_UP		:
					if(timer != null) timer.cancel();
					break;
				}
				// TODO Auto-generated method stub
				return false;
			}
		});

		dPlusBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
			
				TimerInit(HomeActivity.DAY_UP);
				
				return true;
			}
		});

		dMinusBtn = (Button) findViewById(R.id.dminusbtn);
		dMinusBtn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch(event.getAction()) {
				
				case MotionEvent.ACTION_DOWN	:
					if(!btnState) {
						
						btnState = true;
						
						DateChange(HomeActivity.DAY_DOWN);
					}
					break;

				case MotionEvent.ACTION_UP		:
					if(timer != null) timer.cancel();
					break;
				}
				// TODO Auto-generated method stub
				return false;
			}
		});

		dMinusBtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
			
				TimerInit(HomeActivity.DAY_DOWN);
				
				return true;
			}
		});
		
		DateInit();
	}
	
	public void DateInit() {
		
		TimerDisplay.timerState = whichClock.DateClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		GetCurrDate();
	}
		
	public void TimerInit(final int whichDate) {
		
		oneHundredmsPeriod = new TimerTask() {
			
			public void run() {
				Runnable updater = new Runnable() {
					public void run() {
		
						DateChange(whichDate);
					}
				};
				
				handler.post(updater);		
			}
		};
		
		timer = new Timer();
		timer.schedule(oneHundredmsPeriod, 0, 100); // Timer period : 100msec
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
	
	public synchronized void DateDisplay() { // displaying date parameter
		
		yearText.setText(Integer.toString(year));
		monthText.setText(Integer.toString(month));
		dayText.setText(Integer.toString(day));
		
		btnState = false;
	}
	
	public void GetCurrDate() { // acquiring date parameter displayed
		
		c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH) + 1;
		day = c.get(Calendar.DAY_OF_MONTH);
		
		DateDisplay();
	}
	
	public void GetDate() { // getting the calendar data for date
		
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH) + 1;
		day = c.get(Calendar.DAY_OF_MONTH);
		
		DateDisplay();
	}
	
	private void DateChange(int whichDate) {
		
		switch(whichDate) {
		
		case HomeActivity.YEAR_UP		:
			c.add(Calendar.YEAR, 1);
			break;
			
		case HomeActivity.YEAR_DOWN		:
			c.add(Calendar.YEAR, -1);
			break;
		
		case HomeActivity.MONTH_UP		:
			c.add(Calendar.MONTH, 1);
			break;
			
		case HomeActivity.MONTH_DOWN	:
			c.add(Calendar.MONTH, -1);
			break;
		
		case HomeActivity.DAY_UP		:
			c.add(Calendar.DAY_OF_MONTH, 1);
			break;
			
		case HomeActivity.DAY_DOWN		:
			c.add(Calendar.DAY_OF_MONTH, -1);
			break;
			
		default		:
			break;
		}
		
		GetDate();
	}
	
	private void DateSave() { // saving the date modified
		
		TimerDisplay.OneHundredmsPeriod.cancel(); // finishing the running timer 
		
		SystemClock.setCurrentTimeMillis(c.getTimeInMillis());

		DateTimer = new TimerDisplay();
		DateTimer.TimerInit(); // starting the timer

		SerialPort.Sleep(1000);
	}
	
	private void WhichIntent(TargetIntent Itn) { // Activity conversion
		
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
