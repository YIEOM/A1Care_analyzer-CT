package isens.hba1c_analyzer;

import java.text.DecimalFormat;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ControlTestActivity extends Activity {
	
	private SerialPort ControlSerial;
	
	private RelativeLayout cTestLayout;
	private View detailPopupView;
	private PopupWindow detailPopup;
	
	private TextView TestNumText [] = new TextView[5],
					 TypeText    [] = new TextView[5],
					 ResultText  [] = new TextView[5],
					 DateTimeText[] = new TextView[5],
					 patientID,
					 testDate,
					 typeDetailText,
					 primary,
					 range,
					 ref,
					 testNo,
					 operatorID,
					 result;
	
	private static TextView TimeText;
	private static ImageView deviceImage;
	
	private Button homeIcon,
				   backIcon,
				   detailViewBtn,
				   nextViewBtn,
				   preViewBtn,
				   printBtn,
				   cancleBtn;
	
	private ImageButton checkBoxBtn1,
						checkBoxBtn2,
						checkBoxBtn3,
						checkBoxBtn4,
						checkBoxBtn5;
	
	private String dateTime[] = new String[5],
				   testNum [] = new String[5],
				   refNum  [] = new String[5],
				   hbA1c   [] = new String[5],
				   typeStr [] = new String[5],
				   pID     [] = new String[5];
	
	private boolean checkFlag = false,
					btnState = false;
	private ImageButton whichBox = null;
	
	private int boxNum = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.controltest);
		
		TimeText = (TextView) findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);
		
		/* Popup window activation */
		cTestLayout = (RelativeLayout)findViewById(R.id.ctestlayout);
		detailPopupView = View.inflate(this, R.layout.detailviewpopup, null);
		detailPopup = new PopupWindow(detailPopupView, 526, 264, true);
		
		patientID = (TextView) detailPopupView.findViewById(R.id.patient);
		testDate = (TextView) detailPopupView.findViewById(R.id.testdate);
		typeDetailText = (TextView) detailPopupView.findViewById(R.id.type);
		primary = (TextView) detailPopupView.findViewById(R.id.primary);
		range = (TextView) detailPopupView.findViewById(R.id.range);
		ref = (TextView) detailPopupView.findViewById(R.id.ref);
		testNo = (TextView) detailPopupView.findViewById(R.id.testno);
		operatorID = (TextView) detailPopupView.findViewById(R.id.operator);
		result = (TextView) detailPopupView.findViewById(R.id.result);
		
		ControlInit();
		
		checkBoxBtn1 = (ImageButton) findViewById(R.id.chdckbox1);
		checkBoxBtn1.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				
				if(event.getAction() == MotionEvent.ACTION_DOWN) { 

					boxNum = 1;
					PressedCheckBox(checkBoxBtn1);
				}
				return false;
			}
		});
		
		checkBoxBtn2 = (ImageButton) findViewById(R.id.chdckbox2);
		checkBoxBtn2.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				
				if(event.getAction() == MotionEvent.ACTION_DOWN) { 
					
					boxNum = 2;
					PressedCheckBox(checkBoxBtn2);
				}
				return false;
			}
		});
		
		checkBoxBtn3 = (ImageButton) findViewById(R.id.chdckbox3);
		checkBoxBtn3.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				
				if(event.getAction() == MotionEvent.ACTION_DOWN) { 
					
					boxNum = 3;
					PressedCheckBox(checkBoxBtn3);
				}
				return false;
			}
		});
		
		checkBoxBtn4 = (ImageButton) findViewById(R.id.chdckbox4);
		checkBoxBtn4.setOnTouchListener(new View.OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
	
				if(event.getAction() == MotionEvent.ACTION_DOWN) { 
					
					boxNum = 4;
					PressedCheckBox(checkBoxBtn4);
				}
				return false;
			}
		});
		
		checkBoxBtn5 = (ImageButton) findViewById(R.id.chdckbox5);
		checkBoxBtn5.setOnTouchListener(new View.OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				
				if(event.getAction() == MotionEvent.ACTION_DOWN) { 
					
					boxNum = 5;
					PressedCheckBox(checkBoxBtn5);
				}
				return false;
			}
		});
		
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
		
		/*Memory Activity activation*/
		backIcon = (Button)findViewById(R.id.backicon);
		backIcon.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				if(!btnState) {
					
					btnState = true;
				
					backIcon.setEnabled(false);
					
					WhichIntent(TargetIntent.Memory);
				}
			}
		});
		
		preViewBtn = (Button)findViewById(R.id.previousviewbtn);
		preViewBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
				
					preViewBtn.setEnabled(false);
					
					WhichIntent(TargetIntent.PreFile);
				}
			}
		});
		
		/*DetailView pop-up window activation*/
		detailViewBtn = (Button)findViewById(R.id.detailviewbtn);
		detailViewBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if(!btnState) {
			
					btnState = true;
					
					DisplayDetailView();
					cancleBtn.setEnabled(true);
				}
			}
		});
		
		nextViewBtn = (Button)findViewById(R.id.nextviewbtn);
		nextViewBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
					
					nextViewBtn.setEnabled(false);
					
					WhichIntent(TargetIntent.NextFile);
				}
			}
		});
		
		/*Printer operation activation*/
		printBtn = (Button)detailPopupView.findViewById(R.id.printbtn);
		printBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;
					
					PrintRecordData();
				}
			}
		});
		
		/*DetailView pop-up window termination*/
		cancleBtn = (Button)detailPopupView.findViewById(R.id.canclebtn);
		cancleBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;
					
					cancleBtn.setEnabled(false);

					detailPopup.dismiss();
					
					detailViewBtn.setEnabled(true);
					
					btnState = false;
				}
			}
		});
	}	
	
	public void ControlInit() {
		
		TimerDisplay.timerState = whichClock.ControlClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		
		ControlDisplay();
		
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
	
	public void GetItnData() { // getting the intent data
		
		Intent itn = getIntent();
		
		dateTime = itn.getStringArrayExtra("DateTime");
		testNum  = itn.getStringArrayExtra("TestNum");
		refNum   = itn.getStringArrayExtra("RefNumber");
		hbA1c    = itn.getStringArrayExtra("HbA1c");
		pID      = itn.getStringArrayExtra("PatientID");
		
//		Log.w("GetItnData", "Cartridge Lot : " + refNum[0] + " HbA1c : " + hbA1c[0]);
	}
		
	public void ControlText() { // textview activation
		
		TestNumText [0] = (TextView) findViewById(R.id.testNum1);
		TypeText    [0] = (TextView) findViewById(R.id.type1);
		ResultText  [0] = (TextView) findViewById(R.id.result1);
		DateTimeText[0] = (TextView) findViewById(R.id.dateTime1);
		
		TestNumText [1] = (TextView) findViewById(R.id.testNum2);
		TypeText    [1] = (TextView) findViewById(R.id.type2);
		ResultText  [1] = (TextView) findViewById(R.id.result2);
		DateTimeText[1] = (TextView) findViewById(R.id.dateTime2);
		
		TestNumText [2] = (TextView) findViewById(R.id.testNum3);
		TypeText    [2] = (TextView) findViewById(R.id.type3);
		ResultText  [2] = (TextView) findViewById(R.id.result3);
		DateTimeText[2] = (TextView) findViewById(R.id.dateTime3);
		
		TestNumText [3] = (TextView) findViewById(R.id.testNum4);
		TypeText    [3] = (TextView) findViewById(R.id.type4);
		ResultText  [3] = (TextView) findViewById(R.id.result4);
		DateTimeText[3] = (TextView) findViewById(R.id.dateTime4);
		
		TestNumText [4] = (TextView) findViewById(R.id.testNum5);
		TypeText    [4] = (TextView) findViewById(R.id.type5);
		ResultText  [4] = (TextView) findViewById(R.id.result5);
		DateTimeText[4] = (TextView) findViewById(R.id.dateTime5);
	}
	
	public void ControlDisplay() { // displaying the patient data
			
		GetItnData();
		ControlText();
		
		for(int i = 0; i < 5; i++) {
			
			if(testNum[i] != null) {
			
				TestNumText [i].setText(testNum[i]);
				typeStr     [i] = "Control A1c";
				TypeText    [i].setText(typeStr[i]);
				ResultText  [i].setText(hbA1c[i] + "%"); // 17 - 48
	        	DateTimeText[i].setText(dateTime[i].substring(0, 4) + "." + dateTime[i].substring(4, 6) + "." + dateTime[i].substring(6, 8) + " " + dateTime[i].substring(8, 10) + " " + dateTime[i].substring(10, 12) + ":" + dateTime[i].substring(12, 14));	
			}	
    	}
	}
	
	public void PressedCheckBox(ImageButton box) { // displaying the button pressed
		
		if(checkFlag == false) { // whether or not box is checked

			checkFlag = true;
			box.setBackgroundResource(R.drawable.checkbox_s); // changing to checked box
			
		} else {

			whichBox.setBackgroundResource(R.drawable.checkbox); // changing to not checked box

			if(whichBox == box) {
				
				checkFlag = false;
				
			} else {
				
				box.setBackgroundResource(R.drawable.checkbox_s);
			}
		}
		
		whichBox = box;
	}
	
	public void DisplayDetailView() { // displaying the detail patient data

		if(checkFlag == true && testNum[boxNum - 1] != null) {
			
			patientID.setText(pID[boxNum - 1]);
			testDate.setText(dateTime[boxNum - 1].substring(2, 4) + "." + dateTime[boxNum - 1].substring(4, 6) + "." + dateTime[boxNum - 1].substring(6, 8) + " " + dateTime[boxNum - 1].substring(8, 10) + " " + dateTime[boxNum - 1].substring(10, 12) + ":" + dateTime[boxNum - 1].substring(12, 14));
			typeDetailText.setText(typeStr[boxNum - 1]);
			primary.setText("NGSP");
			range.setText("4.0 - 6.0%");
			ref.setText(refNum[boxNum - 1]);
			testNo.setText(testNum[boxNum - 1]);
			operatorID.setText("Guest");
			result.setText(hbA1c[boxNum - 1] + "%");
			
			detailViewBtn.setEnabled(false);
			detailPopup.showAtLocation(cTestLayout, Gravity.CENTER, 0, 0);
			detailPopup.setAnimationStyle(0);
		}
		
		btnState = false;
	}
		            		
	public void PrintRecordData() {
		
		DecimalFormat pIDLenDfm = new DecimalFormat("00");
		
		StringBuffer txData = new StringBuffer();
		
		txData.delete(0, txData.capacity());
		
		txData.append(dateTime[boxNum - 1].substring(0, 4));
		txData.append(dateTime[boxNum - 1].substring(4, 6));
		txData.append(dateTime[boxNum - 1].substring(6, 8));
		txData.append(dateTime[boxNum - 1].substring(8, 10));
		txData.append(dateTime[boxNum - 1].substring(10, 12));
		txData.append(dateTime[boxNum - 1].substring(12, 14));
		txData.append(testNum[boxNum - 1]);
		txData.append(refNum[boxNum - 1]);
		txData.append(pIDLenDfm.format(pID[boxNum - 1].length()));
		txData.append(pID[boxNum - 1]);
		txData.append(hbA1c[boxNum - 1]);
		
		ControlSerial = new SerialPort();
		ControlSerial.PrinterTxStart(SerialPort.PRINTRECORD, txData);
		
		SerialPort.Sleep(100);
		
		btnState = false;
	}
	
	public void WhichIntent(TargetIntent Itn) { // Activity conversion
		
		switch(Itn) {
		
		case Home		:				
			Intent HomeIntent = new Intent(getApplicationContext(), HomeActivity.class);
			startActivity(HomeIntent);
			finish();
			break;
						
		case Memory		:				
			Intent MemoryIntent = new Intent(getApplicationContext(), MemoryActivity.class);
			startActivity(MemoryIntent);
			finish();
			break;
			
		case NextFile	:
			if((RemoveActivity.ControlDataCnt-2)/5 > MemoryActivity.DataPage) {
			
				Intent NextFileIntent = new Intent(getApplicationContext(), FileLoadActivity.class);
				NextFileIntent.putExtra("DataCnt", RemoveActivity.ControlDataCnt);
				NextFileIntent.putExtra("DataPage", ++MemoryActivity.DataPage);
				NextFileIntent.putExtra("Type", (int) FileLoadActivity.CONTROL);
				startActivity(NextFileIntent);
				finish();
			} else nextViewBtn.setEnabled(true);
			break;
		
		case PreFile	:
			if(MemoryActivity.DataPage > 0){
			
				Intent PreFileIntent = new Intent(getApplicationContext(), FileLoadActivity.class);
				PreFileIntent.putExtra("DataCnt", RemoveActivity.ControlDataCnt);
				PreFileIntent.putExtra("DataPage", --MemoryActivity.DataPage);
				PreFileIntent.putExtra("Type", (int) FileLoadActivity.CONTROL);
				startActivity(PreFileIntent);
				finish();
			} else preViewBtn.setEnabled(true);
			break;
			
		default		:	
			break;			
		}
		
		btnState = false;
	}

	public void finish() {
		
		super.finish();
		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
}