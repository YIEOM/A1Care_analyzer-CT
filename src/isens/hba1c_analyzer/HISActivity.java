package isens.hba1c_analyzer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import isens.hba1c_analyzer.HomeActivity.TargetIntent;
import isens.hba1c_analyzer.TimerDisplay.whichClock;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class HISActivity extends Activity {
	
	private Button sendPIDBtn;
	private Button sendORUBtn;
	private Button cancelBtn;
	
	private TextView patientIDText;
	private TextView nameText;
	private TextView birthText;
	private TextView genderText;
	private TextView ackText;
	
	private String[] Segment_array, Field_array, Name_array, Address_array;
	private String Send_msg, Receive_msg, PatientNum, Send_App, Send_Facility, Receive_App, Receive_Facility, Control_ID, Query_ID, MSH_msg, QRD_msg, OBR_msg, OBX_msg, PID_msg, time_msg;
	private String error_msg;

	private int QRYA01 = 1, ORUR01 = 2;
	private int i_vt = 11;
	private int i_cr = 13;
	private int i_fs = 28;
	private int Num = 0;
	private int Flag_Error = 0;
	private int serverPort = 0;

	private Socket socket;
	private boolean Flag_TCP = false;
	private boolean Flag_Thread = false;
	private boolean Flag_TCP_Start = false;
	
	private String serverIP;
	private String Port;
	private String s_cr = Character.toString((char)i_cr);	//<CR>
	private String s_fs = Character.toString((char)i_fs);	//<FS>
	private String s_vt = Character.toString((char)i_vt);	//<VT>

	Main_Thread thread = new Main_Thread();	// Thread start

	Handler mHandler = new Handler();
	
	public static TextView TimeText;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade, R.anim.hold);
		setContentView(R.layout.his);
		
		TimeText = (TextView)findViewById(R.id.timeText);
		
		patientIDText = (TextView) findViewById(R.id.patientidtext);
		nameText = (TextView) findViewById(R.id.nametext);
		birthText = (TextView) findViewById(R.id.birthtext);
		genderText = (TextView) findViewById(R.id.gendertext);
		ackText = (TextView) findViewById(R.id.ackText);
		
//		sendPIDBtn = (Button)findViewById(R.id.sendpidbtn);
//		sendPIDBtn.setOnClickListener(new View.OnClickListener() {
//		
//			public void onClick(View v) {
//		
//				sendPIDBtn.setEnabled(false);
//
//				if(Flag_TCP)
//				{
//					PatientNum = "11";
//					Send_message(QRYA01);
//				}
//				else{
//					Flag_TCP_Start = true;
//					while(Flag_TCP_Start == true);
//					if(Flag_TCP != true)Toast.makeText(HISActivity.this, "Can't connect to server", Toast.LENGTH_SHORT).show();
//					else
//					{
//						PatientNum = "11";
//						Send_message(QRYA01);
//					}
//				}
//			}
//		});
		
