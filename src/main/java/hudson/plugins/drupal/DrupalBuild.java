package hudson.plugins.drupal;

import hudson.model.Build;

import java.io.IOException;

public class DrupalBuild extends Build<DrupalProject, DrupalBuild> {

	protected DrupalBuild(DrupalProject project) throws IOException {
		super(project);
	}

}
