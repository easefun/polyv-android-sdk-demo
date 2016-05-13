package com.easefun.polyvsdk.demo.upload;

public class PolyvUploadInfo {
	private String vid;
	private String title;
	private String filepath;
	private long filesize;
	private long total;
	private String desc;
	private long percent;
	public PolyvUploadInfo(String vid, String title, String desc) {
		this.vid = vid;
		this.title = title;
		this.desc = desc;
	}
	public String getVid() {
		return vid;
	}
	public void setVid(String vid) {
		this.vid = vid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getFilepath() {
		return filepath;
	}
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	public long getFilesize() {
		return filesize;
	}
	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public long getPercent() {
		return percent;
	}
	public void setPercent(long percent) {
		this.percent = percent;
	}
	@Override
	public String toString() {
		return "UploadInfo [vid=" + vid + ", title=" + title + ", filepath=" + filepath + ", filesize=" + filesize
				+ ", total=" + total + ", desc=" + desc + ", percent=" + percent + "]";
	}

}
