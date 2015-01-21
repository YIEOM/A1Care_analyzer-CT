package isens.hba1c_analyzer;

import java.text.DecimalFormat;
import java.util.Calendar;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FileSaveActivity extends Activity {

	public static byte NORMAL_RESULT = 0,
					   CONTROL_TEST = 1,
					   PATIENT_TEST = 2;
	
	private DataStorage SaveData;
	
	private TextView Text;
	private Intent itn;
	
	private StringBuffer overallData = new StringBuffer(),
						 historyData = new StringBuffer();

	public static int DataCnt,
					  TempDataCnt;
	private int runState,
				whichState;
	private String dataType;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
//		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.filesave);
		
		Text = (TextView) findViewById(R.id.text);		
		
		DataInit();
	}
	
	public void DataInit() {
		
		DataArray();
		
		SaveData = new DataStorage();
		
		itn = getIntent();
		
		if(itn.getIntExtra("RunState", 0) == (int) NORMAL_RESULT) {

			if(dataType.equals("B")) {
				
				SaveData.DataSave(CONTROL_TEST, overallData);

			} else if(dataType.equals("D")) {
				
				SaveData.DataSave(PATIENT_TEST, overallData); // if HbA1c test is normal, the Result data is saved
			
			} else if(dataType.equals("E")) {
				
				SaveData.DataSave(PATIENT_TEST, overallData); // if HbA1c test is normal, the Result data is saved
			
			} else if(dataType.equals("F")) {
				
				SaveData.DataSave(PATIENT_TEST, overallData); // if HbA1c test is normal, the Result data is saved
			}
		}
		
		SaveData.DataHistorySave(overallData, historyData); // the History data is saved
		
		WhichIntent();
	}
	
	public void DataArray() { // Enumerating data
		
		DecimalFormat dfm = new DecimalFormat("0000");

		overallData.delete(0, overallData.capacity());
		historyData.delete(0, historyData.capacity());
		
		itn = getIntent();
		whichState = itn.getIntExtra("WhichIntent", 0);
		DataCnt = itn.getIntExtra("DataCnt", 0);
		TempDataCnt = DataCnt % 9999;
		if(TempDataCnt == 0) TempDataCnt = 9999;		
		
		if((whichState != 4) && (whichState != 5)) {
			
			dataType = itn.getStringExtra("RefNumber").substring(0, 1);
	
		} else dataType = "B";
		
		overallData.append(itn.getStringExtra("Year"));
		overallData.append(itn.getStringExtra("Month"));
		overallData.append(itn.getStringExtra("Day"));
		overallData.append(itn.getStringExtra("AmPm"));
		overallData.append(itn.getStringExtra("Hour"));
		overallData.append(itn.getStringExtra("Minute"));
		overallData.append(dfm.format(TempDataCnt));
		overallData.append(itn.getStringExtra("RefNumber"));
		overallData.append(itn.getStringExtra("PatientIDLen"));
		overallData.append(itn.getStringExtra("PatientID"));
		overallData.append(itn.getStringExtra("Hba1cPct"));
		
		historyData.append(itn.getIntExtra("RunMin", 0) + "\t");
		historyData.append(itn.getIntExtra("RunSec", 0) + "\t");
		historyData.append(itn.getStringExtra("BlankVal0") + "\t");
		historyData.append(itn.getStringExtra("BlankVal1") + "\t");
		historyData.append(itn.getStringExtra("BlankVal2") + "\t");
		historyData.append(itn.getStringExtra("BlankVal3") + "\t");
		historyData.append(itn.getStringExtra("St1Abs1by0") + "\t");
		historyData.append(itn.getStringExtra("St1Abs1by1") + "\t");
		historyData.append(itn.getStringExtra("St1Abs1by2") + "\t");
		historyData.append(itn.getStringExtra("St1Abs2by0") + "\t");
		historyData.append(itn.getStringExtra("St1Abs2by1") + "\t");
		historyData.append(itn.getStringExtra("St1Abs2by2") + "\t");
		historyData.append(itn.getStringExtra("St1Abs3by0") + "\t");
		historyData.append(itn.getStringExtra("St1Abs3by1") + "\t");
		historyData.append(itn.getStringExtra("St1Abs3by2") + "\t");
		historyData.append(itn.getStringExtra("St2Abs1by0") + "\t");
		historyData.append(itn.getStringExtra("St2Abs1by1") + "\t");
		historyData.append(itn.getStringExtra("St2Abs1by2") + "\t");
		historyData.append(itn.getStringExtra("St2Abs2by0") + "\t");
		historyData.append(itn.getStringExtra("St2Abs2by1") + "\t");
		historyData.append(itn.getStringExtra("St2Abs2by2") + "\t");
		historyData.append(itn.getStringExtra("St2Abs3by0") + "\t");
		historyData.append(itn.getStringExtra("St2Abs3by1") + "\t");
		historyData.append(itn.getStringExtra("St2Abs3by2"));
	}
	
	public void WhichIntent() { // Activity conversion
	
		Intent RemoveIntent = new Intent(getApplicationContext(), RemoveActivity.class);
		RemoveIntent.putExtra("WhichIntent", whichState);
		RemoveIntent.putExtra("DataCnt", DataCnt);
		startActivity(RemoveIntent);
		finish();
	}
	
	public void finish() {
		
		super.finish();
//		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
}
