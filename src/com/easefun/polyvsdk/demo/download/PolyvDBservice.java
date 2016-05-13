package com.easefun.polyvsdk.demo.download;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.opengl.GLSurfaceView.EGLWindowSurfaceFactory;
import android.util.Log;

public class PolyvDBservice {
	private static final String TAG = "DBservice";
	private PolyvDBOpenHepler dbOpenHepler;
	private SQLiteDatabase db;

	public PolyvDBservice(Context context) {
		// 1 -> database version
		dbOpenHepler = PolyvDBOpenHepler.getInstance(context, 3);
	}

	public void addDownloadFile(PolyvDownloadInfo info) {
		db = dbOpenHepler.getWritableDatabase();
		String sql = "insert into downloadlist(vid,title,duration,filesize,bitrate) values(?,?,?,?,?)";
		db.execSQL(
				sql,
				new Object[] { info.getVid(),info.getTitle(), info.getDuration(),
						info.getFilesize(),info.getBitrate() });
	}
	public void deleteDownloadFile(PolyvDownloadInfo info) {
		db = dbOpenHepler.getWritableDatabase();
		String sql = "delete from downloadlist where vid=? and bitrate=?";
		db.execSQL(
				sql,
				new Object[] { info.getVid(),info.getBitrate() });
	}
	public void updatePercent(PolyvDownloadInfo info,long percent,long total) {
		db = dbOpenHepler.getWritableDatabase();
		String sql = "update downloadlist set percent=?,total=? where vid=? and bitrate=?";
		db.execSQL(
				sql,
				new Object[] { percent,total,info.getVid(),info.getBitrate() });
	}
	public boolean isAdd(PolyvDownloadInfo info) {
		db = dbOpenHepler.getWritableDatabase();
		String sql = "select vid ,duration,filesize,bitrate from downloadlist where vid=? and bitrate=" + info.getBitrate();
		Cursor cursor = db.rawQuery(sql, new String[] { info.getVid() });
		if (cursor.getCount() == 1) {
			return true;
		} else {
			return false;
		}
	}
	
	public LinkedList<PolyvDownloadInfo> getDownloadFiles(){
		LinkedList<PolyvDownloadInfo> infos = new LinkedList<PolyvDownloadInfo>();
		db = dbOpenHepler.getWritableDatabase();
		String sql ="select vid,title,duration,filesize,bitrate,percent,total from downloadlist";
		Cursor cursor= db.rawQuery(sql, null);
		while(cursor.moveToNext()){
			String vid=cursor.getString(cursor.getColumnIndex("vid"));
			String title=cursor.getString(cursor.getColumnIndex("title"));
			String duration=cursor.getString(cursor.getColumnIndex("duration"));
			long filesize=cursor.getInt(cursor.getColumnIndex("filesize"));
			int bitrate=cursor.getInt(cursor.getColumnIndex("bitrate"));
			long percent=cursor.getInt(cursor.getColumnIndex("percent"));
			long total=cursor.getInt(cursor.getColumnIndex("total"));
			PolyvDownloadInfo info = new PolyvDownloadInfo(vid, duration, filesize,bitrate);
			info.setPercent(percent);
			info.setTitle(title);
			info.setTotal(total);
			infos.addLast(info);
		}
		return infos;
	}
}
