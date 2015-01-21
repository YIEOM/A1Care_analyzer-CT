package isens.hba1c_analyzer;

import java.util.ResourceBundle;

import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class OperatorController {
	
	public DatabaseHander OCDataBase;
	public OperatorSettingActivity OCOperatorSetting;
	public ErrorPopup OCErrorPopup;
	
	public Activity activity;
	public Context context;
	public int layoutid;
	
	public View popupView;
	public PopupWindow popupWindow;
	public RelativeLayout hostLayout;
	
	public EditText oIDEText,
	 				passEText;

	public Button loginBtn,
		   		  checkBtn;
	
	public boolean btnState = false;
	
	public OperatorController(Activity activity, Context context, int layoutid) {
		
		this.activity = activity;
		this.context = context;
		this.layoutid = layoutid;
	}
	
	public void LoginDisplay() {
		
		hostLayout = (RelativeLayout)activity.findViewById(layoutid);
		popupView = View.inflate(context, R.layout.loginpopup, null);
		popupWindow = new PopupWindow(popupView, 800, 480, true);

		oIDEText = (EditText) popupView.findViewById(R.id.loginoid);
		passEText = (EditText) popupView.findViewById(R.id.loginpass);
		
		loginBtn = (Button)popupView.findViewById(R.id.loginbtn);
		loginBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
			
				if(!btnState) {
					
					btnState = true;
					
					LoginCheck();
				}
			}
		});
		
		checkBtn = (Button)popupView.findViewById(R.id.checkbtn);
		checkBtn.setOnClickListener(new View.OnClickListener() { 
			
			public void onClick(View v) {
	
				if(!btnState) {
					
					btnState = true;
				
					CheckBoxDisplay(checkBtn);
					
					btnState = false;
				}
			}
		});
		
		hostLayout.post(new Runnable() {
			public void run() {
		
				popupWindow.showAtLocation(hostLayout, Gravity.CENTER, 0, 0);
				popupWindow.setAnimationStyle(0);
				AutoWriteLogin();
			}
		});
	}
	
	public void LoginCheck() {
			
		int Count;
		
		String id = oIDEText.getText().toString(), 
			   password = passEText.getText().toString();
		
		if(id.equals("")) {
			
			popupWindow.dismiss();
			
		} else {
		
			OCDataBase = new DatabaseHander(context);
			OCDataBase.getWritableDatabase();
			
			if(OCDataBase.CheckIDDuplication(id)) {
				password = OCDataBase.GetPassword(id);
				
				Log.w("Login Check", "password : " + password);
				
				if(passEText.getText().toString().equals(password)) {
					
					popupWindow.dismiss();
					
					CheckFlagSave();
					
					OCDataBase.UpdateLastLogIn(id);
					
					if(TimerDisplay.timerState == whichClock.OperatorSettingClock) {
						
						OCOperatorSetting = new OperatorSettingActivity();
						Count = OperatorCount();
						OCOperatorSetting.d_OperatorDisplay(activity, ReadOperator(Count), Count, Count);
					}
					
				} else {
							
					OCErrorPopup = new ErrorPopup(activity, context, layoutid);
					OCErrorPopup.ErrorBtnDisplay(R.string.w006);
				}
			} else {
				
				OCErrorPopup = new ErrorPopup(activity, context, layoutid);
				OCErrorPopup.ErrorBtnDisplay(R.string.w005);
			}
		}
	
		btnState = false;
	}
	
	public void AutoWriteLogin() {
		
		String id,
			   password;
		
		OCDataBase = new DatabaseHander(context);
			
		if(HomeActivity.CheckFlag) {
		
			checkBtn.setBackgroundResource(R.drawable.login_checkbox_check);
		
			id = OCDataBase.GetLastLoginID();
			password = OCDataBase.GetPassword(id);
			
			oIDEText.setText(id);
			passEText.setText(password);
			
		} else {
			
			checkBtn.setBackgroundResource(R.drawable.login_checkbox);
		}
	}
	
	public void CheckBoxDisplay(Button btn) {
		
		if(HomeActivity.CheckFlag) {
			
			HomeActivity.CheckFlag = false;
			btn.setBackgroundResource(R.drawable.login_checkbox);
			
		} else {
			
			HomeActivity.CheckFlag = true;
			btn.setBackgroundResource(R.drawable.login_checkbox_check);
		}
	}
	
	public String[][] ReadOperator(int last) {
		
		int first,
			dataCnt;
		
		String tempDate,
			   tempPassword,
			   formDate,
			   formPassword;
		
		String tempHour[] = new String[2],
			   rowData[] = new String[4];
		
		String Operator[][] = new String[4][5];
		
		OCDataBase = new DatabaseHander(context);
		
		if(last > 5) {
			
			first = last - 5;
			dataCnt = 5;
		
		} else {
			
			first = 0;
			dataCnt = last;
		}
		
		Log.w("Operator display", "first : " + first + " last : " + last);
		
		for(int i = 0; i < dataCnt; i++) {
			
			rowData = OCDataBase.GetRowWithNumber(last - (i + 1));
				
			tempDate = rowData[1];
			tempPassword = rowData[2];
			
			tempHour = TimeHandling(tempDate.substring(8, 10));
			
			formDate = tempDate.substring(0, 4) + "." + tempDate.substring(4, 6) + "." + tempDate.substring(6, 8) + " " + tempHour[0] + " " + tempHour[1] + ":" + tempDate.substring(10, 12);
			formPassword = PasswordHandling(tempPassword);	
			
			Operator[0][i] = rowData[0];
			Operator[1][i] = formDate;
			Operator[2][i] = formPassword;
			Operator[3][i] = rowData[3];
		}
		
		return Operator;
	}
	
	public int OperatorCount() {
		
		int count;
		
		OCDataBase = new DatabaseHander(context);
		count = OCDataBase.GetRowCount();
		
		Log.w("Get operator", "the number : " + count);
		
		return count;
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

	public void CheckFlagSave() { // Saving number of user define parameter
		
		SharedPreferences loginPref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor loginedit = loginPref.edit();
		
		loginedit.putBoolean("Check Box", HomeActivity.CheckFlag);
		loginedit.commit();
	}
}
