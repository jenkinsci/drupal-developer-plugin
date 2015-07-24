package org.jenkinsci.plugins.drupal.projects;

import hudson.model.Build;

import java.io.File;
import java.io.IOException;

/**
 * Drupal build.
 * 
 * @author Fengtan https://github.com/Fengtan/
 *
 */
public class DrupalBuild extends Build<DrupalProject, DrupalBuild> {

	public DrupalBuild(DrupalProject project) throws IOException {
		super(project);
	}
	
	public DrupalBuild(DrupalProject project, File buildDir) throws IOException {
		super(project, buildDir);
	}

}
