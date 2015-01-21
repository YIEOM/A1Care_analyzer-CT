package isens.hba1c_analyzer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class OperatorSettingActivity extends Activity {
	
	public static int PRE_VIEW = 0,
					  NEXT_VIEW = 1;
	
	public OperatorController OperatorOC;
	public DatabaseHander OperatorDB;
	
	private RelativeLayout operatorLayout;
	private View addOperatorPopupView;
	private PopupWindow addOperatorPopup;
	
	private View modOperatorPopupView;
	private PopupWindow modOperatorPopup;

	private View delOperatorPopupView;
	private PopupWindow delOperatorPopup;
	
	private View errorPopupView;
	private PopupWindow errorPopup;

	private View loginPopupView;
	private PopupWindow loginPopup;
	
	public static TextView TimeText;
	public static ImageView deviceImage;
	
	private Button homeIcon,
				   backIcon,
				   addOperatorBtn,
				   modOperatorBtn,
				   delOperatorBtn,
				   nextViewBtn,
				   preViewBtn,
				   loginBtn;
	
	private ImageButton checkBoxBtn1,
						checkBoxBtn2,
						checkBoxBtn3,
						checkBoxBtn4,
						checkBoxBtn5;
	
	public EditText addIDEText,
					addPasswordEText,
					addCPasswordEText;

	public Button addDoneBtn,
	   			  addCancelBtn;

	public EditText modPasswordEText,
					modNPasswordEText,
					modCPasswordEText;

	public Button modDoneBtn,
				  modCancelBtn;

	public TextView modOperatorText;
	
	public Button delOkBtn,
				  delCancelBtn;
	
	public TextView delOperatorText;
	
	public Button errorBtn;
	
	public TextView errorText;
	
	private TextView OperatorText[] = new TextView[5],
			 		 DateTimeText[] = new TextView[5],
			 		 PasswordText[] = new TextView[5],
			 		 CommentText [] = new TextView[5];
	
	private boolean checkFlag = false;
	private ImageButton whichBox = null;
	
	public TextView pageText;
	
	private int numofRow;
	
	private int boxNum = 0,
				pageNum = 1;

	public boolean btnState = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.operatorsetting);
		
		TimeText = (TextView) findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);
		
		/* Popup window activation */
		operatorLayout = (RelativeLayout)findViewById(R.id.operatorlayout);
		addOperatorPopupView = View.inflate(this, R.layout.addoperatorpopup, null);
		addOperatorPopup = new PopupWindow(addOperatorPopupView, 800, 480, true);
		
		modOperatorPopupView = View.inflate(this, R.layout.modoperatorpopup, null);
		modOperatorPopup = new PopupWindow(modOperatorPopupView, 800, 480, true);
		
		modOperatorPopupView = View.inflate(this, R.layout.modoperatorpopup, null);
		modOperatorPopup = new PopupWindow(modOperatorPopupView, 800, 480, true);
		
		delOperatorPopupView = View.inflate(this, R.layout.oxpopup, null);
		delOperatorPopup = new PopupWindow(delOperatorPopupView, 800, 480, true);
		
		errorPopupView = View.inflate(this, R.layout.errorbtnpopup, null);
		errorPopup = new PopupWindow(errorPopupView, 800, 480, true);
		
