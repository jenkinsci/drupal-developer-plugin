package org.jenkinsci.plugins.drupal;

import hudson.FilePath;

import java.io.File;

/**
 * Drupal project (module or theme).
 * 
 * TODO do we need this class ?
 * 
 * @author Fengtan
 *
 */
public class DrupalProject {

	private String filename;
	private String name;
	
	/**
	 * 
	 * @param filename Project filename, as in {system}.filename.
	 * @param name Project name, as in {system}.name.
	 */
	public DrupalProject(String filename, String name) {
		this.filename = filename;
		this.name = name;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getName() {
		return name;
	}
	
}
