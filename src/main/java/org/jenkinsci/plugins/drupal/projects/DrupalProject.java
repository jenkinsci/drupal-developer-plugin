package org.jenkinsci.plugins.drupal.projects;

import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.AbstractProject.AbstractProjectDescriptor;
import hudson.model.Project;
import hudson.plugins.checkstyle.CheckStylePublisher;
import hudson.tasks.junit.JUnitResultArchiver;
import jenkins.model.Jenkins;

import org.jenkinsci.plugins.drupal.builders.CoderReviewBuilder;
import org.jenkinsci.plugins.drupal.builders.DrupalInstanceBuilder;
import org.jenkinsci.plugins.drupal.builders.SimpletestBuilder;
import org.jenkinsci.plugins.drupal.scm.DrushMakefileSCM;

/**
 * Drupal project (top level item).
 * 
 * @author Fengtan https://github.com/Fengtan/
 *
 */
public class DrupalProject extends Project<DrupalProject, DrupalBuild> implements TopLevelItem {

	public DrupalProject(ItemGroup parent, String name) {
		super(parent, name);

		// Add SCM.
		// getSCMs().add(new DrushMakefileSCM("api=2&#xD;core=7.x&#xD;projects[drupal][version]=7.38", "drupal")); // TODO GitSCM ?
		
		// Add builders.
		getBuildersList().add(new DrupalInstanceBuilder("mysql://user:password@localhost/db", "drupal", "standard", false, false));
		getBuildersList().add(new CoderReviewBuilder(true, true, true, true, true, "drupal", "logs.coder", "", false));
		getBuildersList().add(new SimpletestBuilder("http://localhost/", "drupal", "logs.simpletest"));
		
		// Add publishers.
		getPublishersList().add(new CheckStylePublisher("", "", "low", "", false, "", "", "0", "", "", "", "", "", "", "0", "", "", "", "", "", "", false, false, false, false, false, "logs.coder/*"));
		getPublishersList().add(new JUnitResultArchiver("logs.simpletest/*"));
		// TODO make sure dependency shows up in UI when installing module
		// TODO project settings are not persisted
	}
	
	@Override
	protected Class<DrupalBuild> getBuildClass() {
		return DrupalBuild.class;
	}
	
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(getClass());
	}
	
	@Extension
	public static final class DescriptorImpl extends AbstractProjectDescriptor {
		
        /**
         * Human readable name used in the configuration screen.
         */
		public String getDisplayName() {
			return "Drupal project";
		}
		
		@Override
		public DrupalProject newInstance(ItemGroup parent, String name) {
			return new DrupalProject(parent, name);
		}
		
	}

}
