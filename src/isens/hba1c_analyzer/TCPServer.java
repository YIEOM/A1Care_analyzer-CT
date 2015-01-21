package isens.hba1c_analyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;
import android.widget.RelativeLayout;

public class TCPServer {

	final static int SERVER_PORT = 9010;
	
	public BarPriLanActivity mBarPriLanActivity;
	public EthernetControl cEthernet;	
	
	boolean isNormal = true;
	
	public void onCreate() {
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}
	
	public String GetDeviceIP(Activity activity, RelativeLayout layout) {
		
		try {
		
			String ip;
			
			Log.w("GetDeviceIP", "run");
			
			for (Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces(); enumeration.hasMoreElements();) {
				
				NetworkInterface networkInterface = enumeration.nextElement();
				
				for (Enumeration<InetAddress> enumerationIpAddr = networkInterface.getInetAddresses(); enumerationIpAddr.hasMoreElements();) {
					
					InetAddress inetAddress = enumerationIpAddr.nextElement();
					
					if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
		
						ip = inetAddress.getHostAddress();
						
						TCPAndroidServer mTCPAndroidServer = new TCPAndroidServer(activity, layout);
						mTCPAndroidServer.start();
						
						return ip;
					}
				}
			}
			
		} catch (SocketException e) {
				
			Log.e("ERROR:", e.toString());
		}
		
		return null;
	}
	
	public class TCPAndroidServer extends Thread {
		
		Activity activity;
		RelativeLayout layout;
		
		TCPAndroidServer(Activity activity, RelativeLayout layout) {
			
			this.activity = activity;
			this.layout = layout;
		}
				
		public void run() {
			
			try {

				ServerSocket mServerSocket = new ServerSocket(SERVER_PORT);
				
				Log.w("TCP android server", "run");
				
				while(isNormal) {
					
					Socket client = mServerSocket.accept();
					
					try {

						BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
						final String str = in.readLine();
						
						Log.w("TCP android server", "receive : " + str);
					
						layout.post(new Runnable() {
							public void run() {
								
								mBarPriLanActivity = new BarPriLanActivity();
								mBarPriLanActivity.BPLDisplay(activity, Barcode.Barcode, str);		
							}
						});
						
					} catch(Exception e) {
						
					} finally {
						
						client.close();
					}
				}
			
			} catch (Exception e) {
				
			}
		}
	}
	
	public class TCPClient extends Thread {
		
		Context context;
		Activity activity;
		
		TCPClient(Context context, Activity activity) {
			
			this.context = context;
			this.activity = activity;
		}
		
		public void run() {
		
			cEthernet = new EthernetControl(context, activity);
			
			String[] serverIP = cEthernet.LoadEthernet();
			
			String str = null;
			
			try {
				
				Socket socket = new Socket(serverIP[3], SERVER_PORT);
				
				try {
					
					while(isNormal) {
					
						str = TimerDisplay.rTime[3] + " " + TimerDisplay.rTime[4] + ":" + TimerDisplay.rTime[5] + ":" + TimerDisplay.rTime[6];
						
						PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
						out.println(str);
						
						Log.w("TCP client", "send : " + str);
						
						SerialPort.Sleep(250);
					}
					
				} catch(Exception e) {
					
				} finally {
					
					socket.close();
				}
				
				socket.close();
		
			} catch(Exception e) {
				
			}
		}
	}
	
	public void DataTransmit(Activity activity, Context context) {
		
		TCPClient mTCPClient = new TCPClient(context, activity);
		mTCPClient.start();
	}
	
	public void TCPClose() {
		
		isNormal = false;
	}
}
