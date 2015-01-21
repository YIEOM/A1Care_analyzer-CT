package isens.hba1c_analyzer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DBAdapter {
	
	private DatabaseHelper mHelper;
	private SQLiteDatabase mDb;
	
	private final Context mCxt;
	private static String SQL_TABLE_CREATE;
	private static String TABLE_NAME;
	
	private static final String DATABASE_NAME = "Member.db";
	private static final int DATABASE_VERSION = 1;
	
	public static final String SQL_CREATE_MEMBER = 
		"create table member ("
	    + "id integer primary key,"
		+ "Ipaddr text null,"
		+ "Port text null,"
		+ "Senda text null,"
		+ "Sendf text null,"
		+ "Rcva text null,"
		+ "Rcvf text null,"
		+ "Cid text null,"
		+ "Qid text null"
		+ ")";
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			
			super(context, name, factory, version);
		}

		public void onCreate(SQLiteDatabase db) {
			
			db.execSQL(SQL_TABLE_CREATE);
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
		
	}
	
	public DBAdapter(Context cxt, String sql, String tableName) {
		
		this.mCxt = cxt;
		SQL_TABLE_CREATE = sql;
		TABLE_NAME = tableName;
	}
	
	public DBAdapter open() throws SQLException {
		
		mHelper = new DatabaseHelper(mCxt);
		mDb = mHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		
		mHelper.close();
	}
	
	public long insertTable(ContentValues values) {
		
		return mDb.insert(TABLE_NAME, null, values);
	}

	public boolean deleteTable(){
		
		return mDb.delete(TABLE_NAME, null, null) > 0;
	}
	
	public boolean updateTable(ContentValues values, String pkColumn, long pkData) {
		
		return mDb.update(TABLE_NAME, values, pkColumn + "=" + pkData, null) > 0;
	}
	
	public Cursor selectTable(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
		
		return mDb.query(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy);
	}
}

