package isens.hba1c_analyzer;

import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class HISSettingActivity extends Activity {
	
	private Button saveBtn;
	private Button backIcon;
	
	private EditText ipaddrEText, portEText, sendAppEText, sendFacilityEText, receiveAppEText, receiveFacilityEText, controlIDEText, queryIDEText;
	private String ipaddrStr, portStr, sendAppStr, sendFacilityStr, receiveAppStr, receiveFaclilityStr, controlIDStr, queryIDStr;
		
	public static TextView TimeText;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.hissetting);
		
		TimeText = (TextView)findViewById(R.id.timeText);
		
		ipaddrEText = (EditText) findViewById(R.id.ipaddretext);
		portEText = (EditText) findViewById(R.id.portetext);
		sendAppEText = (EditText) findViewById(R.id.sendappetext);
		sendFacilityEText = (EditText) findViewById(R.id.sendfacilityetext);
		receiveAppEText = (EditText) findViewById(R.id.receiveappetext);
		receiveFacilityEText = (EditText) findViewById(R.id.receivefacilityetext);
		controlIDEText = (EditText) findViewById(R.id.controlidetext);
		queryIDEText = (EditText) findViewById(R.id.queryidetext);
		
		backIcon = (Button)findViewById(R.id.backicon);
		backIcon.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				backIcon.setEnabled(false);
				
				Save();
				
				Intent SettingIntent = new Intent(getApplicationContext(), SystemSettingActivity.class);
				startActivity(SettingIntent);
				finish();
			}
		});
		
		HISInit();
	}
	
	public void Load() {
		
		DBAdapter db = new DBAdapter(this, DBAdapter.SQL_CREATE_MEMBER, "member");
	    db.open();
	    
		String columns[] = {"id","Ipaddr","Port","Senda","Sendf","Rcva","Rcvf","Cid","Qid"};
		Cursor cursor = db.selectTable(columns, null, null, null, null, null);

		while(cursor.moveToNext()){
			
			String id = cursor.getString(0);
			
			if(id.compareTo("1")==0){
				
				ipaddrEText.setText(cursor.getString(1));
				portEText.setText(cursor.getString(2));
				sendAppEText.setText(cursor.getString(3));
				sendFacilityEText.setText(cursor.getString(4));
				receiveAppEText.setText(cursor.getString(5));
				receiveFacilityEText.setText(cursor.getString(6));
				controlIDEText.setText(cursor.getString(7));
				queryIDEText.setText(cursor.getString(8));
			}
		}
		
		db.close();
	}
	
	public void Save(){
		
		ipaddrStr           = ipaddrEText.getText().toString();
		portStr             = portEText.getText().toString();
		sendAppStr          = sendAppEText.getText().toString();
		sendFacilityStr     = sendFacilityEText.getText().toString();
		receiveAppStr       = receiveAppEText.getText().toString();
		receiveFaclilityStr	= receiveFacilityEText.getText().toString();
		controlIDStr        = controlIDEText.getText().toString();
		queryIDStr          = queryIDEText.getText().toString();

		DBAdapter db = new DBAdapter(this, DBAdapter.SQL_CREATE_MEMBER, "member");
	    db.open();
	    
		ContentValues value = new ContentValues();
		value.put("Ipaddr", ipaddrStr);
		value.put("Port", portStr);
		value.put("Senda", sendAppStr);
		value.put("Sendf", sendFacilityStr);
		value.put("Rcva", receiveAppStr);
		value.put("Rcvf", receiveFaclilityStr);
		value.put("Cid", controlIDStr);
		value.put("Qid", queryIDStr);
		
		db.deleteTable();
		db.insertTable(value);
		db.close();
		Toast.makeText(this, "Save complete", Toast.LENGTH_SHORT).show();
		
		saveBtn.setEnabled(true);
	}
	
	public void CurrTimeDisplay() {
		
		new Thread(new Runnable() {
		    public void run() {    
		        runOnUiThread(new Runnable(){
		            public void run() {
		            	
//		            	Log.w("SettingTimeDisplay", "run");
		            	TimeText.setText(TimerDisplay.rTime[3] + " " + TimerDisplay.rTime[4] + ":" + TimerDisplay.rTime[5]);
		            }
		        });
		    }
		}).start();	
	}
	
	private void HISInit() {
		
		TimerDisplay.timerState = whichClock.HISSettingClock;		
		CurrTimeDisplay();
		
		Load();
		
		SerialPort.Sleep(300);
	}
	
	public void finish() {
		
		super.finish();
		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
}