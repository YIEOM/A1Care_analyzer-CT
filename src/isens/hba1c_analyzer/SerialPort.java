package isens.hba1c_analyzer;

import isens.hba1c_analyzer.TimerDisplay.whichClock;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class SerialPort {
	
	public Barcode SerialBarcode;
	public ResultActivity SerialResult;
	public BarPriLanActivity mBarPriLanActivity;
	
	/* Board Serial set-up */
	private static FileDescriptor BoardFd;
	public static FileInputStream BoardFileInputStream;
	private FileOutputStream BoardFileOutputStream;
	
	private BoardTxThread mBoardTxThread;
	private static BoardRxThread mBoardRxThread;
	
	/* Printer Serial set-up */
	private static FileDescriptor pFd;
	private FileOutputStream pFileOutputStream;
	
	private PrinterTxThread pPrinterTxThread;
	
	/* Barcode Serial set-up */
	public static FileDescriptor BarcodeFd;
	public static FileInputStream BarcodeFileInputStream;
	private FileOutputStream BarcodeFileOutputStream;
	public static BarcodeRxThread bBarcodeRxThread;
	
	public FileDescriptor mFd;
	public static FileInputStream HHBarcodeFileInputStream;
	public static HHBarcodeRxThread hBarcodeRxThread;
	
	
	public enum CtrTarget {PhotoSet, TmpSet, TmpCall, MotorSet, AmbientTmpCall, CartCall, DoorCall, MotorStop}
	public enum RxTarget {Board, Barcode}
	
	final static byte STX = 0x02,
					  ETX = 0x03,
					  LF  = 0x0A,
					  CR  = 0x0D,
					  ESC = 0x1B,
					  GS  = 0x1D,
					  PRINTRESULT = 1,
					  PRINTRECORD = 2;
	
	final static int UART_RX_MASK = 128;
	final static byte AMB_MSG_RX_MASK = 4,
					  BOARD_INPUT_MASK = 8,
					  BOARD_INPUT_BUFFER = 8;
	
	public static byte BoardInputBuffer[] = new byte[BOARD_INPUT_BUFFER],
					   BoardRxBuffer[][] = new byte[BOARD_INPUT_MASK][BOARD_INPUT_BUFFER];
	
	private static String BoardRxData = "",
						  BoardMsgBuffer[] = new String[UART_RX_MASK];
	
	private static int BoardInputHead = 0,
					   BoardInputTail = 0,
					   BoardRxHead = 0,
					   BoardRxTail = 0,
					   BoardMsgHead = 0,
					   BoardMsgTail = 0,
					   SensorMsgHead = 0,
					   SensorMsgTail = 0;
					   
	private static String SensorMsgBuffer[] = new String[UART_RX_MASK];
	
	private static boolean BoardRxFlag = false;
	
	final static byte BARCODE_RX_BUFFER_SIZE = 32,
					  BARCODE_BUFFER_CNT_SIZE = 16,
					  BARCODE_BUFFER_INDEX_SIZE = 32;
	
	private static byte BarcodeRxBuffer[] = new byte[BARCODE_RX_BUFFER_SIZE],
						BarcodeAppendBuffer[][] = new byte[BARCODE_BUFFER_CNT_SIZE][BARCODE_BUFFER_INDEX_SIZE],
						BarcodeBufCnt = 0,
						BarcodeAppendCnt = 0;

	private static byte HHBarcodeRxBuffer[] = new byte[BARCODE_RX_BUFFER_SIZE],
						HHBarcodeAppendBuffer[][] = new byte[BARCODE_BUFFER_CNT_SIZE][BARCODE_BUFFER_INDEX_SIZE],
						HHBarcodeBufCnt = 0,
						HHBarcodeAppendCnt = 0;

	
	public static byte BarcodeBufIndex = 0;
	public static byte HHBarcodeBufIndex = 0;
	
	public static boolean BarcodeReadStart = false,
						  HHBarcodeReadStart = false;
	
	public static String AmpTemperature = "0";
	
	private class BoardTxThread extends Thread { // Instruction for a board

		private String message;
		private CtrTarget target;
		
		BoardTxThread(String message, CtrTarget target) {
			
			this.message = message;
			this.target = target;
		}
		
		public synchronized void run() {
			
			try {
				
				BoardFileOutputStream = new FileOutputStream(BoardFd);				

				if (BoardFileOutputStream != null) {
//					Log.w("BoardTxThread", "message : " + message);
					BoardFileOutputStream.write(STX);

					switch(target) {
										
					case MotorSet	: 	
						BoardFileOutputStream.write(new String("A").getBytes());
						BoardFileOutputStream.write(new String("012").getBytes()); // Motor shaking angle, default : 012
						BoardFileOutputStream.write(new String("R").getBytes());
						BoardFileOutputStream.write(message.getBytes()); // Motor shaking time, default : 6.5 * 10(sec) = 0065
						break;
						
					default :
						BoardFileOutputStream.write(message.getBytes());
						break;
					}
					
					BoardFileOutputStream.write(ETX);
					
				} else {
					
					return;
				}
				
			} catch (IOException e) {
				
				e.printStackTrace();					
				return;
			}
		}
	}
	
	public class PrinterTxThread extends Thread { // Instruction for a printer
		
		private String type = "";
		private byte mode;
		private StringBuffer txData;
		
		PrinterTxThread(byte mode, StringBuffer txData) {
			
			this.mode = mode;
			this.txData = txData;
		}
		
		public void run() {
			
			try {
				
				pFileOutputStream = new FileOutputStream(pFd);		
				
				if (pFileOutputStream != null) {
					
					pFileOutputStream.write(STX);

					if(HomeActivity.ANALYZER_SW != HomeActivity.CE) {
					
					/* i-SENS */
					pFileOutputStream.write(LF);
					pFileOutputStream.write(CR);
					pFileOutputStream.write("i-SENS, Inc.".getBytes());
					
					/* A1Care */
					pFileOutputStream.write(LF);
					pFileOutputStream.write(CR);
					pFileOutputStream.write(GS); 
					pFileOutputStream.write(0x21); // size of character 
					pFileOutputStream.write(0x01); // 1 times of width and 2 times of height
					pFileOutputStream.write("A1Care".getBytes());
					
					/* Start Line */
					pFileOutputStream.write(LF);
					pFileOutputStream.write(CR);
					pFileOutputStream.write(GS); 
					pFileOutputStream.write(0x21); // size of character 
					pFileOutputStream.write(0x00); // 1 times of width and 1 times of height
					pFileOutputStream.write("------------------------------------".getBytes());
					
					if(mode == PRINTRESULT) {
					
						pFileOutputStream.write(LF);
						pFileOutputStream.write(CR);
						pFileOutputStream.write("Result Data".getBytes());
						
					} else if(mode == PRINTRECORD) {
					
						pFileOutputStream.write(LF);
						pFileOutputStream.write(CR);
						pFileOutputStream.write("Record Data".getBytes());
					}
						
					/* Test Date */
					pFileOutputStream.write(LF);
					pFileOutputStream.write(CR);
					pFileOutputStream.write("Test Date".getBytes());
					
					pFileOutputStream.write(ESC);
					pFileOutputStream.write(0x24);
					pFileOutputStream.write(0xC8);
					pFileOutputStream.write(0x00);
					
					/* Year */
					pFileOutputStream.write(CR);
					pFileOutputStream.write(txData.substring(0, 4).getBytes());
					pFileOutputStream.write(".".getBytes());
					
					/* Month */
					pFileOutputStream.write(CR);
					pFileOutputStream.write(txData.substring(4, 6).getBytes());
					pFileOutputStream.write(".".getBytes());
					
					/* Day */
					pFileOutputStream.write(CR);
					pFileOutputStream.write(txData.substring(6, 8).getBytes());
					pFileOutputStream.write(" ".getBytes());
					
					/* Hour */
					pFileOutputStream.write(CR);
					pFileOutputStream.write(txData.substring(10, 12).getBytes());
					pFileOutputStream.write(":".getBytes());
					
					/* Minute */
					pFileOutputStream.write(CR);
					pFileOutputStream.write(txData.substring(12, 14).getBytes());
					pFileOutputStream.write(" ".getBytes());
					
					/* AM/PM */
					pFileOutputStream.write(CR);
					pFileOutputStream.write(txData.substring(8, 10).getBytes());
					
					/* Type */
					pFileOutputStream.write(LF);
					pFileOutputStream.write(CR);
					pFileOutputStream.write("Type".getBytes());
					
					pFileOutputStream.write(ESC);
					pFileOutputStream.write(0x24);
					pFileOutputStream.write(0xC8);
					pFileOutputStream.write(0x00);
					
					/* HbA1c */
					type = txData.substring(18, 19);
					
					if(type.equals("B")) type = "Control A1c";
					else type = "HbA1c";
					
					pFileOutputStream.write(CR);
					pFileOutputStream.write(type.getBytes());
					
					/* Primary */
					pFileOutputStream.write(LF);
					pFileOutputStream.write(CR);
					pFileOutputStream.write("Primary".getBytes());
					
					pFileOutputStream.write(ESC);
					pFileOutputStream.write(0x24);
					pFileOutputStream.write(0xC8);
					pFileOutputStream.write(0x00);
					
					/* HbA1c percentage */
					pFileOutputStream.write(CR);
					pFileOutputStream.write("NGSP".getBytes());
					
					/* Result */
					pFileOutputStream.write(LF);
					pFileOutputStream.write(CR);
					pFileOutputStream.write("Result".getBytes());
					
					pFileOutputStream.write(ESC);
					pFileOutputStream.write(0x24);
					pFileOutputStream.write(0xC8);
					pFileOutputStream.write(0x00);
					
					/* HbA1c percentage */
					pFileOutputStream.write(CR);
					pFileOutputStream.write(txData.substring(25 + Integer.parseInt(txData.substring(23, 25))).getBytes());
					pFileOutputStream.write(" %".getBytes());
					
					/* Reference Range */
					pFileOutputStream.write(LF);
					pFileOutputStream.write(CR);
					pFileOutputStream.write("Reference Range".getBytes());
					
					pFileOutputStream.write(ESC);
					pFileOutputStream.write(0x24);
					pFileOutputStream.write(0xC8);
					pFileOutputStream.write(0x00);
					
					/* 4.0 - 6.0% */
					pFileOutputStream.write(CR);
					pFileOutputStream.write("4.0 - 6.0 %".getBytes());
					
					/* Test No. */
					pFileOutputStream.write(LF);
					pFileOutputStream.write(CR);
					pFileOutputStream.write("Test No.".getBytes());
					
					pFileOutputStream.write(ESC);
					pFileOutputStream.write(0x24);
					pFileOutputStream.write(0xC8);
					pFileOutputStream.write(0x00);
					
					/* Test number */
					pFileOutputStream.write(CR);
					pFileOutputStream.write(txData.substring(14, 18).getBytes());

					/* Cartridge Lot */
					pFileOutputStream.write(LF);
					pFileOutputStream.write(CR);
					pFileOutputStream.write("Ref#".getBytes());
					
					pFileOutputStream.write(ESC);
					pFileOutputStream.write(0x24);
					pFileOutputStream.write(0xC8);
					pFileOutputStream.write(0x00);
					
					/* Lot number */
					pFileOutputStream.write(CR);
					pFileOutputStream.write(txData.substring(18, 23).getBytes());
					
					/* PID */
					pFileOutputStream.write(LF);
					pFileOutputStream.write(CR);
					pFileOutputStream.write("Patient ID".getBytes());
					
					pFileOutputStream.write(ESC);
					pFileOutputStream.write(0x24);
					pFileOutputStream.write(0xC8);
					pFileOutputStream.write(0x00);
					
					/* Patient ID */
					pFileOutputStream.write(CR);
					pFileOutputStream.write(txData.substring(25, 25 + Integer.parseInt(txData.substring(23, 25))).getBytes());
										
					/* End Line */
					pFileOutputStream.write(LF);
					pFileOutputStream.write(CR);
					pFileOutputStream.write("------------------------------------".getBytes());
					
					pFileOutputStream.write(ESC);
					pFileOutputStream.write(0x64); // print and feed n lines
					pFileOutputStream.write(0x0A); // lines of number
					pFileOutputStream.write(ETX);
					
					} else {
					
						/* Barcode */
						pFileOutputStream.write(LF);
						pFileOutputStream.write(CR);
						pFileOutputStream.write(txData.substring(0).getBytes());
											
						pFileOutputStream.write(ETX);
						
						BarPriLanActivity.isFinish = true;
					}
				}					
						
			} catch (IOException e) {
				
				e.printStackTrace();					
				return;
			}
		}
	}

	public class BoardRxThread extends Thread { // Receiving from a board
		
		public void run() {
			
			while(!isInterrupted()) {
				
				int size;
				
				try {

					if(BoardFileInputStream != null) {

						BoardInputBuffer = new byte[BOARD_INPUT_BUFFER];
						size = BoardFileInputStream.read(BoardInputBuffer);
//						Log.w("BoardRxThread", "BoardInputBuffer : " + new String(BoardInputBuffer));
						BoardRxData(size);
					}
					
				} catch (IOException e) {
					
					e.printStackTrace();
					return;
				}
			}
		}
	}
	
	public synchronized void BoardRxData(int size) {
		
		int tmpHead;
		
		tmpHead = (BoardInputHead + 1) % BOARD_INPUT_MASK;
		
		if(tmpHead != BoardInputTail) {
			
			for(int i = 0; i < size; i++) { 
				
				BoardRxBuffer[tmpHead][i] = BoardInputBuffer[i];
			}
			
			if(size != 8) BoardRxBuffer[tmpHead][size] = 0;
			
			BoardInputHead = tmpHead;
		}
	}
	
//	public byte[] BoardInputData() {
//		
//		int tmpTail;
//		
//		while(BoardInputTail == BoardInputHead) {
//			
//			Sleep(10);
//		}
//		tmpTail = (BoardInputTail + 1) % BOARD_INPUT_MASK;
//		BoardInputTail = tmpTail;
//				
//		return BoardRxBuffer[tmpTail];
//	}
//	
//	public class BoardRxData extends Thread {
//		
//		public void run() {
//			
//			byte[] tmpBuffer;
//			byte tmpData;
//			
//			while(true) {
//				
//				tmpBuffer = BoardInputData();
//				
//				for(int i = 0; i < BOARD_INPUT_BUFFER; i++) {
//					
//					tmpData = tmpBuffer[i];
//
//					if(tmpData == 0) break;
//					
//					if(tmpData != STX) {
//						
//						if(BoardRxFlag) {
//							
//							if(tmpData == ETX) {
//								
//								BoardMessageForm(BoardRxData);
//								BoardRxFlag = false;
//								
//							} else BoardRxData += Character.toString((char) tmpData);	
//						}
//						
//					} else {
//						
//						BoardRxFlag = true;
//						BoardRxData = "";
//					}
//				}
//			}
//		}
//	}
	
	public byte[] BoardInputData() {
		
		int tmpTail;
		
		if(BoardInputTail != BoardInputHead) {
			
			tmpTail = (BoardInputTail + 1) % BOARD_INPUT_MASK;
			BoardInputTail = tmpTail;
					
			return BoardRxBuffer[tmpTail];	
		
		} else return null;
	}
	
	public void BoardRxData2() {
		
		BoardRxData mBoardRxData = new BoardRxData();
		mBoardRxData.start();
	}
	
	public class BoardRxData extends Thread {
		
		public void run() {
			
			byte[] tmpBuffer;
			byte tmpData;
			
			tmpBuffer = BoardInputData();
			
//			Log.w("BoardRxData", "tmpBuffer : " + tmpBuffer);
			
			if(tmpBuffer != null) {
				
				for(int i = 0; i < BOARD_INPUT_BUFFER; i++) {
					
					tmpData = tmpBuffer[i];

					if(tmpData == 0) break;
					
					if(tmpData != STX) {
						
						if(BoardRxFlag) {
							
							if(tmpData == ETX) {
								
								BoardMessageForm(BoardRxData);
								BoardRxFlag = false;
								
							} else BoardRxData += Character.toString((char) tmpData);	
						}
						
					} else {
						
						BoardRxFlag = true;
						BoardRxData = "";
					}
				}
			}
		}
	}
	
	public void BoardMessageForm(String tmpStrData) {
		
		int tmpHead;

//		Log.w("BoardMessageForm", "tmpStrData : " + tmpStrData);
		
		if(tmpStrData.substring(0, 1).equals("S")) {
			
			SensorMessageBuffer(tmpStrData);
			
		} else if(tmpStrData.substring(0, 1).equals("E")) {
			
		} else {
				
			tmpHead = (BoardMsgHead + 1) % UART_RX_MASK;
			
			if(BoardMsgTail != tmpHead) {
				
				BoardMsgBuffer[tmpHead] = tmpStrData;
				BoardMsgHead = tmpHead;
				
			} else {
				
			}	
		}
	}
	
//	public void BoardMessageForm(String tmpStrData) {
//		
//		int tmpHead;
//
//		Log.w("BoardMessageForm", "tmpStrData : " + tmpStrData);
//		
//		if(tmpStrData.substring(0, 1).equals("S")) {
//			
//			SensorMessageBuffer(tmpStrData);
//			
//		} else if(tmpStrData.substring(0, 1).equals("E")) {
//			
//		} else {
//				
//			tmpHead = (BoardMsgHead + 1) % UART_RX_MASK;
//			
//			if(BoardMsgTail != tmpHead) {
//				
//				BoardMsgBuffer[tmpHead] = tmpStrData;
//				BoardMsgHead = tmpHead;
//				
//			} else {
//				
//			}	
//		}
//	}
	
	public String BoardMessageOutput() {
		
		int tmpTail;
		
//		Log.w("BoardMessageOutput", "BoardMsgHead : " + BoardMsgHead + " BoardMsgTail : " + BoardMsgTail);
		
		if(BoardMsgHead == BoardMsgTail) return "NR";
		
		tmpTail = (BoardMsgTail + 1) % UART_RX_MASK;
		BoardMsgTail = tmpTail;
		
		return BoardMsgBuffer[tmpTail];
	}
	
//	public void SensorMessageBuffer(String tmpData) {
//		
//		int tmpHead;
//		
//		tmpHead = (SensorMsgHead + 1) % UART_RX_MASK;
//		
//		if(SensorMsgTail != tmpHead) {
//			
//			SensorMsgBuffer[tmpHead] = tmpData;
//			SensorMsgHead = tmpHead;
//		}
//	}
	
	public void SensorMessageBuffer(String tmpData) {
		
		int tmpHead;
		
		tmpHead = (SensorMsgHead + 1) % UART_RX_MASK;
		
		if(SensorMsgTail != tmpHead) {
			
			SensorMsgBuffer[tmpHead] = tmpData;
			SensorMsgHead = tmpHead;
		}
	}
	
//	public String SensorMessageOutput() {
//		
//		int tmpTail;
//		
//		while(SensorMsgHead == SensorMsgTail) {
//			
//			Sleep(100);
//		}
//		tmpTail = SensorMsgTail + 1;
//		if(tmpTail == UART_RX_MASK) tmpTail = 0;
//		SensorMsgTail = tmpTail;
//		
//		return SensorMsgBuffer[tmpTail];
//	}
	
	public String SensorMessageOutput() {
		
		int tmpTail;
		
		if(SensorMsgHead == SensorMsgTail) return "NR";
			
		tmpTail = SensorMsgTail + 1;
		if(tmpTail == UART_RX_MASK) tmpTail = 0;
		SensorMsgTail = tmpTail;
		
		return SensorMsgBuffer[tmpTail];
	}
	
	public class BarcodeRxThread extends Thread { // Receiving from a barcode sensor
		
		public void run() {

			while(!isInterrupted()) {
				
				int size;
				
				try {
					
					if(BarcodeFileInputStream != null) {
						
						BarcodeRxBuffer = new byte[BARCODE_RX_BUFFER_SIZE];
						size = BarcodeFileInputStream.read(BarcodeRxBuffer);
	
//						Log.w("BarcodeRxThread", "BarcodeInputBuffer : " + new String(BarcodeRxBuffer));
						
						if(size > 0) {
							
							BarcodeDataReceive(size);
						}
					}
					
				} catch (IOException e) {
					
					e.printStackTrace();
					return;
				}
			}
		}
	}
		
	public synchronized void BarcodeDataReceive(int size) { // making a buffer of received data from a barcode sensor
		
		if(BarcodeReadStart == false) {
		
			BarcodeReadStart = true;
			BarcodeBufIndex = 0;
			BarcodeAppendBuffer[BarcodeBufCnt] = new byte[BARCODE_BUFFER_INDEX_SIZE];
		}
//		Log.w("BarcodeDataReceive", "index : " + BarcodeBufIndex);
		
		for(int i = 0; i < size; i++) {

			BarcodeAppendBuffer[BarcodeBufCnt][BarcodeBufIndex++] = BarcodeRxBuffer[i]; // bufCnt : number of each buffer, bufIndex : bit index of one buffer
//			Log.w("BarcodeDataReceive", "BarcodeRxBuffer : " + Character.toString((char) BarcodeRxBuffer[i]));
		}	
		
		if(BarcodeBufIndex > 18 | BarcodeBufIndex < 2) {
			
			ActionActivity.IsCorrectBarcode = false;
			ActionActivity.BarcodeCheckFlag = true;
			BarcodeReadStart = false;
		
		} else {
			
			if(BarcodeRxBuffer[size-2] == CR && BarcodeRxBuffer[size-1] == LF) { // Whether or not end bit

				BarcodeReadStart = false;
				
				BarcodeDataAppend(BarcodeBufCnt, BarcodeBufIndex);
				
				BarcodeBufCnt++;
				if(BarcodeBufCnt == BARCODE_BUFFER_CNT_SIZE) BarcodeBufCnt = 0;
			}
		}
	}
	
	public synchronized void BarcodeDataAppend(byte num, byte len) {
		
		try {
			
			final StringBuffer barcodeReception = new StringBuffer();
			
			barcodeReception.append(new String(BarcodeAppendBuffer[num], 0, len));
		
			SerialBarcode = new Barcode();
			SerialBarcode.BarcodeCheck(barcodeReception);
				
		} catch(ArrayIndexOutOfBoundsException e) {
			
			e.printStackTrace();
			BarcodeBufCnt = 0;
			return;
		}		
	}
	
	public class HHBarcodeRxThread extends Thread { // Receiving from a barcode sensor
		
		public void run() {

			while(HomeActivity.ExternalDeviceBarcode == HomeActivity.FILE_OPEN) {
						
				int size;
				
				try {
					
					if(HHBarcodeFileInputStream != null) {
						
						HHBarcodeRxBuffer = new byte[BARCODE_RX_BUFFER_SIZE];
						size = HHBarcodeFileInputStream.read(HHBarcodeRxBuffer);
						
						Log.w("HHBarcodeRxThread", "HHBarcodeInputBuffer : " + new String(HHBarcodeRxBuffer));
						
						if(size > 0) {
								
							HHBarcodeDataReceive(size);
						
						} else {
							
							HHBarcodeFileInputStream.close();
							
							close();
						}
						
					} else {
						
						return;
					}
					
				} catch (IOException e) {
					
					e.printStackTrace();
					return;	
				}
			}
		}
	}
	
	public synchronized void HHBarcodeDataReceive(int size) { // making a buffer of received data from a barcode sensor
		
		if(HHBarcodeReadStart == false) {
		
			HHBarcodeReadStart = true;
			HHBarcodeBufIndex = 0;
			HHBarcodeAppendBuffer[BarcodeBufCnt] = new byte[BARCODE_BUFFER_INDEX_SIZE];
		}
//		Log.w("HHBarcodeDataReceive", "index : " + HHBarcodeBufIndex);
		
		for(int i = 0; i < size; i++) {

			HHBarcodeAppendBuffer[HHBarcodeBufCnt][HHBarcodeBufIndex++] = HHBarcodeRxBuffer[i]; // bufCnt : number of each buffer, bufIndex : bit index of one buffer
//			Log.w("HHBarcodeDataReceive", "HHBarcodeRxBuffer : " + Character.toString((char) HHBarcodeRxBuffer[i]));
		}	
		
		if(HHBarcodeRxBuffer[size-1] == CR) { // Whether or not end bit

			HHBarcodeReadStart = false;
			
			HHBarcodeDataAppend(HHBarcodeBufCnt, HHBarcodeBufIndex);
			
			HHBarcodeBufCnt++;
			if(HHBarcodeBufCnt == BARCODE_BUFFER_CNT_SIZE) HHBarcodeBufCnt = 0;
		}
	}
	
	public synchronized void HHBarcodeDataAppend(byte num, byte len) {
		
		try {
			
			final StringBuffer HHbarcodeReception = new StringBuffer();
			
			HHbarcodeReception.append(new String(HHBarcodeAppendBuffer[num], 0, len));
			
			if(TimerDisplay.timerState == whichClock.ResultClock) {
				
				Handler mHandler = new Handler(Looper.getMainLooper());

				mHandler.postDelayed(new Runnable() {

					public void run() {
	
						SerialResult = new ResultActivity();
						SerialResult.PatientIDDisplay(HHbarcodeReception);
					}
				}, 0);
			
			} else if(TimerDisplay.timerState == whichClock.BarPriLanClock) {
				
				Handler mHandler = new Handler(Looper.getMainLooper());

				mHandler.postDelayed(new Runnable() {

					public void run() {
	
						mBarPriLanActivity = new BarPriLanActivity();
						mBarPriLanActivity.HHBBarcodeDisplay(HHbarcodeReception);
					}
				}, 0);
			}
				
		} catch(ArrayIndexOutOfBoundsException e) {
			
			e.printStackTrace();
			HHBarcodeBufCnt = 0;
			return;
		}		
	}
	
	public void BoardSerialInit() {
		
		BoardFd = open("/dev/ttySAC3", 9600, 0);
	}
	
	public void BoardRxStart() {
		
		BoardFileInputStream = new FileInputStream(BoardFd);
				
		mBoardRxThread = new BoardRxThread();
		mBoardRxThread.start();
		
		BoardRxData BoardRxDataObj = new BoardRxData();
		BoardRxDataObj.start();
	}
	
	public synchronized void BoardTx(String str, CtrTarget trg) {
		
		switch(trg) {

		case MotorSet	:	
			mBoardTxThread = new BoardTxThread(str, CtrTarget.MotorSet);
			mBoardTxThread.start();
			break;
										
		default			: 
			mBoardTxThread = new BoardTxThread(str, trg);
			mBoardTxThread.start();
			break; 
		}
	}
	
	public void PrinterSerialInit() {
	
		if(HomeActivity.ANALYZER_DEVICE == HomeActivity.PP) pFd = open("/dev/ttySAC2", 9600, 0); // PP
		else if(HomeActivity.ANALYZER_DEVICE == HomeActivity.ES) pFd = open("/dev/ttySAC1", 9600, 0); // ES
		else pFd = open("/dev/ttySAC2", 9600, 0); // PP
	}
	
	public void PrinterTxStart(byte mode, StringBuffer txData) {
		
		pPrinterTxThread = new PrinterTxThread(mode, txData);
		pPrinterTxThread.start();
	}
	
	public void BarcodeSerialInit() {

		if(HomeActivity.ANALYZER_DEVICE == HomeActivity.PP) BarcodeFd = open("/dev/ttySAC1", 115200, 0); // PP
		else if(HomeActivity.ANALYZER_DEVICE == HomeActivity.ES) BarcodeFd = open("/dev/ttySAC2", 115200, 0); // PP
		else BarcodeFd = open("/dev/ttySAC1", 115200, 0); // PPs
	}

	public void BarcodeRxStart() {
		
		BarcodeFileInputStream = new FileInputStream(BarcodeFd);
		bBarcodeRxThread = new BarcodeRxThread();
		bBarcodeRxThread.start();
	}
	
	public void HHBarcodeSerialInit() {
			
		try {

            Runtime.getRuntime().exec("ssu -c busybox chmod 0777 /dev/ttyACM0").waitFor();
    
        } catch (IOException e) {
            Log.e("OLE","Runtime Error: "+e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mFd = open("/dev/ttyACM0", 9600, 0);
	}
	
	public void HHBarcodeRxStart() {
		
		try {
			
			HHBarcodeFileInputStream = new FileInputStream(mFd);
			hBarcodeRxThread = new HHBarcodeRxThread();
			hBarcodeRxThread.start();
			
			HomeActivity.ExternalDeviceBarcode = HomeActivity.FILE_OPEN;
				
		} catch(NullPointerException e) {
			
			HomeActivity.ExternalDeviceBarcode = HomeActivity.FILE_NOT_OPEN;
		}
	}
	
	public void HHBarcodeSerialClose() {
	
		try {
			
			HHBarcodeFileInputStream.close();

//			Log.w("HHBarcodeSerialClose", "RX Thread : " + hBarcodeRxThread + " Input Stream : " + HHBarcodeFileInputStream);
			
			if(HomeActivity.ExternalDeviceBarcode == HomeActivity.FILE_OPEN) {
				
				close();
			}

			HomeActivity.ExternalDeviceBarcode = HomeActivity.FILE_CLOSE;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void Sleep(int t) { // t : msec
		
		try {
			
			Thread.sleep(t);
			
		} catch(InterruptedException e) {
			
			e.printStackTrace();			
			return;
		}
	}
	
	static {
	
		System.loadLibrary("serial_port");
	}
	
	/*JNI*/
	public native static FileDescriptor open(String path, int baudrate, int flags);
	public native void close();
}