//		loginPopupView = View.inflate(this, R.layout.loginpopup, null);
//		loginPopup = new PopupWindow(loginPopupView, 800, 480, true);
		
		addIDEText = (EditText)addOperatorPopupView.findViewById(R.id.id);
		addPasswordEText = (EditText)addOperatorPopupView.findViewById(R.id.password);
		addCPasswordEText = (EditText)addOperatorPopupView.findViewById(R.id.cpassword);
		
		modOperatorText = (TextView)modOperatorPopupView.findViewById(R.id.id);
		modPasswordEText = (EditText)modOperatorPopupView.findViewById(R.id.password);
		modNPasswordEText = (EditText)modOperatorPopupView.findViewById(R.id.npassword);
		modCPasswordEText = (EditText)modOperatorPopupView.findViewById(R.id.cpassword);
		
		delOperatorText = (TextView)delOperatorPopupView.findViewById(R.id.oxtext);
		
		errorText = (TextView)errorPopupView.findViewById(R.id.errortext);
		
		OperatorInit();
		
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
		
		preViewBtn = (Button)findViewById(R.id.previousviewbtn);
		preViewBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
			
				if(!btnState) {
				
					TurnPage(PRE_VIEW);
				}
			}
		});
		
		/*Login Operator pop-up window activation*/
		loginBtn = (Button)findViewById(R.id.loginbtn);
		loginBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
					
					Login();
					
					btnState = false;
				}
			}
		});
		
		/*Modify Operator pop-up window activation*/
		modOperatorBtn = (Button)findViewById(R.id.modifybtn);
		modOperatorBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
				
					DisplayModOperator();
					modCancelBtn.setEnabled(true);
				}
			}
		});
		
		/*Addition Operator pop-up window activation*/
		addOperatorBtn = (Button)findViewById(R.id.addbtn);
		addOperatorBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
				
					DisplayAddOperator();
					addCancelBtn.setEnabled(true);
				}
			}
		});
		
		/* Delete Operator pop-up window activation */
		delOperatorBtn = (Button)findViewById(R.id.deletebtn);
		delOperatorBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
				
					DisplaydelOperator();
					delCancelBtn.setEnabled(true);
				}
			}
		});
		
		nextViewBtn = (Button)findViewById(R.id.nextviewbtn);
		nextViewBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
			
				if(!btnState) {
					
					TurnPage(NEXT_VIEW);
				}
			}
		});
		
		/*Setting Activity activation*/
		backIcon = (Button)findViewById(R.id.backicon);
		backIcon.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {

				if(!btnState) {
					
					btnState = true;
					backIcon.setEnabled(false);
					
					WhichIntent(TargetIntent.Setting);
				}
			}
		});
		
		/*Add new account activation*/
		addDoneBtn = (Button)addOperatorPopupView.findViewById(R.id.donebtn);
		addDoneBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;
				
					AddOperator();
					
					btnState = false;
				}
			}
		});
		
		/*AddOperator pop-up window termination*/
		addCancelBtn = (Button)addOperatorPopupView.findViewById(R.id.canclebtn);
		addCancelBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;
					
					addCancelBtn.setEnabled(false);
					
					addOperatorPopup.dismiss();
					
					addOperatorBtn.setEnabled(true);
				
					btnState = false;
				}
			}
		});
		
		/*Modify account activation*/
		modDoneBtn = (Button)modOperatorPopupView.findViewById(R.id.donebtn);
		modDoneBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;
				
					ModOperator();
					
					btnState = false;
				}
			}
		});
		
		/*ModOperator pop-up window termination*/
		modCancelBtn = (Button)modOperatorPopupView.findViewById(R.id.canclebtn);
		modCancelBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;
					
					modCancelBtn.setEnabled(false);
					
					modOperatorPopup.dismiss();
					
					modOperatorBtn.setEnabled(true);
				
					btnState = false;
				}
			}
		});
		
		/*Delete account activation*/
		delOkBtn = (Button)delOperatorPopupView.findViewById(R.id.yesbtn);
		delOkBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;
				
					DelOperator();
					
					btnState = false;
				}
			}
		});
		
		/*DelOperator pop-up window termination*/
		delCancelBtn = (Button)delOperatorPopupView.findViewById(R.id.nobtn);
		delCancelBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if(!btnState) {
					
					btnState = true;
					
					delCancelBtn.setEnabled(false);
					
					delOperatorPopup.dismiss();
					
					delOperatorBtn.setEnabled(true);
				
					btnState = false;
				}
			}
		});
		
		/*Error pop-up window termination*/
		errorBtn = (Button)errorPopupView.findViewById(R.id.errorbtn);
		errorBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if(!btnState) {
					
					errorPopup.dismiss();
				}
			}
		});
	}	
	
	public void OperatorInit() {
		
		int count;
		
		TimerDisplay.timerState = whichClock.OperatorSettingClock;		
		CurrTimeDisplay();
		ExternalDeviceDisplay();
		
		OperatorOC = new OperatorController(this, getApplicationContext(), R.id.operatorlayout);
		count = OperatorOC.OperatorCount();
		d_OperatorDisplay(this, OperatorOC.ReadOperator(count), count, count);
		//		OperatorText();
//		OperatorDisplay(this, OperatorCount());
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

	public void Login() {
		
		OperatorOC = new OperatorController(this, getApplicationContext(), R.id.operatorlayout);
		OperatorOC.LoginDisplay();
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
	
	public int OperatorCount() {
		
		int count;
		
		OperatorDB = new DatabaseHander(this);
		count = OperatorDB.GetRowCount();
		
		Log.w("Get operator", "the number : " + count);
		
		numofRow = count;
		
		pageNum = 1;
		
		return count;
	}
	
	public void OperatorText(Activity activity) {
		
		OperatorText[0] = (TextView) activity.findViewById(R.id.operator1);
		DateTimeText[0] = (TextView) activity.findViewById(R.id.dateTime1);
		PasswordText[0] = (TextView) activity.findViewById(R.id.password1);
		CommentText [0] = (TextView) activity.findViewById(R.id.comment1);
		
		OperatorText[1] = (TextView) activity.findViewById(R.id.operator2);
		DateTimeText[1] = (TextView) activity.findViewById(R.id.dateTime2);
		PasswordText[1] = (TextView) activity.findViewById(R.id.password2);
		CommentText [1] = (TextView) activity.findViewById(R.id.comment2);
		
		OperatorText[2] = (TextView) activity.findViewById(R.id.operator3);
		DateTimeText[2] = (TextView) activity.findViewById(R.id.dateTime3);
		PasswordText[2] = (TextView) activity.findViewById(R.id.password3);
		CommentText [2] = (TextView) activity.findViewById(R.id.comment3);
		
		OperatorText[3] = (TextView) activity.findViewById(R.id.operator4);
		DateTimeText[3] = (TextView) activity.findViewById(R.id.dateTime4);
		PasswordText[3] = (TextView) activity.findViewById(R.id.password4);
		CommentText [3] = (TextView) activity.findViewById(R.id.comment4);
		
		OperatorText[4] = (TextView) activity.findViewById(R.id.operator5);
		DateTimeText[4] = (TextView) activity.findViewById(R.id.dateTime5);
		PasswordText[4] = (TextView) activity.findViewById(R.id.password5);
		CommentText [4] = (TextView) activity.findViewById(R.id.comment5);
		
		pageText = (TextView) activity.findViewById(R.id.pagetext);
	}
	
	public void OperatorDisplay(Activity activity, int last) {
		
		int first,
			dataCnt;
		
		String tempDate,
			   tempPassword,
			   formDate,
			   formPassword;
		
		String tempHour[] = new String[2],
			   rowData[] = new String[4];
		
		if(last > 5) {
			
			first = last - 5;
			dataCnt = 5;
		
		} else {
			
			first = 0;
			dataCnt = last;
		}
		
		Log.w("Operator display", "first : " + first + " last : " + last);

		OperatorDB = new DatabaseHander(this);
		
		OperatorText(activity);
		
		for(int i = 0; i < 5; i++) {
			
			OperatorText[i].setText("");
			DateTimeText[i].setText("");
			PasswordText[i].setText("");
			CommentText [i].setText("");
		}
		
		for(int i = 0; i < dataCnt; i++) {
			
			rowData = OperatorDB.GetRowWithNumber(last - (i + 1));
				
			tempDate = rowData[1];
			tempPassword = rowData[2];
			
			tempHour = TimeHandling(tempDate.substring(8, 10));
			
			formDate = tempDate.substring(0, 4) + "." + tempDate.substring(4, 6) + "." + tempDate.substring(6, 8) + " " + tempHour[0] + " " + tempHour[1] + ":" + tempDate.substring(10, 12);
			formPassword = PasswordHandling(tempPassword);	
			
			OperatorText[i].setText(rowData[0]);
			DateTimeText[i].setText(formDate);
			PasswordText[i].setText(formPassword);
			CommentText [i].setText(rowData[3]);
		}
		
		String page = Integer.toString(pageNum) + " / " + Integer.toString(numofRow/5 + 1);
		
		pageText.setText(page);
		
		Log.w("Operator display", "last : " + last);
	}
	
	public void d_OperatorDisplay(Activity activity, String[][] Operator, int last, int numofRow) {
		
		int first,
			dataCnt;
		
		if(last > 5) {
			
			first = last - 5;
			dataCnt = 5;
		
		} else {
			
			first = 0;
			dataCnt = last;
		}
		
		OperatorText(activity);
		
		for(int i = 0; i < 5; i++) {
			
			OperatorText[i].setText("");
			DateTimeText[i].setText("");
			PasswordText[i].setText("");
			CommentText [i].setText("");
		}
		
		for(int i = 0; i < dataCnt; i++) {	
			
			OperatorText[i].setText(Operator[0][i]);
			DateTimeText[i].setText(Operator[1][i]);
			PasswordText[i].setText(Operator[2][i]);
			CommentText [i].setText(Operator[3][i]);
		}
		
		if(last == numofRow) pageNum = 1;
		
		String page = Integer.toString(pageNum) + " / " + Integer.toString(numofRow/5 + 1);
		
		pageText.setText(page);
		
		Log.w("Operator display", "last : " + last);
	}
	
	public void DisplayAddOperator() {

		addIDEText.setText("");
		addPasswordEText.setText("");
		addCPasswordEText.setText("");
		
		addOperatorBtn.setEnabled(false);
		addOperatorPopup.showAtLocation(operatorLayout, Gravity.CENTER, 0, 0);
		addOperatorPopup.setAnimationStyle(0);
		
		btnState = false;
	}
	
	public void AddOperator() {
		
		String id = addIDEText.getText().toString(),
			   aPassword = addPasswordEText.getText().toString(),
			   cPassword = addCPasswordEText.getText().toString();
		
		OperatorDB = new DatabaseHander(this);
		
		if(!id.equals("")) {
			
			if(!OperatorDB.CheckIDDuplication(id)) {
				
				if(!aPassword.equals("")) {
					
					if(!cPassword.equals("")) {
						
						if(aPassword.equals(cPassword)) {
							 
							OperatorDB.AddOperator(id, TimerDisplay.rTime[0] + TimerDisplay.rTime[1] + TimerDisplay.rTime[2] + TimerDisplay.rTime[7] + TimerDisplay.rTime[5], aPassword);
							
							addOperatorPopup.dismiss();
							
							addOperatorBtn.setEnabled(true);
							
							OperatorDisplay(this, OperatorCount());
						
						} else {
							
							ErrorDisplay(getString(R.string.w015));// 비밀번호가 일치하지 않아요 팝업
						}
						
					} else {
						
						ErrorDisplay(getString(R.string.w014));// 확인용 비밀번호를 입력해주세요
					}
					
				} else {
					
					ErrorDisplay(getString(R.string.w013));// 비밀번호를 입력해 주세요 팝업
				}
				
			} else {
				
				ErrorDisplay(getString(R.string.w012));// ID가 중복입니다 팝업
			}
	
		} else {
			
			ErrorDisplay(getString(R.string.w011));// ID를 입력해주세요 팝업
		}
	}
	
	public void DisplayModOperator() {
		
		if(checkFlag == true && !OperatorText[boxNum - 1].getText().toString().equals("")) {
		
			modPasswordEText.setText("");
			modNPasswordEText.setText("");
			modCPasswordEText.setText("");

			modOperatorText.setText(OperatorText[boxNum - 1].getText().toString());
			
			modOperatorBtn.setEnabled(false);
			modOperatorPopup.showAtLocation(operatorLayout, Gravity.CENTER, 0, 0);
			modOperatorPopup.setAnimationStyle(0);
		}
		
		btnState = false;
	}
	
	public void ModOperator() {
		
		String iPassword = modPasswordEText.getText().toString(),
			   nPassword = modNPasswordEText.getText().toString(),
			   cPassword = modCPasswordEText.getText().toString();
		
		OperatorDB = new DatabaseHander(this);
		
		if(!iPassword.equals("")) {
			
			if(iPassword.equals(OperatorDB.GetPassword(OperatorText[boxNum - 1].getText().toString()))) {
				
				if(!nPassword.equals("")) {
					
					if(!cPassword.equals("")) {
						
						if(nPassword.equals(cPassword)) {
							 
							OperatorDB.UpdateOperator(OperatorText[boxNum - 1].getText().toString(), nPassword);
							
							modOperatorPopup.dismiss();
							
							modOperatorBtn.setEnabled(true);

							OperatorDisplay(this, OperatorCount());
							
						} else {
							
							ErrorDisplay(getString(R.string.w018));// 비밀번호가 일치하지 않아요 팝업
						}
						
					} else {
						
						ErrorDisplay(getString(R.string.w014));// 확인용 비밀번호를 입력해주세요
					}
					
				} else {
					
					ErrorDisplay(getString(R.string.w017));// 비밀번호를 입력해 주세요 팝업
				}
				
			} else {
				
				ErrorDisplay(getString(R.string.w016));// 비밀번호가 다릅니다
			}
	
		} else {
			
			ErrorDisplay(getString(R.string.w013));// 비밀번호를 입렵하세요
		}
	}
	
	public void DisplaydelOperator() {
		
		if(checkFlag == true && OperatorText[boxNum - 1].getText().toString() != null) {
			
			delOperatorText.setText("ID : " + OperatorText[boxNum - 1].getText().toString() + " " + getString(R.string.delete));
			
			delOperatorBtn.setEnabled(false);
			delOperatorPopup.showAtLocation(operatorLayout, Gravity.CENTER, 0, 0);
			delOperatorPopup.setAnimationStyle(0);
		}
		
		btnState = false;
	}
	
	public void DelOperator() {
		
		OperatorDB = new DatabaseHander(this);
		OperatorDB.DeleteOperator(OperatorText[boxNum - 1].getText().toString());
		
		delOperatorPopup.dismiss();
		
		delOperatorBtn.setEnabled(true);

		OperatorDisplay(this, OperatorCount());
	}
	
	public void TurnPage(int direction) {
		
		int count,
			last;
		
		Log.w("Turn page", "dir : " + direction);
		
		switch(direction) {
		
		case 0	:
			if(pageNum > 1) {
				pageNum--;
				OperatorOC = new OperatorController(this, getApplicationContext(), R.id.operatorlayout);
				count = OperatorOC.OperatorCount();
				last = count-((pageNum-1)*5);
				d_OperatorDisplay(this, OperatorOC.ReadOperator(last), last, count);
			}
			break;
			
		case 1	:
			if(((numofRow/5)+1) > pageNum) {
				
				pageNum++;
				OperatorOC = new OperatorController(this, getApplicationContext(), R.id.operatorlayout);
				count = OperatorOC.OperatorCount();
				last = count-((pageNum-1)*5);
				d_OperatorDisplay(this, OperatorOC.ReadOperator(last), last, count);
			}
			break;
			
		default	:
			break;
		}
	}
	
	public void ErrorDisplay(String str) {
		
		errorText.setText(str);
		
		errorPopup.showAtLocation(operatorLayout, Gravity.CENTER, 0, 0);
		errorPopup.setAnimationStyle(0);
	}
	
	public String[] TimeHandling(String time) {
		
		String pTime[] = new String[2];
		int tempTime = Integer.parseInt(time);
		
		if(tempTime > 12) {
			
			tempTime -= 12;
			pTime[0] = "PM";
			
		} else if((0 < tempTime) && (tempTime < 12)) {
			
			pTime[0] = "AM";
		
		} else if(tempTime == 12) {
			
			pTime[0] = "PM";
		
		} else {
			
			tempTime = 12;
			pTime[0] = "AM";
		}
		
		pTime[1] = Integer.toString(tempTime);
		
		return pTime;
	}
	
	public String PasswordHandling(String password) {
		
		String pPassword = password.substring(0, 1);
		
		for(int i = 0; i < (password.length() - 1); i++) {
			
			pPassword = pPassword + "*";
		}
		
		return pPassword;
	}
	
	public void WhichIntent(TargetIntent Itn) { // Activity conversion
		
		switch(Itn) {
		
		case Home		:				
			Intent HomeIntent = new Intent(getApplicationContext(), HomeActivity.class);
			startActivity(HomeIntent);
			finish();
			break;
						
		case Setting	:				
			Intent SettingIntent = new Intent(getApplicationContext(), SettingActivity.class);
			startActivity(SettingIntent);
			finish();
			break;
			
		default		:	
			break;			
		}		
	}
	
	public void finish() {
		
		super.finish();
		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
}