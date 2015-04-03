package hudson.plugins.drupal;

import java.io.IOException;

import hudson.model.Build;

public class DrupalBuild extends Build<DrupalProject, DrupalBuild> {

	protected DrupalBuild(DrupalProject project) throws IOException {
		super(project);
	}

}
