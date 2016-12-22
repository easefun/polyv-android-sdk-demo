package com.easefun.polyvsdk.demo.upload;

import java.io.File;
import java.util.LinkedList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PolyvUDBService {
	private PolyvUDBOpenHelper udbOpenHepler;
	private SQLiteDatabase db;

	public PolyvUDBService(Context context) {
		// 1 -> database versionconnCount
		udbOpenHepler = PolyvUDBOpenHelper.getInstance(context, 4);
	}

	public void addUploadFile(PolyvUploadInfo info) {
		db = udbOpenHepler.getWritableDatabase();
		String sql = "insert into uploadlist(vid,title,desc,filesize,filepath) values(?,?,?,?,?)";
		db.execSQL(sql, new Object[] { info.getVid(), info.getTitle(), info.getDesc(), info.getFilesize(),
				info.getFilepath() });
	}

	public void deleteUploadFile(PolyvUploadInfo info) {
		db = udbOpenHepler.getWritableDatabase();
		String sql = "delete from uploadlist where vid=?";
		db.execSQL(sql, new Object[] { info.getVid() });
	}

	public void updatePercent(PolyvUploadInfo info, long percent, long total) {
		db = udbOpenHepler.getWritableDatabase();
		String sql = "update uploadlist set percent=?,total=? where vid=?";
		db.execSQL(sql, new Object[] { percent, total, info.getVid() });
	}

	public boolean isAdd(PolyvUploadInfo info) {
		db = udbOpenHepler.getWritableDatabase();
		String sql = "select vid ,title,desc from uploadlist where vid=?";
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(sql, new String[] { info.getVid() });
			if (cursor.getCount() == 1) {
				return true;
			} else {
				String sql1 = "select vid from uploadlist";
				Cursor cursor1 = null;
				try {
					cursor1 = db.rawQuery(sql1, null);
					String vid = null;
					String nsufName = new File(info.getVid()).getName();
					while (cursor1.moveToNext()) {
						vid = cursor1.getString(cursor1.getColumnIndex("vid"));
						String osufName = new File(vid).getName();
						if (nsufName.equals(osufName))
							return true;
					}
				} finally {
					if (cursor1 != null)
						cursor1.close();
				}
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return false;
	}

	public LinkedList<PolyvUploadInfo> getUploadFiles() {
		LinkedList<PolyvUploadInfo> infos = new LinkedList<PolyvUploadInfo>();
		db = udbOpenHepler.getWritableDatabase();
		String sql = "select vid,title,filepath,desc,filesize,percent,total from uploadlist";
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				String vid = cursor.getString(cursor.getColumnIndex("vid"));
				String title = cursor.getString(cursor.getColumnIndex("title"));
				String filepath = cursor.getString(cursor.getColumnIndex("filepath"));
				String desc = cursor.getString(cursor.getColumnIndex("desc"));
				long filesize = cursor.getLong(cursor.getColumnIndex("filesize"));
				long percent = cursor.getInt(cursor.getColumnIndex("percent"));
				long total = cursor.getInt(cursor.getColumnIndex("total"));
				PolyvUploadInfo info = new PolyvUploadInfo(vid, title, desc);
				info.setPercent(percent);
				info.setTotal(total);
				info.setFilepath(filepath);
				info.setFilesize(filesize);
				infos.addLast(info);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return infos;
	}
}
