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
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EthernetControl {

	public EthernetActivity vEthernet;
	
	public static TextView TimeText;
	public static ImageView deviceImage;
	
	public Button escBtn;
	
	public EditText ipEText,
					netmaskEText,
					gateEText,
					serverEText,
					dns1EText,
					dns2EText;
	
	public Context context;
	public Activity activity;
	
	EthernetControl(Context context, Activity activity) {
		
		this.context = context;
		this.activity = activity;
	}
	
	public void EthernetUp() {
		
		SetEthernet(LoadEthernet());
	}
	
	public void SetEthernet(String[] parm) {
		
		try {

            String line;

            Process p ;
            
            Log.w("Set Ethernet","parm: " + parm[0].toString());
            
            Runtime.getRuntime().exec("ssu -c busybox ./ifconfig eth0 down").waitFor();
            Runtime.getRuntime().exec("ssu -c busybox ./ifconfig eth0 " + parm[0] + " netmask " + parm[1] + " up").waitFor();
            Runtime.getRuntime().exec("ssu -c busybox route add default gw " + parm[2] + " dev eth0").waitFor();
            Runtime.getRuntime().exec("setprop net.dns1 " + parm[4]).waitFor();
            Runtime.getRuntime().exec("setprop net.dns2 " + parm[5]).waitFor();
            
//            BufferedReader input = new BufferedReader (new InputStreamReader(p.getInputStream()));   
//	           
//            while ((line = input.readLine()) != null) {   
//
//            	Log.w("OLE","line : "+line);
//  	        }   
//            
//            input.close();
            
        } catch (IOException e) {
            Log.e("OLE","Runtime Error: "+e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String[] LoadEthernet() {
		
		String[] parm = new String[6];
		
		SharedPreferences EthPref = PreferenceManager.getDefaultSharedPreferences(context);
		parm[0] = EthPref.getString("IP Adress",   "0.0.0.0");
		parm[1] = EthPref.getString("Subnet mask", "0.0.0.0");
		parm[2] = EthPref.getString("Gateway",     "0.0.0.0");
		parm[3] = EthPref.getString("Server IP",   "0.0.0.0");
		parm[4] = EthPref.getString("DNS 1",       "0.0.0.0");
		parm[5] = EthPref.getString("DNS 2",       "0.0.0.0");
		
		return parm;
	}
	
	public void SaveEthernet(String[] parm) {
		
		SetEthernet(parm);
	
		SharedPreferences EthPref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor Ethedit = EthPref.edit();
		
		Ethedit.putString("IP Adress",   parm[0]);
		Ethedit.putString("Subnet mask", parm[1]);
		Ethedit.putString("Gateway",     parm[2]);
		Ethedit.putString("Server IP",   parm[3]);
		Ethedit.putString("DNS 1",       parm[4]);
		Ethedit.putString("DNS 2",       parm[5]);
		Ethedit.commit();
	}
}
