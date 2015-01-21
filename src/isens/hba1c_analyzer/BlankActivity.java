package isens.hba1c_analyzer;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.RunActivity.AnalyzerState;
import isens.hba1c_analyzer.SystemCheckActivity.MotorCheck;
import isens.hba1c_analyzer.SystemCheckActivity.TemperatureCheck;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BlankActivity extends Activity {

	private SerialPort BlankSerial;
	private RunActivity BlankRun;
	
	private RelativeLayout blankLinear;
	
	private View errorPopupView;
	private PopupWindow errorPopup;
	
	private static TextView TimeText;
	private static ImageView deviceImage;
	private ImageView barani;
	
	private RunActivity.AnalyzerState blankState;
	private byte photoCheck;
	
	private byte checkError = HomeActivity.NORMAL_OPERATION;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.blank);
		
		/* Error Pop-up window */
		blankLinear = (RelativeLayout)findViewById(R.id.blanklinear);		
		errorPopupView = View.inflate(getApplicationContext(), R.layout.errorpopup, null);
		errorPopup = new PopupWindow(errorPopupView, 800, 480, true);
		
		TimeText = (TextView) findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);
		
		barani = (ImageView) findViewById(R.id.progressBar);
		
		BlankInit();
	}                     
	
	public void BlankInit() {
		
		TimerDisplay.timerState = whichClock.BlankClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
				
		BlankSerial = new SerialPort();
		BlankRun = new RunActivity();
		
		blankState = RunActivity.AnalyzerState.InitPosition;
		photoCheck = 0;
		
		TimerDisplay.RXBoardFlag = true;
		SensorCheck SensorCheckObj = new SensorCheck();
		SensorCheckObj.start();
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

	public class SensorCheck extends Thread {
		
		public void run() {
			
			GpioPort.DoorActState = true;			
			GpioPort.CartridgeActState = true;
			
			SerialPort.Sleep(1500);
			
			if(ActionActivity.CartridgeCheckFlag != 0) ErrorPopup(HomeActivity.CART_SENSOR_ERROR);
			while(ActionActivity.CartridgeCheckFlag != 0) {
				
				SerialPort.Sleep(100);
			}
			errorPopup.dismiss();
			
			if(ActionActivity.DoorCheckFlag != 1) ErrorPopup(HomeActivity.DOOR_SENSOR_ERROR);
			while(ActionActivity.DoorCheckFlag != 1) {
				
				SerialPort.Sleep(100);
			}
			errorPopup.dismiss();
			 
			GpioPort.DoorActState = false;			
			GpioPort.CartridgeActState = false;

			BlankStep BlankStepObj = new BlankStep();
			BlankStepObj.start();
		}
	}
	
	public class BlankStep extends Thread { // Blank run
		
		public void run() {
			
			for(int i = 0; i < 9; i++) {
				
				switch(blankState) {
				
				case InitPosition		:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.MeasurePosition, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 6);
					break; 
				
				case MeasurePosition :
					MotionInstruct(RunActivity.MEASURE_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.MEASURE_POSITION, AnalyzerState.FilterDark, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 5);
					BarAnimation(178);
					break;
					
				case FilterDark :
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.Filter535nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 5);
					BarAnimation(206);
					RunActivity.BlankValue[0] = 0;
					RunActivity.BlankValue[0] = AbsorbanceMeasure(HomeActivity.MinDark, HomeActivity.MaxDark, HomeActivity.ERROR_DARK); // Dark Absorbance

					/* TEST Mode */
					if(HomeActivity.ANALYZER_SW == HomeActivity.NORMAL)
						
					PhotoErrorCheck();
					break;
					
				case Filter535nm :
					/* 535nm filter Measurement */
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter660nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 5);
					BarAnimation(290);
					RunActivity.BlankValue[1] = AbsorbanceMeasure(HomeActivity.Min535, HomeActivity.Max535, HomeActivity.ERROR_535nm); // Dark Absorbance
					break;
				
				case Filter660nm :
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter750nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 5);
					BarAnimation(374);
					RunActivity.BlankValue[2] = AbsorbanceMeasure(HomeActivity.Min660, HomeActivity.Max660, HomeActivity.ERROR_660nm); // Dark Absorbance
					break;
				
				case Filter750nm :
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.FilterHome, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 5);
					BarAnimation(458);
					RunActivity.BlankValue[3] = AbsorbanceMeasure(HomeActivity.Min750, HomeActivity.Max750, HomeActivity.ERROR_750nm); // Dark Absorbance
				
					/* TEST Mode */
					if(HomeActivity.ANALYZER_SW == HomeActivity.NORMAL)
					
					PhotoErrorCheck();
					break;
				
				case FilterHome :
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.CartridgeHome, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 5);
					BarAnimation(542);
					break;
				
				case CartridgeHome :
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NormalOperation, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 5);
					BarAnimation(579);
					break;
					
				case NormalOperation	:
					SerialPort.Sleep(1000);
					WhichIntent(TargetIntent.Action);
					break;
				
				case ShakingMotorError	:
					checkError = HomeActivity.SHAKING_MOTOR_ERROR;
					blankState = AnalyzerState.NoWorking;
					WhichIntent(TargetIntent.Home);
					break;
					
				case FilterMotorError	:
					checkError = HomeActivity.FILTER_MOTOR_ERROR;
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 6);
					WhichIntent(TargetIntent.Home);
					break;
				
				case PhotoSensorError	:
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.CartridgeHome, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 5);
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 6);
					WhichIntent(TargetIntent.Home);
					break;
					
				case LampError			:
					checkError = HomeActivity.LAMP_ERROR;
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.CartridgeHome, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 5);
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 6);
					WhichIntent(TargetIntent.Home);
					break;
					
				case NoResponse :
					Log.w("BlankStep", "NR");
					blankState = AnalyzerState.NoWorking;
					WhichIntent(TargetIntent.Home);
					break;
					
				default	:
					break;
				}
			}
		}
	}
	
	public void MotionInstruct(String str, SerialPort.CtrTarget target) { // Motion of system instruction
		
		BlankSerial.BoardTx(str, target);
	}
	
	public synchronized double AbsorbanceMeasure(double min, double max, byte errBits) { // Absorbance measurement
	
		int time = 0;
		String rawValue;
		double douValue = 0;
		
		BlankSerial.BoardTx("VH", SerialPort.CtrTarget.PhotoSet);
		
		rawValue = BlankSerial.BoardMessageOutput();			
		
		while(rawValue.length() != 8) {
		
			rawValue = BlankSerial.BoardMessageOutput();			
				
			if(time++ > 50) {
				
				blankState = AnalyzerState.NoResponse;
			
				break;
			}
		
			SerialPort.Sleep(100);
		}	
		
		if(blankState != AnalyzerState.NoResponse) {

			douValue = Double.parseDouble(rawValue);
			
			if((min < douValue) & (douValue < max)) {
				
				return (douValue - RunActivity.BlankValue[0]);
				
			} else photoCheck += errBits;
		}
		
		return 0.0;
	}
	
	public void PhotoErrorCheck() {
		
		switch(photoCheck) {
		
		case 1	:
			blankState = AnalyzerState.PhotoSensorError;
			checkError = HomeActivity.PHOTO_SENSOR_ERROR;
			break;
			
		case 2	:
			blankState = AnalyzerState.PhotoSensorError;
			checkError = HomeActivity.FILTER_535nm_ERROR;
			break;
			
		case 4	:
			blankState = AnalyzerState.PhotoSensorError;
			checkError = HomeActivity.FILTER_660nm_ERROR;
			break;
			
		case 8	:
			blankState = AnalyzerState.PhotoSensorError;
			checkError = HomeActivity.FILTER_750nm_ERROR;
			break;
			
		case 14	:
			blankState = AnalyzerState.LampError;
			checkError = HomeActivity.LAMP_ERROR;
			break;
			
		default	:
			break;
		}
	}
	
	public void BoardMessage(String colRsp, AnalyzerState nextState, String errRsp, AnalyzerState errState, int rspTime) {
		
		int time = 0;
		String temp = "";
		
		rspTime = rspTime * 10;
		
		while(true) {
			
			temp = BlankSerial.BoardMessageOutput();
			
			if(colRsp.equals(temp)) {
				
				blankState = nextState;
				break;
			
			} else if(errRsp.equals(temp)) {
				
				blankState = errState;
				break;
			}
					
			if(time++ > rspTime) {
				
				blankState = AnalyzerState.NoResponse;
				break;
			}
			
			SerialPort.Sleep(100);
		}
	}
	
	public void BarAnimation(final int x) {

		new Thread(new Runnable() {
		    public void run() {    
		        runOnUiThread(new Runnable(){
		            public void run() {
		
		            	ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(barani.getLayoutParams());
		            	margin.setMargins(x, 273, 0, 0);
		            	barani.setLayoutParams(new RelativeLayout.LayoutParams(margin));
		            }
		        });
		    }
		}).start();	
	}
	
	public void ErrorPopup(final byte error) {
		
		new Thread(new Runnable() {
		    public void run() {    
		        runOnUiThread(new Runnable(){
		            public void run() {
		            			            	
		            	errorPopup.showAtLocation(blankLinear, Gravity.CENTER, 0, 0);
						errorPopup.setAnimationStyle(0);
											
						TextView errorText = (TextView) errorPopup.getContentView().findViewById(R.id.errortext);
						
						switch(error) {
						
						case HomeActivity.DOOR_SENSOR_ERROR		:
							errorText.setText(R.string.w001);
							break;
						
						case HomeActivity.CART_SENSOR_ERROR		:
							errorText.setText(R.string.w002);
							break;
						}
		            }
		        });
		    }
		}).start();
	}
	
	public void WhichIntent(TargetIntent Itn) { // Activity conversion
		
		TimerDisplay.RXBoardFlag = false;
		
		switch(Itn) {
		
		case Home	:				
			Intent HomeIntent = new Intent(getApplicationContext(), HomeActivity.class);
			HomeIntent.putExtra("System Check State", (int) checkError);
			startActivity(HomeIntent);
			break;
			
		case Action	:				
			Intent ActionIntent = new Intent(getApplicationContext(), ActionActivity.class);
			startActivity(ActionIntent);
			break;
			
		default			:	
			break;
		}		
		
		finish();
	}
	
	public void finish() {
		
		super.finish();
		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
}
