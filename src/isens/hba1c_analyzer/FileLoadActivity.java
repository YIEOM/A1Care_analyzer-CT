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

public class FileLoadActivity extends Activity {

	public DataStorage LoadData;

	final static byte CONTROL = 1,
					  PATIENT = 2;
	
	private TextView Text;
	
	private String fileTestNum[] = new String[5],
				   fileRefNum[] = new String[5],
				   fileHbA1c[] = new String[5],
				   fileDateTime[] = new String[5],
				   filePatientID[] = new String[5];
	
	String filePath = "",
		   loadData;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
//		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.fileload);
		
		Text  = (TextView) findViewById(R.id.text);
		
		StringInit();
		
		FileLoad();
	}
	
	public void StringInit(){
		
		fileTestNum[0] = null;
		fileRefNum[0] = null;
		fileHbA1c[0] = null;
		fileDateTime[0] = null;
		filePatientID[0] = null;
		fileTestNum[1] = null;
		fileRefNum[1] = null;
		fileHbA1c[1] = null;
		fileDateTime[1] = null;
		filePatientID[1] = null;
		fileTestNum[2] = null;
		fileRefNum[2] = null;
		fileHbA1c[2] = null;
		fileDateTime[2] = null;
		filePatientID[2] = null;
		fileTestNum[3] = null;
		fileRefNum[3] = null;
		fileHbA1c[3] = null;
		fileDateTime[3] = null;
		filePatientID[3] = null;
		fileTestNum[4] = null;
		fileRefNum[4] = null;
		fileHbA1c[4] = null;
		fileDateTime[4] = null;
		filePatientID[4] = null;
	}
	
	public void FileLoad() { // loading 10 recently saved data
		
		int dataPage,
			type;
		
		Intent itn = getIntent();
		
		FileSaveActivity.DataCnt = itn.getIntExtra("DataCnt", 0);
		dataPage = itn.getIntExtra("DataPage", 0);
		type = itn.getIntExtra("Type", 0);
		
		LoadData = new DataStorage();
		
		for (int i = 0; i < 5 ; i++) {
		
			filePath = LoadData.FileCheck(dataPage*5 + i + 1, type);
			
			if(filePath != null) { // If file exist
				
				loadData = LoadData.DataLoad(filePath);
				
				fileDateTime [i] = loadData.substring(0, 4) + loadData.substring(4, 6) + loadData.substring(6, 8) + loadData.substring(8, 10) + 
								loadData.substring(10, 12) + loadData.substring(12, 14);
				fileTestNum  [i] = loadData.substring(14, 18);
				fileRefNum   [i] = loadData.substring(18, 23);
				filePatientID[i] = loadData.substring(25, 25 + Integer.parseInt(loadData.substring(23, 25)));
				fileHbA1c    [i] = loadData.substring(25 + Integer.parseInt(loadData.substring(23, 25)));
			}
		}
		
		WhichIntent(type);
	}
	
	public void WhichIntent(int type) { // Activity conversion
	
		switch(type) {
		
		case CONTROL	:
			Intent ControlIntent = new Intent(getApplicationContext(), ControlTestActivity.class);
			ControlIntent.putExtra("DateTime", fileDateTime);
			ControlIntent.putExtra("TestNum", fileTestNum);
			ControlIntent.putExtra("RefNumber", fileRefNum);
			ControlIntent.putExtra("HbA1c", fileHbA1c);
			ControlIntent.putExtra("PatientID", filePatientID);
			startActivity(ControlIntent);
			break;
			
		case PATIENT	:
			Intent PatientIntent = new Intent(getApplicationContext(), PatientTestActivity.class);
			PatientIntent.putExtra("DateTime", fileDateTime);
			PatientIntent.putExtra("TestNum", fileTestNum);
			PatientIntent.putExtra("RefNumber", fileRefNum);
			PatientIntent.putExtra("HbA1c", fileHbA1c);
			PatientIntent.putExtra("PatientID", filePatientID);
			startActivity(PatientIntent);
			break;
			
		default	:
			break;
		}
		
		finish();
	}
	
	public void finish() {
		
		super.finish();
//		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
}