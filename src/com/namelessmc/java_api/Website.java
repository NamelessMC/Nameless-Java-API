package com.namelessmc.java_api;

public class Website {
	
	private String version;
	private Update update;
	private String[] modules;
	
	Website(String version, Update update, String[] modules){
		this.version = version;
		this.update = update;
		this.modules = modules;
	}
	
	public String getVersion() {
		return version;
	}
	
	/**
	 * @return Information about an update, or null if no update is available.
	 */
	public Update getUpdate() {
		return update;
	}
	
	public String[] getModules() {
		return modules;
	}
	
	public static class Update {
		
		private boolean isUrgent;
		private String version;
		
		Update(boolean isUrgent, String version){
			this.isUrgent = isUrgent;
			this.version = version;
		}
		
		public boolean isUrgent() {
			return isUrgent;
		}
		
		public String getVersion() {
			return version;
		}
		
	}

}