//		sendORUBtn = (Button)findViewById(R.id.sendorubtn);
//		sendORUBtn.setOnClickListener(new View.OnClickListener() {
//		
//			public void onClick(View v) {
//		
//				sendORUBtn.setEnabled(false);
//
//				if(Flag_TCP)
//				{
//					PatientNum = "11";
//					Send_message(ORUR01);
//				}
//				else{
//					Flag_TCP_Start = true;
//					while(Flag_TCP_Start == true);
//					if(Flag_TCP != true)Toast.makeText(HISActivity.this, "Can't connect to server", Toast.LENGTH_SHORT).show();
//					else
//					{
//						PatientNum = "11";
//						Send_message(ORUR01);
//					}
//				}
//			}
//		});
				
		cancelBtn = (Button)findViewById(R.id.cancelbtn);
		cancelBtn.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
		
				cancelBtn.setEnabled(false);
				
				WhichIntent(TargetIntent.Result);
			}
		});
		
		HISInit();
		
	    DBAdapter db = new DBAdapter(this, DBAdapter.SQL_CREATE_MEMBER, "member");	// Call Setting DB
		db.open();

		String columns[] = {"id","Ipaddr","Port","Senda","Sendf","Rcva","Rcvf","Cid","Qid"};
		Cursor cursor = db.selectTable(columns, null, null, null, null, null);
		cursor.moveToFirst();		
		serverIP = cursor.getString(1);
		Port = cursor.getString(2);
		Send_App = cursor.getString(3);
		Send_Facility = cursor.getString(4);
		Receive_App = cursor.getString(5);
		Receive_Facility = cursor.getString(6);
		Control_ID = cursor.getString(7);
		Query_ID = cursor.getString(8);

		serverPort = Integer.parseInt(Port);
		db.close();

		Flag_Thread = true;
	    thread.setDaemon(true);
	    thread.start();		
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
		
		TimerDisplay.timerState = whichClock.HISClock;		
		CurrTimeDisplay();
		
		SerialPort.Sleep(300);
	}
	
	public void connect(){
		try {
			InetSocketAddress socketaddAddress = new InetSocketAddress(serverIP, serverPort);
			socket= new Socket();
			socket.connect(socketaddAddress, 3000);			// socket connect timeout 3sec
			Flag_TCP = true;								//TCP/IP connect success flag

		}catch (Exception e) {
	            Log.e("TCP", "C: Error2", e);
	            Flag_TCP = false;
		}
	}
	
    public static void dumpArray(String[] array) {
        for (int i = 0; i < array.length; i++)
          String.format("array[%d] = %s%n", i, array[i]);
      }

    public void Clear_MSG(){			//EditText Clear
		patientIDText.setText("");
        nameText.setText("");
        birthText.setText("");
        genderText.setText("");
//        tv_Phone1.setText("");
//       tv_Phone2.setText("");
//        tv_Address.setText("");
//        tv_Alram.setText("");
//        ackText.setText("");
        error_msg = "";
    }
    
	public void HL7parsing(String message){	//서버로부터 받은 HL7 메세지를 |과 ^를 구분하여 분석, 메시지 헤더 구분(<VT>), 세그먼트 구분(<CR>), 메시지 구분(<FS>)
		int Flag_MSA = 0, Flag_QRD = 0, Flag_MSH = 0, Flag_ACK_R33 = 0, Flag_ADR_A19 = 0, Flag_ACK = 0, Flag_ERROR = 0;
		Flag_Error = 0;

        Segment_array = message.split(s_cr);	//<cr>을 기준으로 Segment 나눔
        dumpArray(Segment_array);				//나눈 문자열을 Segment_array배열에 삽입
        
        for(int j=0; j<Segment_array.length; j++){
            Field_array = Segment_array[j].split("\\|");	//|을 기준으로 Field 나눔
            dumpArray(Field_array);							//나눈 문자열을 Field_array배열에 삽입
            
// Message 종류 확인 ---------------------------------------------------------------------------------------------------------------------------------------------
            // MSH Segment, 전송한 메시지에 대한 응답인지 등 확인 필요
            if((Field_array[0].equals(s_vt+"MSH")==true)){
            	Flag_MSH = 1;
            	if((Field_array[8].equals("ACK^R33")==true)){
            		Flag_ACK_R33 = 1;
            	}
            	if((Field_array[8].equals("ACK")==true)){
            		Flag_ACK = 1;
            	}
            	else if((Field_array[8].equals("ADR^A19")==true)){
            		Flag_ADR_A19 = 1;
            	}
            }
            
            //MSA Segment, 메시지 승인 확인
            else if((Field_array[0].equals("MSA")==true)){ 											//MSA 검사
            	if((Field_array[1].equals("AA")==true)||(Field_array[1].equals("CA")==true)){		
            		Flag_MSA = 1;	// AA or CA -> 승인됨
            	}
            	else if((Flag_ADR_A19==1 || Flag_ACK==1) && (Field_array[1].equals("AE")==true)||(Field_array[1].equals("CE")==true)){	//ADR^A19와 ACK에서 에러 발생시 -> Error1 발생
            		Clear_MSG();
            		Flag_ACK = 0;
            		Flag_ADR_A19 = 0;
            		Flag_MSA = 0;
            		error_msg = Field_array[2];// 전송된 데이터에 해당 란이 비어있을 경우(존재하지 않을 경우) 오류 발생 가능성 있음!!! -> Field_array[2]의 존재 유무를 먼저 확인 한 후 실행 할 것!!!
                    Flag_Error = 1;
            	}
            	else if(Flag_ACK_R33==1 && (Field_array[1].equals("AE")==true)||(Field_array[1].equals("CE")==true)){	//ACK^R33 에러 발생시 -> Error2 발생
            		Flag_ACK_R33 = 0;
            		Flag_MSA = 0;
            		error_msg = Field_array[2];// 전송된 데이터에 해당 란이 비어있을 경우(존재하지 않을 경우) 오류 발생 가능성 있음!!! -> Field_array[2]의 존재 유무를 먼저 확인 한 후 실행 할 것!!!
                    Flag_Error = 2;
            	}
            }

            //ADR^A19 Message로 확인 될 경우
            //QRD Segment, QRY^A19로 전송했던 QRD 세그먼트와 동일한지 확인해야 함
            else if(Flag_ADR_A19 == 1){
            	if((Field_array[0].equals("QRD")==true) && (Field_array[1].equals(time_msg)==true) && (Field_array[2].equals("R")==true) && 
            	   (Field_array[3].equals("I")==true) && (Field_array[4].equals(Query_ID)==true) && (Field_array[7].equals("1^RD")==true) && 
            	   (Field_array[8].equals(PatientNum)==true) && (Field_array[9].equals("DEM")==true)){
            		Flag_ADR_A19 = 0;
           			Flag_QRD = 1;
            	}
	            else{
	           		//요청한 QRY에 대한 응답이 아니므로 에러 메시지 출력
	            	break;
	            }
            }
            
            //ACK^R33 Message로 확인 될 경우           
            else if(Flag_ACK_R33==1 && Flag_MSA==1){
            	// 처리내용
            	// ORC확인으로 Order 유무, Order 생성시 승인 여부, 에러 메시지 확인해야 함 (ORU^R30, R31, R32)
        		Flag_ADR_A19 = 0;
        		Flag_MSA = 0;            }
// Message 종류 확인 끝 ---------------------------------------------------------------------------------------------------------------------------------------------            

//ACK Message에 대한 Parsing --------------------------------------------------------------------------------------------------------------------------------------            
            if( Flag_ACK==1 && Flag_MSA==1 ){
            	ackText.setText("Transmit Succsess");
            	Flag_ACK = 0;
        		Flag_MSA = 0;
            }
            
//ACK^R33 Message에 대한 Parsing ----------------------------------------------------------------------------------------------------------------------------------
            if( Flag_ACK_R33==1 && Flag_MSA==1 ){
            	ackText.setText("Transmit Succsess");
            	Flag_ACK_R33 = 0;
        		Flag_MSA = 0;
            }
            //ORU^R30
            //ORU^R31
            //ORU^R32
//ADR^A19 Message에 대한 Parsing ----------------------------------------------------------------------------------------------------------------------------------
            //PID Segment, 환자 개인정보
            else if((Field_array[0].equals("PID")==true) && (Flag_QRD == 1)){
	            //Message_array[0] = PID									//"PID"는 Segment의 가장 첫위치에 명시되므로 확인 후 없으면 break
	            //Message_array[3] = Patient Number
	            //Message_array[5] = Name
	            //Message_array[7] = Birth
	            //Message_array[8] = Gender
	            //Message_array[11] = Address
	            //Message_array[13] = Phone1
	            //Message_array[14] = Phone2
            	
            	Clear_MSG();
            	Flag_MSH = 0;
            	Flag_MSA = 0;
            	Flag_QRD = 0;
		        
		        Name_array = Field_array[5].split("\\^");		//이름과 주소는 ^를 기준으로 한번더 나눔
		        dumpArray(Name_array);
		        //Name_array[0] = Familly name
		        //Name_array[1] = Given name
		        //Name_array[2] = middle initial or name
		        
		        Address_array = Field_array[11].split("\\^");
		        dumpArray(Address_array);
		        //Address_array[0] = Street address
		        //Address_array[1] = 2nd street address
		        //Address_array[2] = city
		        //Address_array[3] = state
		        //Address_array[4] = zip/postal code
		        //Address_array[5] = contry
	
	            patientIDText.setText(Field_array[3]);
	            nameText.setText(Name_array[0]+Name_array[1]);	//Name과 Address의 경우 배열 갯수만큼 정리해서 출력가능하도록 수정해야 한다-> 애러발생..!!
	            birthText.setText(Field_array[7]);
	            genderText.setText(Field_array[8]);
//	            tv_Phone1.setText(Field_array[13]);
//	            tv_Phone2.setText(Field_array[14]);
	            //tv_Address.setText(Address_array[0]+Address_array[1]+Address_array[2]+Address_array[3]+Address_array[4]+Address_array[5]);

	            String Name = Name_array[0];
	            for(int name_i = 1; name_i < Name_array.length;name_i++){
		            Name += Name_array[name_i];					//Name과 Address의 경우 배열 갯수만큼 정리해서 출력가능하도록 수정해야 한다-> 애러발생..!!
	            }
	            nameText.setText(Name);

	            String Address = Address_array[0];
	            for(int address_i = 1; address_i < Address_array.length;address_i++){
		            Address += Address_array[address_i];		//Name과 Address의 경우 배열 갯수만큼 정리해서 출력가능하도록 수정해야 한다-> 애러발생..!!
	            }

	            break;
            }
//ADR^A19 Message 끝 ---------------------------------------------------------------------------------------------------------------------------------------
            
            else{
            	//ADT^A19, ACK, ACK^R33 이 아닌 다른 메시지가 들어올 경우 혹은 오류 발생시
            }
        }
        check_error_msg();	//메시지 에러 체크 (MSA -> AE, CE)
    }

	public void check_error_msg()
	{
		if(Flag_Error == 1)
		{
			AlertDialog.Builder fn = new AlertDialog.Builder(this);
			fn.setMessage(error_msg);
			fn.setPositiveButton("OK",null);
			fn.show();
			Flag_Error = 0;
		}
		if(Flag_Error == 2)
		{
			AlertDialog.Builder fn = new AlertDialog.Builder(this);
			fn.setMessage(error_msg);
			fn.setPositiveButton("OK",null);
			fn.show();
			Flag_Error = 0;
		}
	}
	
	
