package com.easefun.polyvsdk.demo.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class PolyvDBOpenHepler extends SQLiteOpenHelper {
	private static final String DATEBASENAME = "downloadlist.db";
	private static PolyvDBOpenHepler instance = null;

	// 单例，避免数据库内存泄漏
	public static PolyvDBOpenHepler getInstance(Context context, int version) {
		if (instance == null) {
			synchronized (PolyvDBOpenHepler.class) {
				if (instance == null)
					instance = new PolyvDBOpenHepler(context.getApplicationContext(), version);
			}
		}
		return instance;
	}

	public PolyvDBOpenHepler(Context context, int version) {
		super(context, DATEBASENAME, null, version);
	}

	public PolyvDBOpenHepler(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(
				"create table if not exists downloadlist (vid varchar(20),speed varchar(15),title varchar(100),duration varchar(20),filesize int,bitrate int,percent int default 0,total int default 0,primary key (vid, bitrate,speed))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS downloadlist");
		onCreate(db);
	}

}
