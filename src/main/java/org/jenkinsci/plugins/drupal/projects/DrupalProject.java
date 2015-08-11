/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Fengtan<https://github.com/fengtan/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
			project.getBuildersList().add(new CoderReviewBuilder(true, true, true, true, true, "drupal", "logs.coder", "", false));
			project.getBuildersList().add(new SimpletestBuilder("http://localhost/", "drupal", "logs.simpletest", "", ""));
			
			// Add publishers.
			project.getPublishersList().add(new CheckStylePublisher("", "", "low", "", false, "", "", "0", "", "", "", "", "", "", "0", "", "", "", "", "", "", false, false, false, false, false, "logs.coder/*"));
			project.getPublishersList().add(new JUnitResultArchiver("logs.simpletest/*"));

			return project;
		}
		
	}

}
