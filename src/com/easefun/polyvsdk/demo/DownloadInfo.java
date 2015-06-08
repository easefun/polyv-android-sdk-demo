package com.easefun.polyvsdk.demo;

public class DownloadInfo {
    private String vid;
    private String duration;
    private int filesize;
    
    public DownloadInfo(){
    	
    }
    
	public DownloadInfo(String vid, String duration, int filesize) {
		this.vid = vid;
		this.duration = duration;
		this.filesize = filesize;
	}
	public String getVid() {
		return vid;
	}
	public void setVid(String vid) {
		this.vid = vid;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public int getFilesize() {
		return filesize;
	}
	public void setFilesize(int filesize) {
		this.filesize = filesize;
	}
	@Override
	public String toString() {
		return "DownloadInfo [vid=" + vid + ", duration=" + duration
				+ ", filesize=" + filesize + "]";
	}
}
