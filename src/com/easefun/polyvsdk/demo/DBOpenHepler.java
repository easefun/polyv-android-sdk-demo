package com.easefun.polyvsdk.demo;
 
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHepler extends SQLiteOpenHelper{
    private static final String DATEBASENAME="downloadlist.db";
    
    
    public DBOpenHepler(Context context,int version){
    	super(context,DATEBASENAME, null, version);
    }
    
	public DBOpenHepler(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("create table if not exists downloadlist (vid varchar(20) primary key,duration varchar(20),filesize int)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
