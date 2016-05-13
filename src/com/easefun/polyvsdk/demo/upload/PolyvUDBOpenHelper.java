package com.easefun.polyvsdk.demo.upload;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class PolyvUDBOpenHelper extends SQLiteOpenHelper {
	private static final String DATEBASENAME = "uploadlist.db";

	private static PolyvUDBOpenHelper instance = null;

	// 单例，避免数据库内存泄漏
	public static PolyvUDBOpenHelper getInstance(Context context, int version) {
		if (instance == null) {
			synchronized (PolyvUDBOpenHelper.class) {
				if (instance == null)
					instance = new PolyvUDBOpenHelper(context, version);
			}
		}
		return instance;
	}

	public PolyvUDBOpenHelper(Context context, int version) {
		super(context, DATEBASENAME, null, version);
	}

	public PolyvUDBOpenHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(
				"create table if not exists uploadlist (vid varchar(20),title varchar(100),filepath varchar(100),desc varchar(20),filesize int,percent int default 0,total int default 0,primary key (vid))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS uploadlist");
		onCreate(db);
	}

}
