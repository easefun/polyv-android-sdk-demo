package com.easefun.polyvsdk.demo.upload;

import java.util.HashMap;
import java.util.Map;

import com.easefun.polyvsdk.upload.PolyvUploader;
import com.easefun.polyvsdk.upload.PolyvUploaderManager;

/**
 * id——Uploader管理类
 *
 */
public class PolyvIdUploaderManager {
	private static final Map<Integer, PolyvUploader> iddownloader = new HashMap<Integer, PolyvUploader>();

	public static Map<Integer, PolyvUploader> getAllIdUploader() {
		return iddownloader;
	}

	public static PolyvUploader getIdUploader(int id) {
		return iddownloader.get(id);
	}

	public static void removeIdUploader(int id) {
		if (iddownloader.containsKey(id)) {
			iddownloader.remove(id);
		}
	}

	public static void addIdUploader(final String filePath, final String title, final String desc) {
		int id = PolyvULNotificationService.getId(filePath);
		PolyvUploader polyvUploader = PolyvUploaderManager.getPolyvUploader(filePath, title, desc);
		if (!iddownloader.containsKey(id))
			iddownloader.put(id, polyvUploader);
	}
}
