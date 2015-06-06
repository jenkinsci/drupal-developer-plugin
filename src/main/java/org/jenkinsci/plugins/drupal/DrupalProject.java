package org.jenkinsci.plugins.drupal;

/**
 * Drupal project (module or theme).
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
	
	public String getName() {
		return name;
	}
	
}
