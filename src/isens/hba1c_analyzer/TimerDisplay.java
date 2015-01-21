package isens.hba1c_analyzer;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.SerialPort.BoardRxData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class TimerDisplay {
	
	public Handler handler = new Handler();
	public static TimerTask OneHundredmsPeriod;
	
	public enum whichClock 	{HomeClock, TestClock, RunClock, ActionClock, ResultClock, MemoryClock, BlankClock, SettingClock, SystemSettingClock, RemoveClock, PatientClock, ControlClock, ExportClock,
							ImageClock, DataSettingClock, MaintenanceClock, DateClock, TimeClock, DisplayClock, HISClock, HISSettingClock, AdjustmentClock, SoundClock, CalibrationClock, LanguageClock,
							SystemCheckClock, CorrelationClock, TemperatureClock, OperatorSettingClock, ShakingClock, PhotoClock, BarPriLanClock, ExtScanClock}
	public static whichClock timerState;

	final static String rTime[] = new String[8];
	
	public static boolean RXBoardFlag = false;
	
	private Timer timer;
	
	private RunActivity TimerRun;	
	private HomeActivity TimerHome;
	private BlankActivity TimerBlank;
	private ActionActivity TimerAction;
	private ResultActivity TimerResult;
	private RemoveActivity TimerRemove;
	private MemoryActivity TimerMemory;
	private ExportActivity TimerExport;
	private SettingActivity	TimerSetting;
	private PatientTestActivity TimerPatient;
	private ControlTestActivity TimerControl;
	private SystemSettingActivity TimerSystemSetting;
	private OperatorSettingActivity TimerOperatorSetting;
	private DateActivity TimerDate;
	private DisplayActivity TimerDisplay;
	private HISActivity	TimerHIS;
	private HISSettingActivity TimerHISSetting;
	private AdjustmentFactorActivity TimerAdjustment;
	private SoundActivity TimerSound;
	private CalibrationActivity TimerCalibration;
	private LanguageActivity TimerLanguage;
	private GpioPort TimerGpio;
	private CorrelationFactorActivity TimerCorrelation;
	private TemperatureActivity	TimerTemperature;
	private SerialPort TimerSerial;
	public ShakingActivity TimerShaking;
	public PhotoActivity TimerPhoto;
	public BarPriLanActivity TimerBarPriLan;
	public ExtScanActivity TimerExtScan;
	
//	public void TimerInit() {
//		
//		TimerSerial = new SerialPort();
//		TimerGpio = new GpioPort();
//		
//		Log.w("TimerInit", "run");
//		
//		OneHundredmsPeriod = new TimerTask() {
//			
//			int cnt = 0;
//			
//			public void run() {
//				Runnable updater = new Runnable() {
//					public void run() {
//						
//						TimerSerial.BoardRxData2();
//						Log.w("Timer", "run");
//						if(cnt++ == 100) cnt = 0;
//						
//						if((cnt % 10) == 0) { // One second period
//						
//							TimerGpio.CartridgeSensorScan();
//							TimerGpio.DoorSensorScan();
//						
//							RealTime();
//							
//							if(Integer.parseInt(rTime[6]) == 0) { // Whenever 00 second
//											
//								ClockDecision();
//							}
//
//							ExternalDeviceCheck();
//							
//						} else if((cnt % 2) == 0) {
//							
//							TimerGpio.CartridgeSensorScan();
//							TimerGpio.DoorSensorScan();
//						}
//					}
//				};
//				
//				handler.post(updater);		
//			}
//		};
//		
//		timer = new Timer();
//		timer.schedule(OneHundredmsPeriod, 0, 100); // Timer period : 100msec
//	}
	
	public void TimerInit() {
		
		TimerSerial = new SerialPort();
		TimerGpio = new GpioPort();
		
		Log.w("TimerInit", "run");
		
		OneHundredmsPeriod = new TimerTask() {
			
			int cnt = 0;
			
			public void run() {
				Runnable updater = new Runnable() {
					public void run() {
						
						if(cnt++ == 1000) cnt = 0;
						
						if(RXBoardFlag) TimerSerial.BoardRxData2();
						
						if((cnt % 20) == 0) { // One second period
						
							TimerGpio.CartridgeSensorScan();
							TimerGpio.DoorSensorScan();
						
							RealTime();
							
							if(Integer.parseInt(rTime[6]) == 0) { // Whenever 00 second
											
								ClockDecision();
							}

							ExternalDeviceCheck();
							
						} else if((cnt % 4) == 0) {
							
							TimerGpio.CartridgeSensorScan();
							TimerGpio.DoorSensorScan();
						}
					}
				};
				
				handler.post(updater);		
			}
		};
		
		timer = new Timer();
		timer.schedule(OneHundredmsPeriod, 0, 50); // Timer period : 100msec
	}
	
	public void ExternalDeviceCheck() {
		
		try {
			
		    boolean isConnect = false;
		    
			Process shell = Runtime.getRuntime().exec("/system/bin/busybox lsusb");

			BufferedReader br = new BufferedReader(new InputStreamReader(shell.getInputStream()));
			String line = "";
			
			while((line = br.readLine()) != null) {
			
				if(line.substring(23).equals("0483:5740")) {
					
					isConnect = true;
					
					if(HomeActivity.ExternalDeviceBarcode != HomeActivity.FILE_OPEN) {
					
						Log.w("shell", "line : " + line);
						
						TimerSerial.HHBarcodeSerialInit();
						TimerSerial.HHBarcodeRxStart();
						
						ExternalDeviceDecision(); 
					}
				}
			}
			
			br.close(); 

			if(isConnect == false) {
			
				if(HomeActivity.ExternalDeviceBarcode == HomeActivity.FILE_OPEN) {
					
					SerialPort.Sleep(500);
					
					HomeActivity.ExternalDeviceBarcode = HomeActivity.FILE_CLOSE;
							
					ExternalDeviceDecision();	
				}
			}
			
		} catch (IOException e) {
	
			throw new RuntimeException(e);
		}
	}
	
	public void RealTime() { // Get current date and time
	
		Calendar c = Calendar.getInstance();
		
		DecimalFormat dfm = new DecimalFormat("00");
		
		rTime[0] = Integer.toString(c.get(Calendar.YEAR));
		rTime[1] = dfm.format(c.get(Calendar.MONTH) + 1);
		rTime[2] = dfm.format(c.get(Calendar.DAY_OF_MONTH));
		
		if(c.get(Calendar.AM_PM) == 0) {

			rTime[3] = "AM";			
		} else {

			rTime[3] = "PM";			
		}
		
		if(c.get(Calendar.HOUR) != 0) {
			
			rTime[4] = dfm.format(c.get(Calendar.HOUR));
		} else {
		
			rTime[4] = "12";
		}
		rTime[5] = dfm.format(c.get(Calendar.MINUTE));		
		rTime[6] = dfm.format(c.get(Calendar.SECOND));
		rTime[7] = dfm.format(c.get(Calendar.HOUR_OF_DAY));		
	}
	
	public void ClockDecision() { // Whenever activity change, Corresponding clock action
		
		switch(timerState){
		
		case HomeClock			:	
			TimerHome = new HomeActivity();
			TimerHome.CurrTimeDisplay();
			break;
									
		case ActionClock		:	
			TimerAction = new ActionActivity();
			TimerAction.CurrTimeDisplay();
			break;
			
		case RunClock			:	
			TimerRun = new RunActivity();
			TimerRun.CurrTimeDisplay();
			break;
			
		case ResultClock		:	
			TimerResult = new ResultActivity();
			TimerResult.CurrTimeDisplay();
			break;
			
		case MemoryClock		:	
			TimerMemory = new MemoryActivity();
			TimerMemory.CurrTimeDisplay();
			break;
								
		case BlankClock			:	
			TimerBlank = new BlankActivity();
			TimerBlank.CurrTimeDisplay();
			break;
								
		case SettingClock		:	
			TimerSetting = new SettingActivity();
			TimerSetting.CurrTimeDisplay();
			break;
								
		case SystemSettingClock	:	
			TimerSystemSetting = new SystemSettingActivity();
			TimerSystemSetting.CurrTimeDisplay();
			break;
					
		case OperatorSettingClock	:	
			TimerOperatorSetting = new OperatorSettingActivity();
			TimerOperatorSetting.CurrTimeDisplay();
			break;
			
		case RemoveClock		:
			TimerRemove = new RemoveActivity();
			TimerRemove.CurrTimeDisplay();
			break;
			
		case PatientClock		:
			TimerPatient = new PatientTestActivity();
			TimerPatient.CurrTimeDisplay();
			break;
			
		case ControlClock		:
			TimerControl = new ControlTestActivity();
			TimerControl.CurrTimeDisplay();
			break;
			
		case ExportClock		:
			TimerExport = new ExportActivity();
			TimerExport.CurrTimeDisplay();
			break;
			
		case DateClock			:
			TimerDate = new DateActivity();
			TimerDate.CurrTimeDisplay();
			break;

		case DisplayClock		:
			TimerDisplay = new DisplayActivity();
			TimerDisplay.CurrTimeDisplay();
			break;
			
		case HISSettingClock	:
			TimerHISSetting = new HISSettingActivity();
			TimerHISSetting.CurrTimeDisplay();
			break;
			
		case HISClock			:
			TimerHIS = new HISActivity();
			TimerHIS.CurrTimeDisplay();
			break;
		
		case AdjustmentClock	:
			TimerAdjustment = new AdjustmentFactorActivity();
			TimerAdjustment.CurrTimeDisplay();
			break;

		case SoundClock			:
			TimerSound = new SoundActivity();
			TimerSound.CurrTimeDisplay();
			break;
			
		case CalibrationClock	:
			TimerCalibration = new CalibrationActivity();
			TimerCalibration.CurrTimeDisplay();
			break;
		
		case LanguageClock		:
			TimerLanguage = new LanguageActivity();
			TimerLanguage.CurrTimeDisplay();
			break;
		
		case CorrelationClock	:
			TimerCorrelation = new CorrelationFactorActivity();
			TimerCorrelation.CurrTimeDisplay();
			break;
			
		case TemperatureClock	:
			TimerTemperature = new TemperatureActivity();
			TimerTemperature.CurrTimeDisplay();
			break;

		case ShakingClock	:
			TimerShaking = new ShakingActivity();
			TimerShaking.CurrTimeDisplay();
			break;
		
		case PhotoClock	:
			TimerPhoto = new PhotoActivity();
			TimerPhoto.CurrTimeDisplay();
			break;
		
		case BarPriLanClock	:
			TimerBarPriLan = new BarPriLanActivity();
			TimerBarPriLan.CurrTimeDisplay();
			break;
		
		case ExtScanClock	:
			TimerExtScan = new ExtScanActivity();
			TimerExtScan.CurrTimeDisplay();
			break;
		
		default					:	
			break;		
		}
	}
	
	public void ExternalDeviceDecision() { // Whenever activity change, Corresponding clock action
		
		switch(timerState){
		
		case HomeClock			:	
			TimerHome = new HomeActivity();
			TimerHome.ExternalDeviceDisplay();
			break;
									
		case ActionClock		:	
			TimerAction = new ActionActivity();
			TimerAction.ExternalDeviceDisplay();
			break;
			
		case RunClock			:	
			TimerRun = new RunActivity();
			TimerRun.ExternalDeviceDisplay();
			break;
			
		case ResultClock		:	
			TimerResult = new ResultActivity();
			TimerResult.ExternalDeviceDisplay();
			break;
			
		case MemoryClock		:	
			TimerMemory = new MemoryActivity();
			TimerMemory.ExternalDeviceDisplay();
			break;
								
		case BlankClock			:	
			TimerBlank = new BlankActivity();
			TimerBlank.ExternalDeviceDisplay();
			break;
								
		case SettingClock		:	
			TimerSetting = new SettingActivity();
			TimerSetting.ExternalDeviceDisplay();
			break;
								
		case SystemSettingClock	:	
			TimerSystemSetting = new SystemSettingActivity();
			TimerSystemSetting.ExternalDeviceDisplay();
			break;
							
		case OperatorSettingClock	:	
			TimerOperatorSetting = new OperatorSettingActivity();
			TimerOperatorSetting.ExternalDeviceDisplay();
			break;
		
		case RemoveClock		:
			TimerRemove = new RemoveActivity();
			TimerRemove.ExternalDeviceDisplay();
			break;
			
		case PatientClock		:
			TimerPatient = new PatientTestActivity();
			TimerPatient.ExternalDeviceDisplay();
			break;
			
		case ControlClock		:
			TimerControl = new ControlTestActivity();
			TimerControl.ExternalDeviceDisplay();
			break;
			
		case ExportClock		:
			TimerExport = new ExportActivity();
			TimerExport.ExternalDeviceDisplay();
			break;
			
		case DateClock			:
			TimerDate = new DateActivity();
			TimerDate.ExternalDeviceDisplay();
			break;

		case DisplayClock		:
			TimerDisplay = new DisplayActivity();
			TimerDisplay.ExternalDeviceDisplay();
			break;
			
		case HISSettingClock	:
			TimerHISSetting = new HISSettingActivity();
//			TimerHISSetting.ExternalDeviceDisplay();
			break;
			
		case HISClock			:
			TimerHIS = new HISActivity();
//			TimerHIS.ExternalDeviceDisplay();
			break;
		
		case AdjustmentClock	:
			TimerAdjustment = new AdjustmentFactorActivity();
			TimerAdjustment.ExternalDeviceDisplay();
			break;

		case SoundClock			:
			TimerSound = new SoundActivity();
			TimerSound.ExternalDeviceDisplay();
			break;
			
		case CalibrationClock	:
			TimerCalibration = new CalibrationActivity();
			TimerCalibration.ExternalDeviceDisplay();
			break;
		
		case LanguageClock		:
			TimerLanguage = new LanguageActivity();
			TimerLanguage.ExternalDeviceDisplay();
			break;
		
		case CorrelationClock	:
			TimerCorrelation = new CorrelationFactorActivity();
			TimerCorrelation.ExternalDeviceDisplay();
			break;
			
		case TemperatureClock	:
			TimerTemperature = new TemperatureActivity();
			TimerTemperature.ExternalDeviceDisplay();
			break;

		case ShakingClock	:
			TimerShaking = new ShakingActivity();
			TimerShaking.ExternalDeviceDisplay();
			break;

		case PhotoClock	:
			TimerPhoto = new PhotoActivity();
			TimerPhoto.ExternalDeviceDisplay();
			break;

		case BarPriLanClock	:
			TimerBarPriLan = new BarPriLanActivity();
			TimerBarPriLan.ExternalDeviceDisplay();
			break;

		case ExtScanClock	:
			TimerExtScan = new ExtScanActivity();
			TimerExtScan.ExternalDeviceDisplay();
			break;

		default					:	
			break;		
		}
	}
}
