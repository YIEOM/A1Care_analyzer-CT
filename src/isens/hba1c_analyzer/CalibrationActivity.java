package isens.hba1c_analyzer;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.RunActivity.AnalyzerState;
import isens.hba1c_analyzer.RunActivity.Cart1stFilter1;
import isens.hba1c_analyzer.RunActivity.Cart1stFilter2;
import isens.hba1c_analyzer.RunActivity.Cart1stFilter3;
import isens.hba1c_analyzer.RunActivity.Cart2ndFilter1;
import isens.hba1c_analyzer.RunActivity.Cart2ndFilter2;
import isens.hba1c_analyzer.RunActivity.Cart2ndFilter3;
import isens.hba1c_analyzer.RunActivity.Cart2ndShaking;
import isens.hba1c_analyzer.RunActivity.CartDump;
import isens.hba1c_analyzer.RunActivity.ShakingAniThread;
import isens.hba1c_analyzer.SerialPort.CtrTarget;
import isens.hba1c_analyzer.TimerDisplay.whichClock;

public class CalibrationActivity extends Activity{

	private SerialPort CalibSerial;
	private ActionActivity CalibAction;
	
	private Button escBtn,
				   blankBtn,
				   scanBtn,
				   quickBtn,
				   fullBtn;
	
	private TextView deviceState,
					 oneOne,
					 oneTwo,
					 oneThree,
					 twoOne,
					 twoTwo,
					 twoThree,
					 threeOne,
					 threeTwo,
					 threeThree,
					 fourOne,
					 fourTwo,
					 fourThree,
					 fiveOne,
					 fiveTwo,
					 fiveThree,
					 sixOne,
					 sixTwo,
					 sixThree,
					 hba1cStr,
					 tHbStr;
	
	private Handler handler = new Handler();
	private TimerTask OneHundredmsPeriod;
	private Timer timer;
	
	private static boolean TestFlag = false,
						   ThreadRun = false;
	
	private enum TargetMode {Blank, Quick, Full, Scan}
	private enum MeasTarget {Shk1stOne, Shk1stTwo, Shk1stThree, Shk2ndOne, Shk2ndTwo, Shk2ndThree}
	
	private TargetMode targetMode = null;
	private MeasTarget measTarget = null;
	
	private RunActivity.AnalyzerState calibState;
	
	private static TextView TimeText;
	private static ImageView deviceImage;
	
	private boolean absorbCheck = false,
					btnState = false;
	
