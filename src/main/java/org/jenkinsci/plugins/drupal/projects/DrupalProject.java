package org.jenkinsci.plugins.drupal.projects;

import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.Project;
import jenkins.model.Jenkins;

/**
 * Drupal project (top level item).
 * 
 * @author Fengtan https://github.com/Fengtan/
 *
 */
public class DrupalProject extends Project<DrupalProject, DrupalBuild> implements TopLevelItem {

	public DrupalProject(ItemGroup parent, String name) {
		super(parent, name);
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
		// TODO description does not seem to show up
		// TODO add builders
		
		public DrupalProject newInstance(ItemGroup parent, String name) {
			return new DrupalProject(parent, name);
		}
		
	}

}
