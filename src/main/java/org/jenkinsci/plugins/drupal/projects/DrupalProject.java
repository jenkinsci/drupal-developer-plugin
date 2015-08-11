/*
 * Copyright (c) 2015 Fengtan<https://github.com/fengtan/>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jenkinsci.plugins.drupal.projects;

import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.Project;
import hudson.plugins.checkstyle.CheckStylePublisher;
import hudson.tasks.junit.JUnitResultArchiver;

import java.io.IOException;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.drupal.builders.CoderReviewBuilder;
import org.jenkinsci.plugins.drupal.builders.DrupalInstanceBuilder;
import org.jenkinsci.plugins.drupal.builders.SimpletestBuilder;
import org.jenkinsci.plugins.drupal.scm.DrushMakefileSCM;

/**
 * Drupal project (top level item).
 * 
 * @author Fengtan https://github.com/fengtan/
 *
 */
public class DrupalProject extends Project<DrupalProject, DrupalBuild> implements TopLevelItem {
	
    private static final Logger LOGGER = Logger.getLogger(DrupalProject.class.getName());
	
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
		
		@Override
		public DrupalProject newInstance(ItemGroup parent, String name) {
			DrupalProject project = new DrupalProject(parent, name);
			
			// Add SCM.
			try {
				project.setScm(new DrushMakefileSCM("api=2\r\ncore=7.x\r\nprojects[drupal][version]=7.38", "drupal"));
			} catch (IOException e) {
				LOGGER.warning("[DRUPAL] Unable to instantiate Makefile SCM: "+e.toString());
			}

			// Add builders.
			project.getBuildersList().add(new DrupalInstanceBuilder("mysql://user:password@localhost/db", "drupal", "standard", false, false));
			project.getBuildersList().add(new CoderReviewBuilder(true, true, true, true, true, "drupal", "logs_codereview", "", false));
			project.getBuildersList().add(new SimpletestBuilder("http://localhost/", "drupal", "logs_tests", "", ""));
			
			// Add publishers.
			project.getPublishersList().add(new CheckStylePublisher("", "", "low", "", false, "", "", "0", "", "", "", "", "", "", "0", "", "", "", "", "", "", false, false, false, false, false, "logs_codereview/*"));
			project.getPublishersList().add(new JUnitResultArchiver("logs_tests/*"));

			return project;
		}
		
	}

}
