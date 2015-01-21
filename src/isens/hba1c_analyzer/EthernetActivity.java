package isens.hba1c_analyzer;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EthernetActivity extends Activity {

	public EthernetControl cEthernet;
	
	public static TextView TimeText;
	public static ImageView deviceImage;
	
	public RelativeLayout networkLayout;
	
	public Button escBtn;
	
	public EditText ipEText,
					netmaskEText,
					gateEText,
					serverEText,
					dns1EText,
					dns2EText;
	
	public String[] ethParm = new String[6];
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.network);

		networkLayout = (RelativeLayout) findViewById(R.id.networklayout);		
		
		TimeText = (TextView) findViewById(R.id.timeText);
		deviceImage = (ImageView) findViewById(R.id.device);
		
		cEthernet = new EthernetControl(this, this);
		
		/*System setting Activity activation*/
		escBtn = (Button)findViewById(R.id.escicon);
		escBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				escBtn.setEnabled(false);
				
				cEthernet.SaveEthernet(GetEthernet());
				
				WhichIntent(TargetIntent.Test);
			}
		});
		
		Init();
	}
	
	public void Init() {
		
		EthernetText(this);
		
		ethParm = cEthernet.LoadEthernet();
		
		ipEText.setText(ethParm[0]);
		netmaskEText.setText(ethParm[1]);
		gateEText.setText(ethParm[2]);
		serverEText.setText(ethParm[3]);
		dns1EText.setText(ethParm[4]);
		dns2EText.setText(ethParm[5]);
	}
	
	public void EthernetText(Activity activity) {
		
		ipEText      = (EditText) activity.findViewById(R.id.ipetext);
		netmaskEText = (EditText) activity.findViewById(R.id.netmasketext);
		gateEText    = (EditText) activity.findViewById(R.id.gateetext);
		serverEText  = (EditText) activity.findViewById(R.id.serveretext);
		dns1EText    = (EditText) activity.findViewById(R.id.dns1etext);
		dns2EText    = (EditText) activity.findViewById(R.id.dns2etext);
	}
	
	public String[] GetEthernet() {

		String[] parm = new String[6];
		
    	parm[0] = ipEText.getText().toString();
		parm[1] = netmaskEText.getText().toString();
		parm[2] = gateEText.getText().toString();
		parm[3] = serverEText.getText().toString();
		parm[4] = dns1EText.getText().toString();
		parm[5] = dns2EText.getText().toString();
		        
		Log.w("get Ethernet","ip : " + parm[0]);
		
		return parm;
	}
	
	public void WhichIntent(TargetIntent Itn) { // Activity conversion
		
		switch(Itn) {
		
		case Test	:
			Intent TestIntent = new Intent(getApplicationContext(), TestActivity.class);
			startActivity(TestIntent);
			break;
			
		default		:	
			break;			
		}
		
		finish();
	}
}
