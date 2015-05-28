package isens.hba1c_analyzer;

import java.text.DecimalFormat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class Barcode {

	final static double a1ref  = 0.01, 
						b1ref  = -0.07, 
						a21ref = 0.05, 
						b21ref = 0.03, 
						a22ref = 0.035, 
						b22ref = 0.04;
	
	private GpioPort BarcodeGpio;
	
	private float refScale[] = {1f, 1f, 1f, 1f, 0.6f, 1.4f, 1f};
	
	public static String RefNum;
	
	public static double a1, 
						 b1, 
						 a21, 
						 b21, 
						 a22, 
						 b22, 
						 L, 
						 H;
	
	public static String Barcode;
	
	public void BarcodeCheck(StringBuffer buffer) { // Check a barcode data
		
		int len, 
			test, 
			year, 
			month, 
			day, 
			line, 
			locate, 
			check, 
			sum; 		
		
		float scale;
		
		len = buffer.length();
		
//		if(len == 18) { // Check length of barcode data

//			try {
//				
//				if(HomeActivity.ANALYZER_SW != HomeActivity.CE) {
//				
//				DecimalFormat dfm = new DecimalFormat("00");
//				
//				/* Classification for each digit barcode */
//				test   = (int) buffer.charAt(0) - 64;
//				scale = refScale[test - 1];
//				year   = (int) buffer.charAt(1) - 64;
//				if(year > 26) year -= 6;
//				month  = (int) buffer.charAt(2) - 64;
//				day    = (int) buffer.charAt(3) - 64;
//				if(day > 26) day -= 6;
//				line   = (int) buffer.charAt(4) - 64;
//				locate = (int) buffer.charAt(5) - 42;
//				check  = (int) buffer.charAt(15) - 48;
//				
//				RefNum = buffer.substring(0, 5);
//				
//				a1  = 0.0001 * (0.5 * ((int) buffer.charAt(6) - 42) - 20) + scale * a1ref; // tHb slope
//				b1  = 0.0001 * (20 * ((int) buffer.charAt(7) - 42) - 800) + scale * b1ref; // tHb y-intercept
//				a21 = 0.00025 * (((int) buffer.charAt(8) - 42) - 40) + scale * a21ref; // A1c-Low slope
//				b21 = 0.001 * (((int) buffer.charAt(9) - 42) - 40) + scale * b21ref; // A1c-Low intercept
//				a22 = 0.00025 * (((int) buffer.charAt(10) - 42) - 40) + scale * a22ref; // A1c-High slope
//				b22 = 0.001 * (((int) buffer.charAt(11) - 42) - 40) + scale * b22ref; // A1c-High intercept
//				L   = 0.2 * (0.5 * ((int) buffer.charAt(12) - 42) - 5) + 5; // A1c-Low %
//				H   = 0.2 * (0.5 * ((int) buffer.charAt(13) - 42) - 5) + 9; // A1c-High %
//				
//				sum = (test + year + month + day + line + locate) % 10; // Checksum bit
//				
//				Log.w("Barcode", "scale : " + scale + " test : " + test + " year : " + year + " month : " + month + " day : " + day + " line : " + line + " locate : " + locate + " check : " + check);
//				Log.w("Barcode", "a1ref : " + scale * a1ref + " b1ref : " + scale * b1ref + " a21ref : " + scale * a21ref + " b21ref : " + scale * b21ref + " a22ref : " + scale * a22ref + " b22ref : " + scale * b22ref);
//				Log.w("Barcode", "a1 : " + a1 + " b1 : " + b1 + " a21 : " + a21 + " b21 : " + b21 + " a22 : " + a22 + " b22 : " + b22 + " L : " + L + " H : " + H);
//				
//				if( sum == check ) { // Whether or not the correct barcode code
//	
////					Log.w("Barcode", "Correct Data : " + buffer);
//					BarcodeStop(true);
//						
//				} else {
//					
//					BarcodeStop(false);	
//				}
//			
//				} else {
//					
					Barcode = buffer.substring(0, len-2);
//					
//					Log.w("Barcode check", "barcode : " + Barcode);
					
//					if(Barcode.equals("DAJAUvMfRPQU24+1")) {
						
						BarcodeStop(true);
						
//					} else {
//						
//						BarcodeStop(false);	
//					}
//				}
//				
//			} catch (NumberFormatException e) {
//				
//				e.printStackTrace();
//			}
//		} else {
//			
//			BarcodeStop(false);
//		}
	}
	
	public void BarcodeStop(boolean state) { // Turn off barcode module
		
		Log.w("Barcpde stop", "state : " + state);
		
		if(state) {
			
			ActionActivity.IsCorrectBarcode = true;
			ActionActivity.BarcodeCheckFlag = true;
			
		} else {
//			Log.w("BarcodeStop", "BarcodeCheck : " + ActionActivity.BarcodeCheckFlag);
			ActionActivity.IsCorrectBarcode = false;
			ActionActivity.BarcodeCheckFlag = true;
		}
	}
}
