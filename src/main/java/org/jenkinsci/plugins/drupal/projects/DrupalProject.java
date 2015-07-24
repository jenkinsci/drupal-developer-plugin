package org.jenkinsci.plugins.drupal.projects;

import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.Project;
import hudson.plugins.checkstyle.CheckStylePublisher;
import hudson.tasks.Builder;
import hudson.tasks.junit.JUnitResultArchiver;

import java.util.ArrayList;
import java.util.List;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.drupal.builders.CoderReviewBuilder;
import org.jenkinsci.plugins.drupal.builders.DrupalInstanceBuilder;
import org.jenkinsci.plugins.drupal.builders.SimpletestBuilder;

/**
 * Drupal project (top level item).
 * 
 * @author Fengtan https://github.com/Fengtan/
 *
 */
public class DrupalProject extends Project<DrupalProject, DrupalBuild> implements TopLevelItem {

	public DrupalProject(ItemGroup parent, String name) {
		super(parent, name);
		getPublishersList().add(new CheckStylePublisher("", "", "low", "", false, "", "", "0", "", "", "", "", "", "", "0", "", "", "", "", "", "", false, false, false, false, false, "logs.coder/*"));
		getPublishersList().add(new JUnitResultArchiver("logs.simpletest/*"));
		// TODO test unstable/fail thresholds
		// TODO move into this.getPublishersList() { super() }
		// TODO analysis collector ?
		// TODO make sure dependency shows up in UI when installing module
	}
	
	@Override
	protected Class<DrupalBuild> getBuildClass() {
		return DrupalBuild.class;
	}
	
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(getClass());
	}
	
	@Override
	public List<Builder> getBuilders() {
		List<Builder> builders = new ArrayList<Builder>();
		builders.add(new DrupalInstanceBuilder("mysql://user:password@localhost/db", "drupal", "standard", false, false));
		builders.add(new CoderReviewBuilder(true, true, true, true, true, "drupal", "logs.coder", "", false)); // TODO profiles/** ?
		builders.add(new SimpletestBuilder("http://localhost/", "drupal", "logs.simpletest"));
		return builders;
		// TODO add Makefile / git checkout drupal
	}
	
	@Extension
	public static final class DescriptorImpl extends AbstractProjectDescriptor {
		
        /**
         * Human readable name used in the configuration screen.
         */
		public String getDisplayName() {
			return "Drupal project";
		}
		// TODO description does not seem to show up
		
		@Override
		public DrupalProject newInstance(ItemGroup parent, String name) {
			return new DrupalProject(parent, name);
		}
		
	}

}