//	public void Send_message(int mode){
//		time_msg = TimerDisplay.rTime[0]+TimerDisplay.rTime[1]+TimerDisplay.rTime[2]+TimerDisplay.rTime[7]+TimerDisplay.rTime[4]+TimerDisplay.rTime[6];
//		if(mode == 1)	//QRY^A19
//		{
//			MSH_msg = ("MSH|^~\\&|"+ Send_App +"|"+ Send_Facility +"|"+ Receive_App +"|"+ Receive_Facility +"|"+time_msg+"||QRY^A19|"+Control_ID+"|P|2.4");
//			QRD_msg = ("QRD|"+time_msg+"|R|I|"+Query_ID+"|||1^RD|"+ PatientNum +"|DEM|||");
//			Send_msg = (MSH_msg + s_cr + QRD_msg + s_cr);			// 메시지 헤더 vt삽입, 각 세그먼트 뒤 cr삽입, 메시지 뒤 fs + cr 삽입  -> vt + 메시지(세그먼트 cr + 세그먼트 cr...) + fs + cr
//		}
//		if(mode == 2)	//ORU^R01
//		{
//			MSH_msg = ("MSH|^~\\&|"+ Send_App +"|"+ Send_Facility +"|"+ Receive_App +"|"+ Receive_Facility +"|"+time_msg+"||ORU^R01|"+Control_ID+"|P|2.4");
//			PID_msg = Segment_array[3];
//			OBR_msg = ("OBR|1|||41995-2^Hemoglobin A1c^LN|||"+time_msg+"||||||||||||||||||F");
//			OBX_msg = ("OBX|1|ST|41995-2^Hemoglobin A1c^LN||"+RunActivity.HbA1cPctStr+"|%|||||F");		//type ST = string data, NM = Numeric
//			Send_msg = (MSH_msg + s_cr + PID_msg + s_cr + OBR_msg + s_cr + OBX_msg + s_cr);		// 메시지 헤더 vt삽입, 각 세그먼트 뒤 cr삽입, 메시지 뒤 fs + cr 삽입  -> vt + 메시지(세그먼트 cr + 세그먼트 cr...) + fs + cr
//		}
//		try	{
//			BufferedWriter out = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream(),"EUC-KR"));	// *중요* 서버가 euc-kr을 사용할 경우 인코딩 해서 전송한다!!
//			out.write(s_vt);
////			out.flush();
//			out.write(Send_msg);
////			out.flush();
//			out.write(s_fs+s_cr);
//			out.flush();
//		} catch (Exception e){
//			Log.e("TCP", "C: Send Error", e);
//		} 
//	}
	
	public void Stop(){		// Thread와 Socket을 멈춤
		try {
			if(Flag_TCP){
				Flag_TCP = false;					//Stop thread
				Flag_Thread = false;
				socket.close();		//Stop TCP IP
			}
			else{
				Flag_Thread = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		Stop();
	}
		
	public class Main_Thread extends Thread{	//Receive Message
		@Override
		public void run() {
    		InputStream in;
			while (Flag_Thread) {		// Thread Flag, isrunning은 초기값이 1이므로 Main class진입 순간부터 바로 Thread 동작
				
				if(Flag_TCP){
					try	{
			    		in = socket.getInputStream();
			    		byte[] buf = new byte[1024];
		    			int size = in.read(buf);
		    			Receive_msg = new String(buf,0,size);
	                    mHandler.post(new Runnable() {
						
	                        public void run() {      
	                           HL7parsing(Receive_msg);	//HL7 프로토콜 이외의 문자가 넘어올 경우 에러가 발생한다.
	                        }
	                     });
					} catch(Exception e) {
						try {
							socket.close();
							Log.e("TCP", "C: Rcv Error", e);
							Flag_TCP = false;
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
				if(Flag_TCP_Start){
					connect();
					Flag_TCP_Start = false;
                    mHandler.post(new Runnable() {
						
                        public void run() {      
            			//	if(Flag_TCP != true) Toast.makeText(this, "Can't connect to server", Toast.LENGTH_SHORT).show();
                        }
                    });
				}
			}
		}
	}
	
	public void WhichIntent(TargetIntent Itn) { // Activity conversion
		
		switch(Itn) {
		
		case Result	:	
			Intent ResultIntent = new Intent(getApplicationContext(), ResultActivity.class);
			startActivity(ResultIntent);
			break;
						
		default		:	
			break;			
		}
		
		finish();
	}
	
	public void finish() {
		
		super.finish();
		overridePendingTransition(R.anim.fade, R.anim.hold);
	}
}
