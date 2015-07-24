package org.jenkinsci.plugins.drupal.beans;

/**
 * Drupal extension (theme/module).
 * 
 * 
 * @author Fengtan https://github.com/Fengtan/
 *
 */
public class DrupalExtension {

	private String name;
	private String type;
	private String status;
	private String version;
	
	public DrupalExtension(String name, String type, String status, String version) {
		this.name = name;
		this.type = type;
		this.status = status;
		this.version = version;
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public String getStatus() {
		return status;
	}
	
	public String getVersion() {
		return version;
	}
	
}
