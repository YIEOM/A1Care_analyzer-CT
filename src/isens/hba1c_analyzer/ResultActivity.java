package isens.hba1c_analyzer;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.TimerDisplay.whichClock;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ResultActivity extends Activity {
		
	private Temperature ResultTmp;
	private SerialPort ResultSerial;
	
	public static TextView TimeText;
	private static ImageView deviceImage;
	
	public static EditText PatientIDText;
	
	private TextView HbA1cText,
					 DateText,
					 AMPMText,
					 Ref;
	
	private RelativeLayout resultLinear;
	private View errorPopupView;
	private PopupWindow errorPopup;
	
	private Button homeIcon,
				   printBtn,
				   nextSampleBtn,
				   errorBtn;
	
	private String getTime[] = new String[6];
	
	public static int ItnData;
	public int dataCnt;
	private double cellBlockEndTmp;
	
	public boolean btnState = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.result);
		
		TimeText = (TextView) findViewById(R.id.timeText);		
		deviceImage = (ImageView) findViewById(R.id.device);
		PatientIDText = (EditText) findViewById(R.id.patientidtext);
				
		/* Popup window activation */
		resultLinear = (RelativeLayout)findViewById(R.id.resultlinear);
		errorPopupView = View.inflate(this, R.layout.errorbtnpopup, null);
		errorPopup = new PopupWindow(errorPopupView, 800, 480, true);
		
		ResultInit();
		
		/*Home Activity activation*/
		homeIcon = (Button)findViewById(R.id.homeicon);
		homeIcon.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;
					
					homeIcon.setEnabled(false);
					
					WhichIntent(TargetIntent.Home);
				}
			}
		});
		
		printBtn = (Button)findViewById(R.id.printbtn);
		printBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
					
					PrintResultData();
				}
			}
		});
		
		/*Test Activity activation*/
		nextSampleBtn = (Button)findViewById(R.id.nextsamplebtn);
		nextSampleBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {

				if(!btnState) {
					
					btnState = true;
					
					nextSampleBtn.setEnabled(false);
				
					WhichIntent(TargetIntent.Run);
				}
			}
		});
		
		/* error pop-up Close */
		errorBtn = (Button)errorPopupView.findViewById(R.id.errorbtn);
		errorBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;
					
					errorPopup.dismiss();
				
					btnState = false;
				}
			}
		});
	}
	
	public void ResultInit() {
		
		TimerDisplay.timerState = whichClock.ResultClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		
		GetCurrTime();
		GetDataCnt();
		
//		ResultTmp = new Temperature();
//		cellBlockEndTmp = ResultTmp.CellTmpRead();
		
		HbA1cText = (TextView)findViewById(R.id.hba1cPct);
		DateText = (TextView)findViewById(R.id.r_testdate1);
		AMPMText = (TextView)findViewById(R.id.r_testdate2);
		Ref = (TextView) findViewById(R.id.ref);
				
		Intent itn = getIntent();
		ItnData = itn.getIntExtra("RunState", 0);
		
		switch(ItnData) {
			
		case HomeActivity.NORMAL_OPERATION		:
			if(HomeActivity.ANALYZER_SW == HomeActivity.DEVEL) {
				
				RunActivity.HbA1cPctDbl = 0;
			} else if(HomeActivity.ANALYZER_SW == HomeActivity.DEMO) {
				
				int rem;
				
				rem = (int) ((Math.random() * 10) + 1) % 3;
				
				switch(rem) {
				
				case 0	:
					RunActivity.HbA1cPctDbl = 5.5;
					break;
					
				case 1	:
					RunActivity.HbA1cPctDbl = 6.7;
					break;
				
				case 2	:
					RunActivity.HbA1cPctDbl = 8.3;
					break;
					
				default	:
					break;
				}
			}
			
			DecimalFormat hbA1cFormat = new DecimalFormat("0.0");
			HbA1cText.setText(hbA1cFormat.format(RunActivity.HbA1cPctDbl) + "%");
			break;
			
		case HomeActivity.tHb_LOW_ERROR			:
			HbA1cText.setText(R.string.e111);
			ErrorPopup("E111");
			break;
			
		case HomeActivity.tHb_HIGH_ERROR		:
			HbA1cText.setText(R.string.e112);
			ErrorPopup("E112");
			break;
			
		case HomeActivity.A1c_LOW_ERROR			:
			HbA1cText.setText(R.string.e121);
			ErrorPopup("E121");
			break;
			
		case HomeActivity.A1c_HIGH_ERROR		:
			HbA1cText.setText(R.string.e122);
			ErrorPopup("E122");
			break;
			
		case HomeActivity.FILTER_MOTOR_ERROR		:
			HbA1cText.setText(R.string.e212);
			ErrorPopup("E212");
			break;
			
		case HomeActivity.SHAKING_MOTOR_ERROR		:
			HbA1cText.setText(R.string.e211);
			ErrorPopup("E221");
			break;
			
		case HomeActivity.COMMUNICATION_ERROR	:
			HbA1cText.setText(R.string.e241);
			ErrorPopup("E241");
			break;
			
		case HomeActivity.STOP_RESULT			:
			HbA1cText.setText(R.string.stop);
			break;
		}
		
		DateText.setText(TimerDisplay.rTime[0] + "." + TimerDisplay.rTime[1] + "." + TimerDisplay.rTime[2] + " " + TimerDisplay.rTime[4] + ":" + TimerDisplay.rTime[5]);
		AMPMText.setText(TimerDisplay.rTime[3]);
		Ref.setText(Barcode.RefNum);
		
		if(TestActivity.WhichTest == TestActivity.STABLE) {
			
			if(ItnData != HomeActivity.STOP_RESULT) WhichIntent(TargetIntent.Test); // Stable
			else HomeActivity.NumofStable = 0;
		}
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

	public void PatientIDDisplay(final StringBuffer str) {
		
		new Thread(new Runnable() {
		    public void run() {    
		        runOnUiThread(new Runnable(){
		            public void run() {
		            	
		            	PatientIDText.setText(str.substring(0, str.length() - 1));
		            }
		        });
		    }
		}).start();
	}
	
	public void ErrorPopup(final String str) { // E101 error pop-up window
				
		errorPopupView.post(new Runnable() {
	        public void run() {
	
	        	errorPopup.showAtLocation(resultLinear, Gravity.CENTER, 0, 0);
				errorPopup.setAnimationStyle(0);					
							
				TextView errorText = (TextView)errorPopup.getContentView().findViewById(R.id.errortext);
				errorText.setText(str);	
	        }
	    });
	}
	
	public void GetCurrTime() { // getting the current date and time
		
		getTime[0] = TimerDisplay.rTime[0];
		getTime[1] = TimerDisplay.rTime[1];
		getTime[2] = TimerDisplay.rTime[2];
		getTime[3] = TimerDisplay.rTime[3];		
		if(TimerDisplay.rTime[4].length() != 2) getTime[4] = "0" + TimerDisplay.rTime[4];
		else getTime[4] = TimerDisplay.rTime[4];
		getTime[5] = TimerDisplay.rTime[5];			
	}
	
	public void GetDataCnt() {
		
		if(TestActivity.WhichTest != TestActivity.STABLE) {
		
			if(Barcode.RefNum.substring(0, 1).equals("B")) dataCnt = RemoveActivity.ControlDataCnt;
			else dataCnt = RemoveActivity.PatientDataCnt;
		
		} else dataCnt = RemoveActivity.ControlDataCnt;
	}
	
	public void PrintResultData() {
		
		StringBuffer txData = new StringBuffer();
		DecimalFormat dfm = new DecimalFormat("0000"),
					  hbA1cFormat = new DecimalFormat("0.0"),
					  pIDLenDfm = new DecimalFormat("00");
		
		int tempDataCnt;
		
		tempDataCnt = dataCnt % 9999;
		if(tempDataCnt == 0) tempDataCnt = 9999; 
		
		txData.delete(0, txData.capacity());
		
		txData.append(getTime[0]);
		txData.append(getTime[1]);
		txData.append(getTime[2]);
		txData.append(getTime[3]);
		txData.append(getTime[4]);
		txData.append(getTime[5]);
		txData.append(dfm.format(tempDataCnt));
		txData.append(Barcode.RefNum);
		txData.append(pIDLenDfm.format(PatientIDText.getText().toString().length()));
		txData.append(PatientIDText.getText().toString());
		txData.append(hbA1cFormat.format(RunActivity.HbA1cPctDbl));
		
		ResultSerial = new SerialPort();
		ResultSerial.PrinterTxStart(SerialPort.PRINTRESULT, txData);
		
		SerialPort.Sleep(100);
		
		btnState = false;
	}
	
	public void WhichIntent(TargetIntent Itn) { // Activity conversion after intent data deliver
		
		Intent DataSaveIntent = new Intent(getApplicationContext(), FileSaveActivity.class);
		DecimalFormat photoDfm = new DecimalFormat("0.0"),
					  absorbDfm = new DecimalFormat("0.0000"),
					  pIDLenDfm = new DecimalFormat("00");
		
		DataSaveIntent.putExtra("RunState", ItnData);
		DataSaveIntent.putExtra("Year", getTime[0]);
		DataSaveIntent.putExtra("Month", getTime[1]);
		DataSaveIntent.putExtra("Day", getTime[2]);
		DataSaveIntent.putExtra("AmPm", getTime[3]);
		DataSaveIntent.putExtra("Hour", getTime[4]);
		DataSaveIntent.putExtra("Minute", getTime[5]);
		DataSaveIntent.putExtra("DataCnt", dataCnt);
		DataSaveIntent.putExtra("RefNumber", Barcode.RefNum);
		DataSaveIntent.putExtra("PatientIDLen", pIDLenDfm.format(PatientIDText.getText().toString().length()));
		DataSaveIntent.putExtra("PatientID", PatientIDText.getText().toString());
		DataSaveIntent.putExtra("Hba1cPct", photoDfm.format(RunActivity.HbA1cPctDbl));
		
		DataSaveIntent.putExtra("RunMin", (int) RunActivity.runMin);
		DataSaveIntent.putExtra("RunSec", (int) RunActivity.runSec);
		DataSaveIntent.putExtra("BlankVal0", photoDfm.format(RunActivity.BlankValue[0]));
		DataSaveIntent.putExtra("BlankVal1", photoDfm.format(RunActivity.BlankValue[1]));
		DataSaveIntent.putExtra("BlankVal2", photoDfm.format(RunActivity.BlankValue[2]));
		DataSaveIntent.putExtra("BlankVal3", photoDfm.format(RunActivity.BlankValue[3]));
		DataSaveIntent.putExtra("St1Abs1by0", absorbDfm.format(RunActivity.Step1stAbsorb1[0]));
		DataSaveIntent.putExtra("St1Abs1by1", absorbDfm.format(RunActivity.Step1stAbsorb1[1]));
		DataSaveIntent.putExtra("St1Abs1by2", absorbDfm.format(RunActivity.Step1stAbsorb1[2]));
		DataSaveIntent.putExtra("St1Abs2by0", absorbDfm.format(RunActivity.Step1stAbsorb2[0]));
		DataSaveIntent.putExtra("St1Abs2by1", absorbDfm.format(RunActivity.Step1stAbsorb2[1]));
		DataSaveIntent.putExtra("St1Abs2by2", absorbDfm.format(RunActivity.Step1stAbsorb2[2]));
		DataSaveIntent.putExtra("St1Abs3by0", absorbDfm.format(RunActivity.Step1stAbsorb3[0]));
		DataSaveIntent.putExtra("St1Abs3by1", absorbDfm.format(RunActivity.Step1stAbsorb3[1]));
		DataSaveIntent.putExtra("St1Abs3by2", absorbDfm.format(RunActivity.Step1stAbsorb3[2]));
		DataSaveIntent.putExtra("St2Abs1by0", absorbDfm.format(RunActivity.Step2ndAbsorb1[0]));
		DataSaveIntent.putExtra("St2Abs1by1", absorbDfm.format(RunActivity.Step2ndAbsorb1[1]));
		DataSaveIntent.putExtra("St2Abs1by2", absorbDfm.format(RunActivity.Step2ndAbsorb1[2]));
		DataSaveIntent.putExtra("St2Abs2by0", absorbDfm.format(RunActivity.Step2ndAbsorb2[0]));
		DataSaveIntent.putExtra("St2Abs2by1", absorbDfm.format(RunActivity.Step2ndAbsorb2[1]));
		DataSaveIntent.putExtra("St2Abs2by2", absorbDfm.format(RunActivity.Step2ndAbsorb2[2]));
		DataSaveIntent.putExtra("St2Abs3by0", absorbDfm.format(RunActivity.Step2ndAbsorb3[0]));
		DataSaveIntent.putExtra("St2Abs3by1", absorbDfm.format(RunActivity.Step2ndAbsorb3[1]));
		DataSaveIntent.putExtra("St2Abs3by2", absorbDfm.format(RunActivity.Step2ndAbsorb3[2]));
		
		switch(Itn) {
		
		case Home		:
			
			if(TestActivity.WhichTest != TestActivity.STABLE) {
			
				DataSaveIntent.putExtra("WhichIntent", (int) HomeActivity.HOME_ACTIVITY);
			} else {
				
				DataSaveIntent.putExtra("WhichIntent", (int) HomeActivity.TEST_ACTION_ESC);
			}
			
			startActivity(DataSaveIntent);
			
			break;

		case Run	:			
			DataSaveIntent.putExtra("WhichIntent", (int) HomeActivity.ACTION_ACTIVITY);
			startActivity(DataSaveIntent);
			break;

		case Test	:			
			DataSaveIntent.putExtra("WhichIntent", (int) HomeActivity.TEST_ACTIVITY);
			startActivity(DataSaveIntent);
			break;
			
		default			:	
			break;			
		}	
		
		finish();
	}
	
	public void finish() {
		
		super.finish();
//		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
}
