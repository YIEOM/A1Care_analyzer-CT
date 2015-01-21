package isens.hba1c_analyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import isens.hba1c_analyzer.CalibrationActivity.Cart1stShaking;
import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.RunActivity.AnalyzerState;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SystemCheckActivity extends Activity {

	static byte NUMBER_CELL_BLOCK_TEMP_CHECK = 60;
	static final byte NUMBER_AMBIENT_TEMP_CHECK = 30/5;
	final static String SHAKING_CHECK_TIME = "0030";
	
	private enum TmpState {FirstTmp, SecondTmp, ThirdTmp, ForthTmp, FifthTmp}
	
	private GpioPort SystemGpio;
	private SerialPort SystemSerial;
	private Temperature SystemTmp;
	private TimerDisplay SystemTimer;
	public EthernetControl cEthernet;
	
	private AudioManager audioManager;
	
	private RelativeLayout systemCheckLinear;
	
	private AnimationDrawable systemCheckAni;
	private ImageView systemCheckImage;
	
	private View errorPopupView;
	private PopupWindow errorPopup;
	
	private AnalyzerState systemState;
	
	public TmpState tmpNumber;
	
	private byte checkError;
	private byte photoCheck;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.systemcheck);
		
		/* Error Pop-up window */
		systemCheckLinear = (RelativeLayout)findViewById(R.id.systemchecklayout);		
		errorPopupView = View.inflate(getApplicationContext(), R.layout.errorpopup, null);
		errorPopup = new PopupWindow(errorPopupView, 800, 480, true);
				
		SystemCheckInit();
	}
	
	public void SystemCheckInit() {
		
		SystemAniStart();
	
		/* Serial communication start */
		SystemSerial = new SerialPort();
		SystemSerial.BoardSerialInit();
		SystemSerial.BoardRxStart();
		SystemSerial.PrinterSerialInit();
		SystemSerial.BarcodeSerialInit();
		SystemSerial.BarcodeRxStart();
			
		/* Timer start */
		TimerDisplay.timerState = whichClock.SystemCheckClock;
		SystemTimer = new TimerDisplay();
		SystemTimer.TimerInit();
		SystemTimer.RealTime();
		
		/* Barcode reader off */
		SystemGpio = new GpioPort();
		SystemGpio.TriggerHigh();
		
		cEthernet = new EthernetControl(this, this);
		cEthernet.EthernetUp();
		
		ParameterInit();

		/* Temperature setting */
		SystemTmp = new Temperature(); // to test
		SystemTmp.TmpInit(); // to test
		
		BrightnessInit();
		
		VolumeInit();
		
		/* TEST Mode */
		if(HomeActivity.ANALYZER_SW == HomeActivity.DEVEL | HomeActivity.ANALYZER_SW == HomeActivity.CE) {
			
			Stable mStable = new Stable();
			mStable.start();
			
		} else {
			
//		TimerDisplay.RXBoardFlag = true;
//		SensorCheck SensorCheckObj = new SensorCheck();
//		SensorCheckObj.start();
		
		
		}
	}
	
	public class Stable extends Thread {
		
		public void run() {
			
//			SerialPort.Sleep(300000);
			
			WhichIntent(TargetIntent.Home);
		}
	}
	
	public class SensorCheck extends Thread {
		
		public void run() {
			
			GpioPort.DoorActState = true;			
			GpioPort.CartridgeActState = true;
			
			SerialPort.Sleep(2000);
			
			if(ActionActivity.CartridgeCheckFlag != 0) ErrorPopup(HomeActivity.CART_SENSOR_ERROR);
			while(ActionActivity.CartridgeCheckFlag != 0);
			errorPopup.dismiss();
			
			if(ActionActivity.DoorCheckFlag != 1) ErrorPopup(HomeActivity.DOOR_SENSOR_ERROR);
			while(ActionActivity.DoorCheckFlag != 1);
			errorPopup.dismiss();
			 
			GpioPort.DoorActState = false;			
			GpioPort.CartridgeActState = false;

			MotorCheck MotorCheckObj = new MotorCheck();
			MotorCheckObj.start();
		}
	}
	
	public class MotorCheck extends Thread {
		
		public void run() {
			
			for(int i = 0; i < 17; i++) {
				
				switch(systemState) {
				
				case InitPosition		:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					MotorCheck(RunActivity.HOME_POSITION, AnalyzerState.Step1Position, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 6);
					break;
				
				case Step1Position		:
					MotionInstruct(RunActivity.Step1st_POSITION, SerialPort.CtrTarget.PhotoSet);			
					MotorCheck(RunActivity.Step1st_POSITION, AnalyzerState.Step1Shaking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 1);
					break;
					
				case Step1Shaking		:
					MotionInstruct(SHAKING_CHECK_TIME, SerialPort.CtrTarget.MotorSet);
					MotorCheck(RunActivity.MOTOR_COMPLETE, AnalyzerState.Step2Position, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 6);
					break;
					
				case Step2Position		:
					MotionInstruct(RunActivity.Step2nd_POSITION, SerialPort.CtrTarget.PhotoSet);
					MotorCheck(RunActivity.Step2nd_POSITION, AnalyzerState.Step2Shaking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 2);
					break;
					
				case Step2Shaking		:
					MotionInstruct(SHAKING_CHECK_TIME, SerialPort.CtrTarget.MotorSet);
					MotorCheck(RunActivity.MOTOR_COMPLETE, AnalyzerState.MeasurePosition, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 6);
					break;
					
				case MeasurePosition	:
					MotionInstruct(RunActivity.MEASURE_POSITION, SerialPort.CtrTarget.PhotoSet);
					MotorCheck(RunActivity.MEASURE_POSITION, AnalyzerState.MeasureDark, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 2);
					break;
					
				case MeasureDark		:
					PhotoCheck(AnalyzerState.Filter535nm, HomeActivity.MaxDark, HomeActivity.MinDark, HomeActivity.ERROR_DARK, 1);
					PhotoErrorCheck();
					break;
					
				case Filter535nm		:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					MotorCheck(RunActivity.NEXT_FILTER, AnalyzerState.Measure535nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 2);
					break;
					
				case Measure535nm		:
					PhotoCheck(AnalyzerState.Filter660nm, HomeActivity.Max535, HomeActivity.Min535, HomeActivity.ERROR_535nm, 1);
					break;
					
				case Filter660nm		:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					MotorCheck(RunActivity.NEXT_FILTER, AnalyzerState.Measure660nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 2);
					break;
					
				case Measure660nm		:
					PhotoCheck(AnalyzerState.Filter750nm, HomeActivity.Max660, HomeActivity.Min660, HomeActivity.ERROR_660nm, 1);
					break;
					
				case Filter750nm		:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					MotorCheck(RunActivity.NEXT_FILTER, AnalyzerState.Measure750nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 2);
					break;
					
				case Measure750nm		:
					PhotoCheck(AnalyzerState.FilterDark, HomeActivity.Max750, HomeActivity.Min750, HomeActivity.ERROR_750nm, 1);
					PhotoErrorCheck();
					break;
					
				case FilterDark			:
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					MotorCheck(RunActivity.FILTER_DARK, AnalyzerState.CartridgeDump, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 1);
					break;
					
				case CartridgeDump		:
					MotionInstruct(RunActivity.CARTRIDGE_DUMP, SerialPort.CtrTarget.PhotoSet);
					MotorCheck(RunActivity.CARTRIDGE_DUMP, AnalyzerState.CartridgeHome, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 6);
					break;
					
				case CartridgeHome		:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);
					MotorCheck(RunActivity.HOME_POSITION, AnalyzerState.NormalOperation, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 2);
					break;
					
				case NormalOperation	:
//					if((HomeActivity.ANALYZER_SW == HomeActivity.NORMAL) || (HomeActivity.ANALYZER_SW == HomeActivity.CE)) {
					if(HomeActivity.ANALYZER_SW == HomeActivity.NORMAL) {
						
						TemperatureCheck TemperatureCheckObj = new TemperatureCheck();
						TemperatureCheckObj.start();
					
					} else WhichIntent(TargetIntent.Home);
					break;
					
				case ShakingMotorError	:
					checkError = HomeActivity.SHAKING_MOTOR_ERROR;
					systemState = AnalyzerState.NoWorking;
					WhichIntent(HomeActivity.TargetIntent.Home);
					break;
					
				case FilterMotorError	:
					checkError = HomeActivity.FILTER_MOTOR_ERROR;
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					MotorCheck(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 6);
					WhichIntent(HomeActivity.TargetIntent.Home);
					break;
				
				case PhotoSensorError	:
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					MotorCheck(RunActivity.FILTER_DARK, AnalyzerState.NoWorking, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 3);
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					MotorCheck(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 6);
					WhichIntent(HomeActivity.TargetIntent.Home);
					break;
					
				case LampError			:
					checkError = HomeActivity.LAMP_ERROR;
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					MotorCheck(RunActivity.FILTER_DARK, AnalyzerState.NoWorking, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError, 3);
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					MotorCheck(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError, 6);
					WhichIntent(HomeActivity.TargetIntent.Home);
					break;
					
				case NoResponse			:
					checkError = HomeActivity.COMMUNICATION_ERROR;
					systemState = AnalyzerState.NoWorking;
					WhichIntent(HomeActivity.TargetIntent.Home);
					break;
					
				default	:
					break;
				}
			}
		}
	}
	
	public class TemperatureCheck extends Thread {
		
		public void run() {
			
			int i;
			double tmp;
			tmpNumber = TmpState.FirstTmp;
			
			for(i = 0; i < NUMBER_CELL_BLOCK_TEMP_CHECK; i++) {
				
				tmp = SystemTmp.CellTmpRead();
				Log.w("TemperatureCheck", "Cell Temperature : " + tmp);
				
				switch(tmpNumber) {
				
				case FirstTmp	:
					if(((Temperature.InitTmp - 1) < tmp) || (tmp < (Temperature.InitTmp + 1))) tmpNumber = TmpState.SecondTmp;
					break;
					
				case SecondTmp	:
					if(((Temperature.InitTmp - 1) < tmp) || (tmp < (Temperature.InitTmp + 1))) tmpNumber = TmpState.ThirdTmp;
					else tmpNumber = TmpState.FirstTmp;
					break;
					
				case ThirdTmp	:
					if(((Temperature.InitTmp - 1) < tmp) || (tmp < (Temperature.InitTmp + 1))) tmpNumber = TmpState.ForthTmp;
					else tmpNumber = TmpState.FirstTmp;
					break;
					
				case ForthTmp	:
					if(((Temperature.InitTmp - 1) < tmp) || (tmp < (Temperature.InitTmp + 1))) tmpNumber = TmpState.FifthTmp;
					else tmpNumber = TmpState.FirstTmp;
					break;
					
				case FifthTmp	:
					if(((Temperature.InitTmp - 1) < tmp) || (tmp < (Temperature.InitTmp + 1))) NUMBER_CELL_BLOCK_TEMP_CHECK = 0;
					else tmpNumber = TmpState.FirstTmp;
					break;
				
				default	:
					break;
				}
				
				 SerialPort.Sleep(1000);
			}
			
			if(i != NUMBER_CELL_BLOCK_TEMP_CHECK) {
			
				tmp = 0;
				
				for(i = 0; i < NUMBER_AMBIENT_TEMP_CHECK; i++) {
					
					tmp += SystemTmp.AmbTmpRead();
//					Log.w("TemperatureCheck", "Amb Temperature : " + tmp);
					
					SerialPort.Sleep(5000);
				}
				
				TimerDisplay.RXBoardFlag = false;
				
				if((Temperature.MinAmbTmp < tmp/NUMBER_AMBIENT_TEMP_CHECK) & (tmp/NUMBER_AMBIENT_TEMP_CHECK < Temperature.MaxAmbTmp)) {
					
					SerialPort.Sleep(300000);
					
					WhichIntent(TargetIntent.Home);
				
				} else {
					
					checkError = HomeActivity.AMBIENT_TEMP_ERROR;
					WhichIntent(TargetIntent.Home);
				}

			} else {
				
				checkError = HomeActivity.CELL_TEMP_ERROR;
				WhichIntent(TargetIntent.Home);
			}
		}
	}
	
