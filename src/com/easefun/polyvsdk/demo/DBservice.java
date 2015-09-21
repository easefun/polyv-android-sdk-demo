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
		String sql = "insert into downloadlist(vid,title,duration,filesize,bitrate) values(?,?,?,?,?)";
		db.execSQL(
				sql,
				new Object[] { info.getVid(),info.getTitle(), info.getDuration(),
						info.getFilesize(),info.getBitrate() });
	}
	public void deleteDownloadFile(DownloadInfo info) {
		SQLiteDatabase db = dbOpenHepler.getWritableDatabase();
		String sql = "delete from downloadlist where vid=? and bitrate=?";
		db.execSQL(
				sql,
				new Object[] { info.getVid(),info.getBitrate() });
	}
	public void updatePercent(DownloadInfo info,int percent) {
		SQLiteDatabase db = dbOpenHepler.getWritableDatabase();
		String sql = "update downloadlist set percent=? where vid=? and bitrate=?";
		db.execSQL(
				sql,
				new Object[] { percent, info.getVid(),info.getBitrate() });
	}
	public boolean isAdd(DownloadInfo info) {
		SQLiteDatabase db = dbOpenHepler.getWritableDatabase();
		String sql = "select vid ,duration,filesize,bitrate from downloadlist where vid=? and bitrate=" + info.getBitrate();
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
		String sql ="select vid,title,duration,filesize,bitrate,percent from downloadlist";
		Cursor cursor= db.rawQuery(sql, null);
		while(cursor.moveToNext()){
			String vid=cursor.getString(cursor.getColumnIndex("vid"));
			String title=cursor.getString(cursor.getColumnIndex("title"));
			String duration=cursor.getString(cursor.getColumnIndex("duration"));
			int filesize=cursor.getInt(cursor.getColumnIndex("filesize"));
			int bitrate=cursor.getInt(cursor.getColumnIndex("bitrate"));
			int percent=cursor.getInt(cursor.getColumnIndex("percent"));
			DownloadInfo info = new DownloadInfo(vid, duration, filesize,bitrate);
			info.setPercent(percent);
			info.setTitle(title);
			infos.addLast(info);
		}
		return infos;
	}
}
