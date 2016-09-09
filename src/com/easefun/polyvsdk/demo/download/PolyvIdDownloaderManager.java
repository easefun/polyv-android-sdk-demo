package com.easefun.polyvsdk.demo.download;

import java.util.HashMap;
import java.util.Map;

import com.easefun.polyvsdk.PolyvDownloader;
import com.easefun.polyvsdk.PolyvDownloaderManager;

/**
 * id——Downloader管理类
 */
public class PolyvIdDownloaderManager {
	private static final Map<Integer, PolyvDownloader> iddownloader = new HashMap<Integer, PolyvDownloader>();

	public static Map<Integer, PolyvDownloader> getAllIdDownloader(){
		return iddownloader;
	}
	
	public static PolyvDownloader getIdDownloader(int id) {
		return iddownloader.get(id);
	}

	public static void removeIdDownloader(int id) {
		if (iddownloader.containsKey(id)) {
			iddownloader.remove(id);
		}
	}
	
	public static void addIdDownloader(String vid,int bitRate){
		int id = PolyvDLNotificationService.getId(vid, bitRate);
		PolyvDownloader polyvDownloader=PolyvDownloaderManager.getPolyvDownloader(vid, bitRate);
		if(!iddownloader.containsKey(id))
			iddownloader.put(id, polyvDownloader);
	}
}