//	public class PhotoCheck extends Thread {
//		
//		public void run() {
//			
//			MotionInstruct(RunActivity.MEASURE_POSITION, SerialPort.CtrTarget.PhotoSet);			
//			while(!RunActivity.OPERATE_COMPLETE.equals(SystemSerial.BoardMessageOutput()));
//				
//			for(int i = 0; i < 5; i++) {
//			
////				MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
////				while(!RunActivity.OPERATE_COMPLETE.equals(SystemSerial.BoardMessageOutput()));
//				
////				MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
////				while(!RunActivity.OPERATE_COMPLETE.equals(SystemSerial.BoardMessageOutput()));
//			
//				RunActivity.BlankValue[1] = AbsorbanceMeasure(); // 535nm Absorbance
//				
////				Log.w("PhotoCheck", "535nm blank : " + RunActivity.BlankValue[1]);
//				
//				MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//				while(!RunActivity.OPERATE_COMPLETE.equals(SystemSerial.BoardMessageOutput()));
//				
////				AbsorbanceMeasure();
//				
////				RunActivity.BlankValue[2] = SystemRun.AbsorbanceChange(); // 660nm Absorbance
//	
////				Log.w("PhotoCheck", "660nm blank : " + RunActivity.BlankValue[2]);
//				
//				MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//				while(!RunActivity.OPERATE_COMPLETE.equals(SystemSerial.BoardMessageOutput()));
//				
////				AbsorbanceMeasure();
//				
////				RunActivity.BlankValue[3] = SystemRun.AbsorbanceChange(); // 750nm Absorbance
//				
////				Log.w("PhotoCheck", "750nm blank : " + RunActivity.BlankValue[3]);
//			}			
//			
//			WhichIntent(HomeActivity.TargetIntent.Home);
//		}
//	}
	
	public synchronized double AbsorbanceMeasure(double min, double max) { // Absorbance measurement
		
		int time = 0;
		String rawValue;
		double douValue = 0;
		
		SystemSerial.BoardTx("VH", SerialPort.CtrTarget.PhotoSet);
		
		rawValue = SystemSerial.BoardMessageOutput();			
		
		while(rawValue.length() != 8) {
		
			rawValue = SystemSerial.BoardMessageOutput();			
				
			if(time++ > 50) systemState = AnalyzerState.NoResponse;
			
			SerialPort.Sleep(100);
		}
		
		if(systemState != AnalyzerState.NoResponse) {

			douValue = Double.parseDouble(rawValue);
			
			if((min < douValue) & (douValue < max)) {
				
				return (douValue - RunActivity.BlankValue[0]);
			}
		}
		
		return 0.0;
	}
	
	public void MotorCheck(String colRsp, AnalyzerState nextState, String errRsp, AnalyzerState errState, int rspTime) {
		
		String temp = "";
		
		SerialPort.Sleep(rspTime * 1000);
		
		temp = SystemSerial.BoardMessageOutput();
		
		if(colRsp.equals(temp)) systemState = nextState;
		else if(errRsp.equals(temp)) systemState = errState;
		else systemState = AnalyzerState.NoResponse;
	}
	
	public void PhotoCheck(AnalyzerState nextState, double max, double min, byte errBits, int rspTime) {
		
		String tempStr = "";
		double tempDouble = 0.0;
		
		RunActivity.BlankValue[0] = 0;
		
		SystemSerial.BoardTx("VH", SerialPort.CtrTarget.PhotoSet);
		
		SerialPort.Sleep(rspTime * 1000);
		
		tempStr = SystemSerial.BoardMessageOutput();

		if(tempStr.length() == 8) {
			
			tempDouble = Double.parseDouble(tempStr);
			
			if(!(min < tempDouble) | !(tempDouble < max)) photoCheck += errBits;
			
			systemState = nextState;
			
		} else systemState = AnalyzerState.NoResponse;
	}
	
	public void PhotoErrorCheck() {
		
		switch(photoCheck) {
		
		case 1	:
			systemState = AnalyzerState.PhotoSensorError;
			checkError = HomeActivity.PHOTO_SENSOR_ERROR;
			break;
			
		case 2	:
			systemState = AnalyzerState.PhotoSensorError;
			checkError = HomeActivity.FILTER_535nm_ERROR;
			break;
			
		case 4	:
			systemState = AnalyzerState.PhotoSensorError;
			checkError = HomeActivity.FILTER_660nm_ERROR;
			break;
			
		case 8	:
			systemState = AnalyzerState.PhotoSensorError;
			checkError = HomeActivity.FILTER_750nm_ERROR;
			break;
			
		case 14	:
			systemState = AnalyzerState.LampError;
			checkError = HomeActivity.LAMP_ERROR;
			break;
			
		default	:
			break;
		}
	}
	
	public void MotionInstruct(String str, SerialPort.CtrTarget target) { // Motion of system instruction
		
		SystemSerial.BoardTx(str, target);
	}
	
	public void SystemAniStart() { // System Check animation start

		systemCheckLinear = (RelativeLayout)findViewById(R.id.systemchecklayout);
		systemCheckImage = (ImageView)findViewById(R.id.systemcheckani);
		systemCheckAni = (AnimationDrawable)systemCheckImage.getBackground();
		
      	systemCheckLinear.post(new Runnable() {
	        public void run() {

	        	systemCheckAni.start();
	        }
		});
	}
	
	public void VolumeInit() { 
		
		int volume;
		
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		if((volume % 3) != 0 ) {
			
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 3, AudioManager.FLAG_PLAY_SOUND);
		}
	}
	
	public void BrightnessInit() {
		
		int brightness;
		
		try {
		
			brightness = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
		
			if((brightness % 51) != 0) {
				
				WindowManager.LayoutParams params = getWindow().getAttributes();
				params.screenBrightness = (float)brightness/100;
				getWindow().setAttributes(params);
				
				android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);
			}
		} catch (Exception e) {
			
		}
	}
	
	public void ParameterInit() { // Load to saved various parameter
		
		photoCheck = 0;
		systemState = AnalyzerState.InitPosition;
		checkError = HomeActivity.NORMAL_OPERATION;
		
		SharedPreferences DcntPref = getSharedPreferences("Data Counter", MODE_PRIVATE);
		RemoveActivity.PatientDataCnt = DcntPref.getInt("PatientDataCnt", 1);
		RemoveActivity.ControlDataCnt = DcntPref.getInt("ControlDataCnt", 1);
		
		/* TEST Mode */
		if(HomeActivity.ANALYZER_SW == HomeActivity.DEVEL) {
			
			RemoveActivity.PatientDataCnt = 3;
			RemoveActivity.ControlDataCnt = 3;
		}
		
		SharedPreferences AdjustmentPref = getSharedPreferences("User Define", MODE_PRIVATE);
		RunActivity.AF_Slope = AdjustmentPref.getFloat("AF SlopeVal", 1.0f);
		RunActivity.AF_Offset = AdjustmentPref.getFloat("AF OffsetVal", 0f);
		RunActivity.CF_Slope = AdjustmentPref.getFloat("CF SlopeVal", 1.0f);
		RunActivity.CF_Offset = AdjustmentPref.getFloat("CF OffsetVal", 0f);
		
		SharedPreferences LoginPref = PreferenceManager.getDefaultSharedPreferences(this);
		HomeActivity.LoginFlag = LoginPref.getBoolean("Activation", true);
		HomeActivity.CheckFlag = LoginPref.getBoolean("Check Box", false);
		
		SharedPreferences temperaturePref = getSharedPreferences("Temperature", MODE_PRIVATE);
		Temperature.InitTmp = temperaturePref.getFloat("Cell Block", 27.0f);
	}
	
	public void ErrorPopup(final byte error) {
		
		new Thread(new Runnable() {
		    public void run() {    
		        runOnUiThread(new Runnable(){
		            public void run() {
		            			            	
		            	errorPopup.showAtLocation(systemCheckLinear, Gravity.CENTER, 0, 0);
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
		
		switch(Itn) {
		
		case Home		:				
			Intent HomeIntent = new Intent(getApplicationContext(), HomeActivity.class);
			HomeIntent.putExtra("System Check State", (int) checkError);
			startActivity(HomeIntent);
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
