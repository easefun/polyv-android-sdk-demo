package com.easefun.polyvsdk.demo;
 
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBservice {
	private static final String TAG = "DBservice";
	private DBOpenHepler dbOpenHepler;

	
	public DBservice(Context context) {
		// 1 -> database version
		dbOpenHepler = new DBOpenHepler(context, 1);
	}

	public void addDownloadFile(DownloadInfo info) {
		SQLiteDatabase db = dbOpenHepler.getWritableDatabase();
		String sql = "insert into downloadlist(vid,duration,filesize) values(?,?,?)";
		db.execSQL(
				sql,
				new Object[] { info.getVid(), info.getDuration(),
						info.getFilesize() });
		Log.i(TAG, "add to db");
	}

	public boolean isAdd(DownloadInfo info) {
		SQLiteDatabase db = dbOpenHepler.getWritableDatabase();
		String sql = "select vid ,duration,filesize from downloadlist where vid=?";
		Cursor cursor = db.rawQuery(sql, new String[] { info.getVid() });
		if (cursor.getCount() == 1) {
			return true;
		} else {
			return false;
		}
	}
	
	public LinkedList<DownloadInfo> getDownloadFiles(){
		LinkedList<DownloadInfo> infos = new LinkedList<DownloadInfo>();
		SQLiteDatabase db = dbOpenHepler.getWritableDatabase();
		String sql ="select vid,duration,filesize from downloadlist";
		Cursor cursor= db.rawQuery(sql, null);
		while(cursor.moveToNext()){
			String vid=cursor.getString(cursor.getColumnIndex("vid"));
			String duration=cursor.getString(cursor.getColumnIndex("duration"));
			int filesize=cursor.getInt(cursor.getColumnIndex("filesize"));
			infos.addLast(new DownloadInfo(vid, duration, filesize));
		}
		return infos;
	}
}