	DecimalFormat AbsorbanceFormat = new DecimalFormat("0.0000"),
				  hbA1cFormat = new DecimalFormat("0.00"),
				  tmpFormat = new DecimalFormat("0.0");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.calibration);
		
		TimeText = (TextView) findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);
		
		deviceState = (TextView) findViewById(R.id.devicestate);
		
		oneOne = (TextView) findViewById(R.id.oneone);
		oneTwo = (TextView) findViewById(R.id.onetwo);
		oneThree = (TextView) findViewById(R.id.onethree);
		
		twoOne = (TextView) findViewById(R.id.twoone);
		twoTwo = (TextView) findViewById(R.id.twotwo);
		twoThree = (TextView) findViewById(R.id.twothree);

		fourOne = (TextView) findViewById(R.id.fourone);
		fourTwo = (TextView) findViewById(R.id.fourtwo);
		fourThree = (TextView) findViewById(R.id.fourthree);
		
		threeOne = (TextView) findViewById(R.id.threeone);
		threeTwo = (TextView) findViewById(R.id.threetwo);
		threeThree = (TextView) findViewById(R.id.threethree);
		
		fiveOne = (TextView) findViewById(R.id.fiveone);
		fiveTwo = (TextView) findViewById(R.id.fivetwo);
		fiveThree = (TextView) findViewById(R.id.fivethree);
		
		sixOne = (TextView) findViewById(R.id.sixone);
		sixTwo = (TextView) findViewById(R.id.sixtwo);
		sixThree = (TextView) findViewById(R.id.sixthree);
		
		hba1cStr = (TextView) findViewById(R.id.HbA1cText);
		tHbStr = (TextView) findViewById(R.id.tHbText);
		
		escBtn = (Button)findViewById(R.id.escicon);
		escBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
			
				if(!btnState) {
				
					btnState = true;

					escBtn.setEnabled(false);
					
					WhichIntent(TargetIntent.SystemSetting);
				}
			}
		});
		
		blankBtn = (Button)findViewById(R.id.blankbtn);
		blankBtn.setOnClickListener(new View.OnClickListener() { 
		
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
					
					blankBtn.setEnabled(false);
					
					BlankMode();
				}
			}
		});
		
		scanBtn = (Button)findViewById(R.id.scanbtn);
		scanBtn.setOnClickListener(new View.OnClickListener() { 
		
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;
					
					scanBtn.setEnabled(false);
					
					BarcodeStart();
				}
			}
		});

		
		quickBtn = (Button)findViewById(R.id.quickbtn);
		quickBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
					
					quickBtn.setEnabled(false);
					
					QuickMode();
				}
			}
		});
		
		fullBtn = (Button)findViewById(R.id.fullbtn);
		fullBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {

				if(!btnState) {
					
					btnState = true;
					
					fullBtn.setEnabled(false);
					
					FullMode();
				}
			}
		});
		
		CalibrationInit();
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
	
	public void CalibrationInit() {
		
		TimerDisplay.timerState = whichClock.CalibrationClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		
		AbsorbanceDisplay();
		CalValueDisplay();
		
		CalibSerial = new SerialPort();
	}

	public void CurrTimeDisplay() { // displaying current time
		
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
	
	public void TimerInit() {
		
		OneHundredmsPeriod = new TimerTask() {
			
			int cnt = 0;
			
			public void run() {
				Runnable updater = new Runnable() {
					public void run() {
						
						cnt++;
						
						if(targetMode != TargetMode.Scan) {
						
							if(cnt == 5)	DeviceStateDisplay1();
							else if (cnt == 10) {
								
								cnt = 0; 
								DeviceStateDisplay2();
							}
							
							ThreadCheck();
						
						} else {
							
							if(cnt == 50) {
								
								cnt = 0;
								
								CalibAction = new ActionActivity();
								CalibAction.BarcodeScan();
							}
						}
					}
				};
				
				handler.post(updater);		
			}
		};
		
		timer = new Timer();
		timer.schedule(OneHundredmsPeriod, 0, 100); // Timer period : 100msec
	}
	
	public void ThreadCheck() {
		
		if(absorbCheck)
		{
			absorbCheck = false;
			
			switch(measTarget) {
			
			case Shk1stOne		:
				AbsorbanceDisplay1();
				break;
			
			case Shk1stTwo		:
				AbsorbanceDisplay2();
				break;
			
			case Shk1stThree	:
				AbsorbanceDisplay3();
				break;
			
			case Shk2ndOne		:
				AbsorbanceDisplay4();
				break;
			
			case Shk2ndTwo		:
				AbsorbanceDisplay5();
				break;
			
			case Shk2ndThree	:
				AbsorbanceDisplay6();
				break;
				
			default :
				break;
			}
		}
				
		if(!ThreadRun) {
			
			Stop();	
		}
	}
	
	public void Stop() {
		
		timer.cancel();
		
		SerialPort.Sleep(300);
		
		switch(calibState) {
		
		case NormalOperation	:
			deviceState.setTextColor(Color.parseColor("#565656"));
			deviceState.setText("READY");
			break;
			
		case ShakingMotorError	:
			deviceState.setTextColor(Color.parseColor("#FF0000"));
			deviceState.setText("SHAKING ERROR");
			break;
			
		case FilterMotorError	:
			deviceState.setTextColor(Color.parseColor("#FF0000"));
			deviceState.setText("FILTER ERROR");
			break;
			
		default	:
			break;
		}
		
		btnState = false;
		
		blankBtn.setEnabled(true);
		scanBtn.setEnabled(true);
		quickBtn.setEnabled(true);
		fullBtn.setEnabled(true);
		escBtn.setEnabled(true);
	}
	
	public void BlankMode() {
		
		escBtn.setEnabled(false);
		scanBtn.setEnabled(false);
		quickBtn.setEnabled(false);
		fullBtn.setEnabled(false);
		
		targetMode = TargetMode.Blank;
		calibState = AnalyzerState.InitPosition;
		ThreadRun = true;
		
		TimerInit();
		
		BlankStep BlankStepObj = new BlankStep();
		BlankStepObj.start();
	}
	
	public class BlankStep extends Thread { // Blank run
		
		public void run() {
			
			for(int i = 0; i < 9; i++) {
				
				switch(calibState) {
				
				case InitPosition		:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.MeasurePosition, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break; 
				
				case MeasurePosition :
					MotionInstruct(RunActivity.MEASURE_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.MEASURE_POSITION, AnalyzerState.FilterDark, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case FilterDark :
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.Filter535nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.BlankValue[0] = 0;
					RunActivity.BlankValue[0] = AbsorbanceMeasure(); // Dark Absorbance
					break;
					
				case Filter535nm :
					/* 535nm filter Measurement */
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter660nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.BlankValue[1] = AbsorbanceMeasure(); // Dark Absorbance
					break;
				
				case Filter660nm :
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter750nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.BlankValue[2] = AbsorbanceMeasure(); // Dark Absorbance
					break;
				
				case Filter750nm :
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.FilterHome, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.BlankValue[3] = AbsorbanceMeasure(); // Dark Absorbance
					break;
				
				case FilterHome :
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.CartridgeHome, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					break;
				
				case CartridgeHome :
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NormalOperation, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case ShakingMotorError	:
					calibState = AnalyzerState.NoWorking;
					break;
					
				case FilterMotorError	:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				default	:
					break;
				}
			}
			
			ThreadRun = false;
		}
	}
	
	public void QuickMode() {
		
		blankBtn.setEnabled(false);
		scanBtn.setEnabled(false);
		fullBtn.setEnabled(false);
		escBtn.setEnabled(false);
		
		targetMode = TargetMode.Quick;
		calibState = AnalyzerState.InitPosition;
		ThreadRun = true;
		
		AbsorbanceDisplay();
		
		TimerInit();
		
		Cart1stShaking Cart1stShakingObj = new Cart1stShaking();
		Cart1stShakingObj.start();
	}
	
	public void FullMode() {
		
		blankBtn.setEnabled(false);
		scanBtn.setEnabled(false);
		quickBtn.setEnabled(false);
		escBtn.setEnabled(false);
		
		targetMode = TargetMode.Full;
		calibState = AnalyzerState.InitPosition;
		ThreadRun = true;
		
		AbsorbanceDisplay();
		
		TimerInit();
		
		Cart1stShaking BlankCart1stShakingObj = new Cart1stShaking();
		BlankCart1stShakingObj.start();
	}
	
//	public class Cart1stShaking extends Thread { // First shaking motion
//		
//		public void run() {
//						
//			String shkTime = "0000";
//			
//			if(targetMode == TargetMode.Quick) shkTime = "0030";
//			else if(targetMode == TargetMode.Full) shkTime = "0630";
//			
//			MotionInstruct(RunActivity.Step1st_POSITION, SerialPort.CtrTarget.PhotoSet);			
//			while(!RunActivity.Step1st_POSITION.equals(CalibSerial.BoardMessageOutput()));
//			
//			MotionInstruct(shkTime, SerialPort.CtrTarget.MotorSet);  // Motor shaking time, default : 6.5 * 10(sec) = 0065
//			while(!RunActivity.MOTOR_COMPLETE.equals(CalibSerial.BoardMessageOutput()));
//			SerialPort.Sleep(2000);
//				
//			MotionInstruct(RunActivity.MEASURE_POSITION, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.MEASURE_POSITION.equals(CalibSerial.BoardMessageOutput()));
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			RunActivity.Step1stValue1[0] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			
//			RunActivity.Step1stValue1[1] = AbsorbanceMeasure(); // 535nm Absorbance
//				
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//				
//			RunActivity.Step1stValue1[2] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
//			AbsorbCal1st();
//			measTarget = MeasTarget.Shk1stOne;
//			absorbCheck = true;
//			while(!RunActivity.FILTER_DARK.equals(CalibSerial.BoardMessageOutput()));
//			
//			SerialPort.Sleep(1000);
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			RunActivity.Step1stValue2[0] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			
//			RunActivity.Step1stValue2[1] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			
//			RunActivity.Step1stValue2[2] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
//			AbsorbCal1st2();
//			measTarget = MeasTarget.Shk1stTwo;
//			absorbCheck = true;
//			while(!RunActivity.FILTER_DARK.equals(CalibSerial.BoardMessageOutput()));
//				
//			SerialPort.Sleep(1000);
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			RunActivity.Step1stValue3[0] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			
//			RunActivity.Step1stValue3[1] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			
//			RunActivity.Step1stValue3[2] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
//			AbsorbCal1st3();
//			measTarget = MeasTarget.Shk1stThree;
//			absorbCheck = true;
//			while(!RunActivity.FILTER_DARK.equals(CalibSerial.BoardMessageOutput()));
//			
//			Cart2ndShaking Cart2ndShakingObj = new Cart2ndShaking();
//			Cart2ndShakingObj.start();	
//		}
//	}
	
	public class Cart1stShaking extends Thread { // First shaking motion

		public void run() {
			
			String shkTime = "0000";
			
			if(targetMode == TargetMode.Quick) shkTime = "0030";
			else if(targetMode == TargetMode.Full) shkTime = "0630";
			
			for(int i = 0; i < 3; i++) {
				
				switch(calibState) {
				
				case InitPosition		:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.Step1Position, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
				
				case Step1Position	:
					MotionInstruct(RunActivity.Step1st_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.Step1st_POSITION, AnalyzerState.Step1Shaking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					SerialPort.Sleep(500);
					break;
					
				case Step1Shaking	:
					MotionInstruct(shkTime, SerialPort.CtrTarget.MotorSet);  // Motor shaking time, default : 6.5 * 10(sec) = 0065
					BoardMessage(RunActivity.MOTOR_COMPLETE, AnalyzerState.MeasurePosition, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case ShakingMotorError	:
					calibState = AnalyzerState.NoWorking;
					break;
					
				case FilterMotorError	:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				default	:
					break;
				}
			}
			
			if(calibState == AnalyzerState.MeasurePosition) {
				
				Cart1stFilter1 Cart1stFilter1Obj = new Cart1stFilter1();
				Cart1stFilter1Obj.start();	
			}
		}
	}

	public class Cart1stFilter1 extends Thread { // First filter motion of first shaking 

		public void run() {

			SerialPort.Sleep(2000);
			
			for(int i = 0; i < 5; i++) {
				
				switch(calibState) {
				
				case MeasurePosition	:
					MotionInstruct(RunActivity.MEASURE_POSITION, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.MEASURE_POSITION, AnalyzerState.Filter535nm, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case Filter535nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter660nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step1stValue1[0] = AbsorbanceMeasure(); // 535nm Absorbance
					break;
					
				case Filter660nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter750nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step1stValue1[1] = AbsorbanceMeasure(); // 660nm Absorbance
					break;
					
				case Filter750nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.FilterDark, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step1stValue1[2] = AbsorbanceMeasure(); // 750nm Absorbance
					break;
					
				case FilterDark		:
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.Filter535nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					AbsorbCal1st();
					measTarget = MeasTarget.Shk1stOne;
					absorbCheck = true;
					break;
					
				case ShakingMotorError	:
					calibState = AnalyzerState.NoWorking;
					break;
					
				case FilterMotorError	:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case NoResponse :
					calibState = AnalyzerState.NoWorking;
					break;
					
				default	:
					break;
				}
			}
			
			if(calibState == AnalyzerState.Filter535nm) {
				
				Cart1stFilter2 Cart1stFilter2Obj = new Cart1stFilter2();
				Cart1stFilter2Obj.start();	
			}
		}
	}

	public class Cart1stFilter2 extends Thread { // Second filter motion of first shaking

		public void run() {
			
			SerialPort.Sleep(1000);
			
			for(int i = 0; i < 4; i++) {
				
				switch(calibState) {
				
				case Filter535nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter660nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step1stValue2[0] = AbsorbanceMeasure(); // 535nm Absorbance
					break;
					
				case Filter660nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter750nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step1stValue2[1] = AbsorbanceMeasure(); // 660nm Absorbance
					break;
					
				case Filter750nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.FilterDark, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step1stValue2[2] = AbsorbanceMeasure(); // 750nm Absorbance
					break;
					
				case FilterDark		:
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.Filter535nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					AbsorbCal1st2();
					measTarget = MeasTarget.Shk1stTwo;
					absorbCheck = true;
					break;
					
				case ShakingMotorError	:
					calibState = AnalyzerState.NoWorking;
					break;
					
				case FilterMotorError	:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case NoResponse :
					calibState = AnalyzerState.NoWorking;
					break;
					
				default	:
					break;
				}
			}
			
			if(calibState == AnalyzerState.Filter535nm) {
				
				Cart1stFilter3 Cart1stFilter3Obj = new Cart1stFilter3();
				Cart1stFilter3Obj.start();
			}
		}
	}
	
	public class Cart1stFilter3 extends Thread { // Third filter motion of first shaking

		public void run() {
			
			SerialPort.Sleep(1000);
			
			for(int i = 0; i < 4; i++) {
				
				switch(calibState) {
				
				case Filter535nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter660nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step1stValue3[0] = AbsorbanceMeasure(); // 535nm Absorbance
					break;
					
				case Filter660nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter750nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step1stValue3[1] = AbsorbanceMeasure(); // 750nm Absorbance
					break;
					
				case Filter750nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.FilterDark, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step1stValue3[2] = AbsorbanceMeasure(); // 750nm Absorbance
					break;
					
				case FilterDark		:
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.Step2Position, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					AbsorbCal1st3();
					measTarget = MeasTarget.Shk1stThree;
					absorbCheck = true;
					break;
					
				case ShakingMotorError	:
					calibState = AnalyzerState.NoWorking;
					break;
					
				case FilterMotorError	:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case NoResponse :
					calibState = AnalyzerState.NoWorking;
					break;
					
				default	:
					break;
				}
			}
			
			if(calibState == AnalyzerState.Step2Position) {
				
				Cart2ndShaking Cart2ndShakingObj = new Cart2ndShaking();
				Cart2ndShakingObj.start();	
			}
		}
	}
	
//	public class Cart2ndShaking extends Thread { // Second shaking motion
//		
//		public void run() {			
//		
//			String shkTime = "0000";
//			
//			if(targetMode == TargetMode.Quick) shkTime = "0030";
//			else if(targetMode == TargetMode.Full) shkTime = "0540";
//			
//			MotionInstruct(RunActivity.Step2nd_POSITION, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.Step2nd_POSITION.equals(CalibSerial.BoardMessageOutput()));
//						
//			MotionInstruct(shkTime, SerialPort.CtrTarget.MotorSet);  // Motor shaking time, default : 6.5 * 10(sec) = 0065
//			while(!RunActivity.MOTOR_COMPLETE.equals(CalibSerial.BoardMessageOutput()));
//			SerialPort.Sleep(2000);
//						
//			MotionInstruct(RunActivity.MEASURE_POSITION, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.MEASURE_POSITION.equals(CalibSerial.BoardMessageOutput()));
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			RunActivity.Step2ndValue1[0] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			RunActivity.Step2ndValue1[1] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			RunActivity.Step2ndValue1[2] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
//			AbsorbCal2nd();
//			measTarget = MeasTarget.Shk2ndOne;
//			absorbCheck = true;
//			while(!RunActivity.FILTER_DARK.equals(CalibSerial.BoardMessageOutput()));
//			
//			SerialPort.Sleep(1000);
//						
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			RunActivity.Step2ndValue2[0] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			RunActivity.Step2ndValue2[1] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			RunActivity.Step2ndValue2[2] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
//			AbsorbCal2nd2();
//			measTarget = MeasTarget.Shk2ndTwo;
//			absorbCheck = true;
//			while(!RunActivity.FILTER_DARK.equals(CalibSerial.BoardMessageOutput()));
//		
//			SerialPort.Sleep(1000);
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			RunActivity.Step2ndValue3[0] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			RunActivity.Step2ndValue3[1] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.NEXT_FILTER.equals(CalibSerial.BoardMessageOutput()));
//			RunActivity.Step2ndValue3[2] = AbsorbanceMeasure(); // 535nm Absorbance
//			
//			MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
//			AbsorbCal2nd3();
//			measTarget = MeasTarget.Shk2ndThree;
//			absorbCheck = true;
//			while(!RunActivity.FILTER_DARK.equals(CalibSerial.BoardMessageOutput()));
//			
//			CartDump CartDumpObj = new CartDump();
//			CartDumpObj.start();	
//		}
//	}
	
	public class Cart2ndShaking extends Thread { // Second shaking motion
		
		public void run() {			
			
			String shkTime = "0000";
			
			if(targetMode == TargetMode.Quick) shkTime = "0030";
			else if(targetMode == TargetMode.Full) shkTime = "0540";
			
			for(int i = 0; i < 2; i++) {
				
				switch(calibState) {
				
				case Step2Position	:
					MotionInstruct(RunActivity.Step2nd_POSITION, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.Step2nd_POSITION, AnalyzerState.Step2Shaking, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					SerialPort.Sleep(500);
					break;
					
				case Step2Shaking	:
					MotionInstruct(shkTime, SerialPort.CtrTarget.MotorSet);  // Motor shaking time, default : 6 * 10(sec) = 0065
					BoardMessage(RunActivity.MOTOR_COMPLETE, AnalyzerState.MeasurePosition, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case ShakingMotorError	:
					calibState = AnalyzerState.NoWorking;
					break;
					
				case FilterMotorError	:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case NoResponse :
					calibState = AnalyzerState.NoWorking;
					break;
					
				default	:
					break;
				}
			}
			
			if(calibState == AnalyzerState.MeasurePosition) {
				
				Cart2ndFilter1 Cart2ndFilter1Obj = new Cart2ndFilter1();
				Cart2ndFilter1Obj.start();
			}
		}
	}
	
	public class Cart2ndFilter1 extends Thread { // First filter motion of second shaking
		
		public void run() {			

			SerialPort.Sleep(2000);
			
			for(int i = 0; i < 5; i++) {
				
				switch(calibState) {
				
				case MeasurePosition	:
					MotionInstruct(RunActivity.MEASURE_POSITION, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.MEASURE_POSITION, AnalyzerState.Filter535nm, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case Filter535nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter660nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step2ndValue1[0] = AbsorbanceMeasure(); // 535nm Absorbance
					break;
					
				case Filter660nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter750nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step2ndValue1[1] = AbsorbanceMeasure(); // 660nm Absorbance
					break;
				
				case Filter750nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.FilterDark, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step2ndValue1[2] = AbsorbanceMeasure(); // 750nm Absorbance
					break;
				
				case FilterDark		:
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.Filter535nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					AbsorbCal2nd();
					measTarget = MeasTarget.Shk2ndOne;
					absorbCheck = true;
					break;
					
				case ShakingMotorError	:
					calibState = AnalyzerState.NoWorking;
					break;
					
				case FilterMotorError	:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case NoResponse :
					calibState = AnalyzerState.NoWorking;
					break;
					
				default	:
					break;
				}
			}
			
			if(calibState == AnalyzerState.Filter535nm) {
			
				Cart2ndFilter2 Cart2ndFilter2Obj = new Cart2ndFilter2();
				Cart2ndFilter2Obj.start();
			}
		}
	}
	
	public class Cart2ndFilter2 extends Thread { // Second filter motion of second shaking
		
		public void run() {
			
			SerialPort.Sleep(1000);
						
			for(int i = 0; i < 4; i++) {
				
				switch(calibState) {
				
				case Filter535nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter660nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step2ndValue2[0] = AbsorbanceMeasure(); // 535nm Absorbance
					break;
					
				case Filter660nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter750nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step2ndValue2[1] = AbsorbanceMeasure(); // 535nm Absorbance
					break;
					
				case Filter750nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.FilterDark, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step2ndValue2[2] = AbsorbanceMeasure(); // 535nm Absorbance
					break;
					
				case FilterDark		:
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.Filter535nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					AbsorbCal2nd2();
					measTarget = MeasTarget.Shk2ndTwo;
					absorbCheck = true;
					break;
					
				case ShakingMotorError	:
					calibState = AnalyzerState.NoWorking;
					break;
					
				case FilterMotorError	:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case NoResponse :
					calibState = AnalyzerState.NoWorking;
					break;
					
				default	:
					break;
				}
			}
		
			if(calibState == AnalyzerState.Filter535nm) {
					
				Cart2ndFilter3 Cart2ndFilter3Obj = new Cart2ndFilter3();
				Cart2ndFilter3Obj.start();
			}
		}
	}
	
	public class Cart2ndFilter3 extends Thread { // Third filter motion of second shaking
		
		public void run() {
			
			SerialPort.Sleep(1000);
			
			for(int i = 0; i < 4; i++) {
				
				switch(calibState) {
				
				case Filter535nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter660nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step2ndValue3[0] = AbsorbanceMeasure(); // 535nm Absorbance
					break;
					
				case Filter660nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.Filter750nm, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step2ndValue3[1] = AbsorbanceMeasure(); // 535nm Absorbance
					break;
					
				case Filter750nm	:
					MotionInstruct(RunActivity.NEXT_FILTER, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.NEXT_FILTER, AnalyzerState.FilterDark, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					RunActivity.Step2ndValue3[2] = AbsorbanceMeasure(); // 535nm Absorbance
					break;
					
				case FilterDark		:
					MotionInstruct(RunActivity.FILTER_DARK, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.FILTER_DARK, AnalyzerState.CartridgeDump, RunActivity.FILTER_ERROR, AnalyzerState.FilterMotorError);
					AbsorbCal2nd3();
					measTarget = MeasTarget.Shk2ndThree;
					absorbCheck = true;
					break;
					
				case ShakingMotorError	:
					calibState = AnalyzerState.NoWorking;
					break;
					
				case FilterMotorError	:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case NoResponse :
					calibState = AnalyzerState.NoWorking;
					break;
					
				default	:
					break;
				}
			}			
			
			if(calibState == AnalyzerState.CartridgeDump) {
				
				CartDump CartDumpObj = new CartDump();
				CartDumpObj.start();
			}
		}
	}
		
//	public class CartDump extends Thread { // Cartridge dumping motion
//		
//		public void run() {
//						
//			MotionInstruct(RunActivity.CARTRIDGE_DUMP, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.CARTRIDGE_DUMP.equals(CalibSerial.BoardMessageOutput()));
//
//			MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);
//			while(!RunActivity.HOME_POSITION.equals(CalibSerial.BoardMessageOutput()));
//			
//			ThreadRun = false;
//		}
//	}
	
	public class CartDump extends Thread { // Cartridge dumping motion
		
		public void run() {
		
			for(int i = 0; i < 3; i++) {
				
				switch(calibState) {
				
				case CartridgeDump	:
					MotionInstruct(RunActivity.CARTRIDGE_DUMP, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.CARTRIDGE_DUMP, AnalyzerState.CartridgeHome, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case CartridgeHome	:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NormalOperation, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case ShakingMotorError	:
					calibState = AnalyzerState.NoWorking;
					break;
					
				case FilterMotorError	:
					MotionInstruct(RunActivity.HOME_POSITION, SerialPort.CtrTarget.PhotoSet);			
					BoardMessage(RunActivity.HOME_POSITION, AnalyzerState.NoWorking, RunActivity.CARTRIDGE_ERROR, AnalyzerState.ShakingMotorError);
					break;
					
				case NoResponse :
					calibState = AnalyzerState.NoWorking;
					break;
					
				default	:
					break;
				}
			}
			
			if(calibState == AnalyzerState.NormalOperation) {
				
				ThreadRun = false;
			}
		}
	}
	
	public void MotionInstruct(String str, SerialPort.CtrTarget target) { // Motion of system instruction
		
		CalibSerial.BoardTx(str, target);
	}
	
	public synchronized void AbsorbCal1st() {
		
		RunActivity.Step1stAbsorb1[0]  = -Math.log10(RunActivity.Step1stValue1[0]/RunActivity.BlankValue[1]);
		RunActivity.Step1stAbsorb1[1]  = -Math.log10(RunActivity.Step1stValue1[1]/RunActivity.BlankValue[2]);
		RunActivity.Step1stAbsorb1[2]  = -Math.log10(RunActivity.Step1stValue1[2]/RunActivity.BlankValue[3]);
	}

	public synchronized void AbsorbCal1st2() {
		
		RunActivity.Step1stAbsorb2[0] = -Math.log10(RunActivity.Step1stValue2[0]/RunActivity.BlankValue[1]);
		RunActivity.Step1stAbsorb2[1] = -Math.log10(RunActivity.Step1stValue2[1]/RunActivity.BlankValue[2]);
		RunActivity.Step1stAbsorb2[2] = -Math.log10(RunActivity.Step1stValue2[2]/RunActivity.BlankValue[3]);
	}
	
	public synchronized void AbsorbCal1st3() {
		
		RunActivity.Step1stAbsorb3[0] = -Math.log10(RunActivity.Step1stValue3[0]/RunActivity.BlankValue[1]);
		RunActivity.Step1stAbsorb3[1] = -Math.log10(RunActivity.Step1stValue3[1]/RunActivity.BlankValue[2]);
		RunActivity.Step1stAbsorb3[2] = -Math.log10(RunActivity.Step1stValue3[2]/RunActivity.BlankValue[3]);
	}
	
	public synchronized void AbsorbCal2nd() {
		
		RunActivity.Step2ndAbsorb1[0]  = -Math.log10(RunActivity.Step2ndValue1[0]/RunActivity.BlankValue[1]); // 535nm
		RunActivity.Step2ndAbsorb1[1]  = -Math.log10(RunActivity.Step2ndValue1[1]/RunActivity.BlankValue[2]); // 660nm
		RunActivity.Step2ndAbsorb1[2]  = -Math.log10(RunActivity.Step2ndValue1[2]/RunActivity.BlankValue[3]); // 750nm
	}
	
	public synchronized void AbsorbCal2nd2() {
		
		RunActivity.Step2ndAbsorb2[0] = -Math.log10(RunActivity.Step2ndValue2[0]/RunActivity.BlankValue[1]);
		RunActivity.Step2ndAbsorb2[1] = -Math.log10(RunActivity.Step2ndValue2[1]/RunActivity.BlankValue[2]);
		RunActivity.Step2ndAbsorb2[2] = -Math.log10(RunActivity.Step2ndValue2[2]/RunActivity.BlankValue[3]);
	}
	
	public synchronized void AbsorbCal2nd3() {
				
		RunActivity.Step2ndAbsorb3[0] = -Math.log10(RunActivity.Step2ndValue3[0]/RunActivity.BlankValue[1]);
		RunActivity.Step2ndAbsorb3[1] = -Math.log10(RunActivity.Step2ndValue3[1]/RunActivity.BlankValue[2]);
		RunActivity.Step2ndAbsorb3[2] = -Math.log10(RunActivity.Step2ndValue3[2]/RunActivity.BlankValue[3]);
	}
	
	public void DeviceStateDisplay1() {

		switch(targetMode) {
		
		case Blank			:
			deviceState.setTextColor(Color.parseColor("#565656"));
			deviceState.setText("BLANK");
			break;
			
		case Quick			:
			deviceState.setTextColor(Color.parseColor("#04A458"));
			deviceState.setText("QUICK");
			break;
		
		case Full	:
			deviceState.setTextColor(Color.parseColor("#023894"));
			deviceState.setText("FULL");
			break;
			
		default	:
			break;
		}
	}
	
	public void DeviceStateDisplay2() {
		
		switch(targetMode) {
		
		case Blank			:
			deviceState.setTextColor(Color.parseColor("#FFFFFF"));
			deviceState.setText("BLANK");
			break;
			
		case Quick			:
			deviceState.setTextColor(Color.parseColor("#FFFFFF"));
			deviceState.setText("QUICK");
			break;
			
		case Full	:
			deviceState.setTextColor(Color.parseColor("#FFFFFF"));
			deviceState.setText("FULL");
			break;
			
		default	:
			break;
		}
   	}
	
	public void AbsorbanceDisplay() {
		
    	oneOne.setText("");
    	oneTwo.setText("");
    	oneThree.setText("");
			
    	twoOne.setText("");
    	twoTwo.setText("");
    	twoThree.setText(""); 
		
    	threeOne.setText("");
    	threeTwo.setText("");
    	threeThree.setText("");
	
    	fourOne.setText("");
    	fourTwo.setText("");
    	fourThree.setText("");
		
		fiveOne.setText("");
    	fiveTwo.setText("");
    	fiveThree.setText("");
		
    	sixOne.setText("");
    	sixTwo.setText("");
    	sixThree.setText("");
	}
	
	public void CalValueDisplay() {
    	
    	hba1cStr.setText("");
    	tHbStr.setText("");
	}
	
	public void AbsorbanceDisplay1() {
		
    	oneOne.setText(AbsorbanceFormat.format(RunActivity.Step1stAbsorb1[0]));
    	oneTwo.setText(AbsorbanceFormat.format(RunActivity.Step1stAbsorb1[1]));
    	oneThree.setText(AbsorbanceFormat.format(RunActivity.Step1stAbsorb1[2]));
	}
	
	public void AbsorbanceDisplay2() {
			
    	twoOne.setText(AbsorbanceFormat.format(RunActivity.Step1stAbsorb2[0]));
    	twoTwo.setText(AbsorbanceFormat.format(RunActivity.Step1stAbsorb2[1]));
    	twoThree.setText(AbsorbanceFormat.format(RunActivity.Step1stAbsorb2[2])); 
	}
	
	public void AbsorbanceDisplay3() {
		
    	threeOne.setText(AbsorbanceFormat.format(RunActivity.Step1stAbsorb3[0]));
    	threeTwo.setText(AbsorbanceFormat.format(RunActivity.Step1stAbsorb3[1]));
    	threeThree.setText(AbsorbanceFormat.format(RunActivity.Step1stAbsorb3[2]));
	}
	
	public void AbsorbanceDisplay4() {

    	fourOne.setText(AbsorbanceFormat.format(RunActivity.Step2ndAbsorb1[0]));
    	fourTwo.setText(AbsorbanceFormat.format(RunActivity.Step2ndAbsorb1[1]));
    	fourThree.setText(AbsorbanceFormat.format(RunActivity.Step2ndAbsorb1[2]));
	}
	
	public void AbsorbanceDisplay5() {
		
		fiveOne.setText(AbsorbanceFormat.format(RunActivity.Step2ndAbsorb2[0]));
    	fiveTwo.setText(AbsorbanceFormat.format(RunActivity.Step2ndAbsorb2[1]));
    	fiveThree.setText(AbsorbanceFormat.format(RunActivity.Step2ndAbsorb2[2]));
	}
	
	public void AbsorbanceDisplay6() {
		
    	sixOne.setText(AbsorbanceFormat.format(RunActivity.Step2ndAbsorb3[0]));
    	sixTwo.setText(AbsorbanceFormat.format(RunActivity.Step2ndAbsorb3[1]));
    	sixThree.setText(AbsorbanceFormat.format(RunActivity.Step2ndAbsorb3[2])); 
	}
	
	public void HbA1cDisplay() {
		
    	hba1cStr.setText(hbA1cFormat.format(RunActivity.HbA1cPctDbl));
		tHbStr.setText(hbA1cFormat.format(RunActivity.tHbDbl));
	}
	
	public void BarcodeStart() {

		blankBtn.setEnabled(false);
		quickBtn.setEnabled(false);
		fullBtn.setEnabled(false);
		escBtn.setEnabled(false);
		
		CalValueDisplay();
		
		CalibSerial = new SerialPort();
		CalibSerial.BarcodeSerialInit();
		CalibSerial.BarcodeRxStart();
		
		ActionActivity.BarcodeCheckFlag = false;
		
		targetMode = TargetMode.Scan;
		
		CalibAction = new ActionActivity();
		CalibAction.BarcodeScan();
		
		TimerInit();
		
		BarcodeScan BarcodeScan = new BarcodeScan();
		BarcodeScan.start();
	}
	
	public class BarcodeScan extends Thread {
		
		public void run () {
			
			while(!ActionActivity.BarcodeCheckFlag);
			
			timer.cancel();
			
			if(ActionActivity.IsCorrectBarcode) HbA1cCalculation();
			
			new Thread(new Runnable() {
			    public void run() {
			        runOnUiThread(new Runnable(){
			            public void run() {
			
			            	HbA1cDisplay();
			            	
			            	btnState = false;
			            	
			            	blankBtn.setEnabled(true);
			    			quickBtn.setEnabled(true);
			    			fullBtn.setEnabled(true);
			    			scanBtn.setEnabled(true);
			    			escBtn.setEnabled(true);
			            }
			        });
			    }
			}).start();
		}
	}
	
	public synchronized double AbsorbanceMeasure() { // Absorbance measurement
		
		int time = 0;
		String rawValue;
		double douValue = 0;
		
//		SerialPort.Sleep(1000);
		
		CalibSerial.BoardTx("VH", SerialPort.CtrTarget.PhotoSet);
		
		rawValue = CalibSerial.BoardMessageOutput();			
		
		while(rawValue.length() != 8) {
		
			rawValue = CalibSerial.BoardMessageOutput();			
		
			SerialPort.Sleep(100);
		}	
					
		douValue = Double.parseDouble(rawValue);
		
		return (douValue - RunActivity.BlankValue[0]);
	}
	
	public void BoardMessage(String colRsp, AnalyzerState nextState, String errRsp, AnalyzerState errState) {
		
		int time = 0;
		String temp = "";
		
		while(true) {
			
			temp = CalibSerial.BoardMessageOutput();
			
			if(colRsp.equals(temp)) {
				
				calibState = nextState;
				break;			
				
			} else if(errRsp.equals(temp)) {
				
				calibState = errState;
				break;
			}
			
			SerialPort.Sleep(100);
		}
	}
	
	public synchronized void HbA1cCalculation() {
		
		double A, 
			   B, 
			   St, 
			   Bt, 
			   SLA, 
			   SHA, 
			   BLA, 
			   BHA, 
			   SLV, 
			   SHV, 
			   BLV, 
			   BHV, 
			   a3, 
			   b3, 
			   b32, 
			   a4, 
			   b4;
		
		A = Absorb1stHandling();
		B = Absorb2ndHandling();
		
		St = (A - Barcode.b1)/Barcode.a1;
		RunActivity.tHbDbl = St;
		Bt = (A - Barcode.b1)/Barcode.a1 + 1;
		
		SLA = St * Barcode.L / 100;
		SHA = St * Barcode.H / 100;
		BLA = Bt * Barcode.L / 100;
		BHA = Bt * Barcode.H / 100;
		
		SLV = SLA * Barcode.a21 + Barcode.b21;
		SHV = SHA * Barcode.a22 + Barcode.b22;
		BLV = BLA * Barcode.a21 + Barcode.b21;
		BHV = BHA * Barcode.a22 + Barcode.b22;
		
		a3 = (SHV - SLV) / (SHA - SLA);
		b3 = SLV - (a3 * SLA);
		
		b32 = BLV - (((BHV - BLV) / (BHA - BLA)) * BLA);
		
		a4 = (b32 - b3) / (Bt - St);
		b4 = b3 - (a4 * St);
		
		RunActivity.HbA1cPctDbl = (B - (St * a4 + b4)) / a3 / St * 100;
		RunActivity.HbA1cPctDbl = RunActivity.CF_Slope * (RunActivity.AF_Slope * RunActivity.HbA1cPctDbl + RunActivity.AF_Offset) + RunActivity.CF_Offset;
	}
	
	public double Absorb1stHandling() {
		
		double abs[] = new double[3],
			   dev[] = new double[3],
			   std, 
			   sum, 
			   avg, 
			   res;
		int idx = 0;
		
		abs[0] = RunActivity.Step1stAbsorb1[0] - RunActivity.Step1stAbsorb1[2];
		abs[1] = RunActivity.Step1stAbsorb2[0] - RunActivity.Step1stAbsorb2[2];
		abs[2] = RunActivity.Step1stAbsorb3[0] - RunActivity.Step1stAbsorb3[2];
		
		std = (abs[0] + abs[1] + abs[2]) / 3;
		
		for(int i = 0; i < 3; i++) {
			
			if(std > abs[i]) dev[i] = std - abs[i];
			else dev[i] = abs[i] - std;
		}
		
		if(dev[0] > dev[1]) idx = 0; 
		else idx = 1;
		
		if(dev[2] > dev[idx]) idx = 2;
		
		sum = abs[0] + abs[1] + abs[2];
		
		avg = (sum - abs[idx]) / 2;
		
		return avg;
	}
	
	public double Absorb2ndHandling() {
		
		double abs[] = new double[3],
			   dev[] = new double[3],
			   std, 
			   sum, 
			   avg, 
			   res;
		int idx = 0;
		
		abs[0] = RunActivity.Step2ndAbsorb1[1] - RunActivity.Step2ndAbsorb1[2];
		abs[1] = RunActivity.Step2ndAbsorb2[1] - RunActivity.Step2ndAbsorb2[2];
		abs[2] = RunActivity.Step2ndAbsorb3[1] - RunActivity.Step2ndAbsorb3[2];
		
		std = (abs[0] + abs[1] + abs[2]) / 3;
				
		for(int i = 0; i < 3; i++) {
			
			if(std > abs[i]) dev[i] = std - abs[i];
			else dev[i] = abs[i] - std;
		}
		
		if(dev[0] > dev[1]) idx = 0; 
		else idx = 1;
		
		if(dev[2] > dev[idx]) idx = 2;
		
		sum = abs[0] + abs[1] + abs[2];
		
		avg = (sum - abs[idx]) / 2;
		
		return avg;
	}
	
	public void WhichIntent(TargetIntent Itn) { // Activity conversion
		
		switch(Itn) {
		
		case Home			:				
			Intent HomeIntent = new Intent(getApplicationContext(), HomeActivity.class);
			startActivity(HomeIntent);
			break;
		
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
