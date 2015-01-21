package isens.hba1c_analyzer;

import java.text.DecimalFormat;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHander extends SQLiteOpenHelper {

	public static final String DB_NAME = "A1Care_DB";
	
	public static final int DB_VERSION = 1;
	
	public static final String TABLE = "Operator",
							   PRIMARY_KEY = "PK",
							   COLUMN1 = "ID",
							   COLUMN2 = "RegisteredDate",
							   COLUMN3 = "Password",
							   COLUMN4 = "IsLastLogin";
	
	public DatabaseHander(Context context) {
		
		super(context, DB_NAME, null, DB_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		
		String str = "CREATE TABLE IF NOT EXISTS " + TABLE + "(" + 
				     PRIMARY_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				     COLUMN1 	 + " TEXT UNIQUE, " + 
				     COLUMN2 	 + " TEXT, " + 
				     COLUMN3 	 + " TEXT, " +
				     COLUMN4	 + " TEXT " +
				     ");";
		
		Log.w("Create", "str : " + str);
		
		db.execSQL(str);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		String str = "DROP TABLE IF EXISTS " + TABLE + ";"; 
		
		db.execSQL(str);
		
		onCreate(db);
	}

	public void AddOperator(String id, String date, String password) {
		
		String str = "INSERT INTO " + TABLE + " VALUES " +
			     	 "(" + 
			     	 "null, " +
			     	 "'" + id	    + "', " +
			     	 "'" + date	    + "', " +
			     	 "'" + password + "', " +
			     	 "'false'" +
			     	 ");";

		Log.w("Add Operator", "str : " + str);
		
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL(str);
				
		db.close();
	}
	
	public void UpdateOperator(String id, String password) {
		 
//		String date = TimerDisplay.rTime[0] + TimerDisplay.rTime[1] + TimerDisplay.rTime[2] + TimerDisplay.rTime[7] + TimerDisplay.rTime[5]; // year + month + day + hour + minute
		
		UpdateField(COLUMN3, password, COLUMN1, id);
//		UpdateField(COLUMN2, date, COLUMN1, id);
	}
	
	public void UpdateLastLogIn(String id) {
		
		UpdateField(COLUMN4, "false", COLUMN4, "true");
		UpdateField(COLUMN4, "true", COLUMN1, id);
	}
	
	public void UpdateField(String sField, String sValue, String cField, String cValue) {
		
		String str = "UPDATE " + TABLE + 
				     " SET "   + sField + " = '" + sValue + "'" +
				     " WHERE " + cField + " = '" + cValue + "';";

		Log.w("Update Field", "str : " + str);
		
		SQLiteDatabase db = this.getWritableDatabase();
				
		db.execSQL(str);
				
		db.close();
	}
	
	public void DeleteOperator(String id) {
		
		String str = "DELETE FROM " + TABLE + 
				     " WHERE " + COLUMN1 + " = '" + id + "';";
		
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL(str);
		db.close();
	}
	
	public String GetPassword(String id) {
		
		String value[] = new String[4];
		String password;
		
		value = GetRowWithField(COLUMN1, id);
		
		password = value[2];
		
		Log.w("Get Password", "password : " + password);
		
		return password;
	}
	
	public String GetLastLoginID() {
		
		String value[] = new String[4];
		String id;
		
		value = GetRowWithField(COLUMN4, "true");
		
		id = value[0];
		
		Log.w("Get Last log in", "id : " + id);
		
		return id;
	}
	
	public boolean CheckIDDuplication(String id) {
		
		boolean isDuplicated = false;
		String savedID[] = GetRowWithField(COLUMN1, id);
		
		if(savedID[0] != null) isDuplicated = true;
		
		Log.w("Check ID Duplication", "isDuplicated : " + isDuplicated);
		
		return isDuplicated;
	}
	
	public String[] GetRowWithField(String sField, String sValue) {
		
		String value[] = new String[4]; 
		String str = "SELECT * " + 
				     "FROM "  + TABLE   +
				     " WHERE " + sField + " = '" + sValue + "';";
	
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor;
		
		cursor = db.rawQuery(str, null);
		
		if(cursor.moveToFirst()) {
			
			for(int i = 1; i < cursor.getColumnCount(); i++) {
			
				value[i-1] = cursor.getString(i);
			}
		}
		
		cursor.close();
		db.close();
		
		Log.w("Get Row With Field", "str : " + str);
		
		return value; // value[0] = id, value[1] = password, value[2] = date, value[3] = flag of last log in 
	}
	
//	public HashMap<String, String> GetTableDetails() {
//		
//		HashMap<String, String> table = new HashMap<String, String>();
//		
//		String str = "SELECT * FROM " + TABLE + ";";
//		 
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery(str, null);
//        
//        cursor.moveToFirst();
//        if(cursor.getColumnCount() > 0){
//        	
//        	Log.w("Get Table Details", "str : " + str);
//        	
//        	table.put(COLUMN1, cursor.getString(1));
//        	table.put(COLUMN2, cursor.getString(2));
//        	table.put(COLUMN3, cursor.getString(3));
//        	table.put(COLUMN4, cursor.getString(4));
//        }
//        
//        cursor.close();
//        db.close();
//        
//		return table;
//	}
	
	public String[] GetRowWithNumber(int number) {
		
		String value[] = new String[4]; 
		String str = "SELECT * " + 
				     "FROM "     + TABLE +
				     " LIMIT "   + "1" +
				     " OFFSET "  + Integer.toString(number) + ";";
	
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor;
		
		cursor = db.rawQuery(str, null);
		
		if(cursor.moveToFirst()) {
			
			for(int i = 1; i < cursor.getColumnCount(); i++) {
			
				value[i-1] = cursor.getString(i);
			}
		}
		
		cursor.close();
		db.close();
		
		Log.w("Get Row With Number", "str : " + str);
		
		return value; // value[0] = id, value[1] = password, value[2] = date, value[3] = flag of last log in 
	}
	
	public int GetRowCount() {
		
		int count;
		String str = "SELECT * FROM " + TABLE + ";";
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(str, null);
		
		count = cursor.getCount();
		
		cursor.close();
		db.close();
		
		return count;
	}
}
