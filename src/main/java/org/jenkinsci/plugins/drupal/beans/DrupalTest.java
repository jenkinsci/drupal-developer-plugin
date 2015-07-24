package org.jenkinsci.plugins.drupal.beans;


/**
 * Drupal test class.
 * 
 * 
 * @author Fengtan https://github.com/Fengtan/
 *
 */
public class DrupalTest {

	private String group;
	private String className;
	
	public DrupalTest(String group, String className) {
		this.group = group;
		this.className = className;
	}
	
	public String getGroup() {
		return group;
	}
	
	public String getClassName() {
		return className;
	}
	
}
