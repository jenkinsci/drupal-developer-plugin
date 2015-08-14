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

package org.jenkinsci.plugins.drupal.builders;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.drupal.beans.DrupalTest;
import org.jenkinsci.plugins.drupal.beans.DrushInvocation;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Run Simpletest on Drupal.
 * 
 * @author Fengtan https://github.com/fengtan/
 *
 */
public class DrupalTestsBuilder extends Builder {

    public final String uri;
    public final String root;
    public final String logs;
    public final String exceptGroups;
    public final String exceptClasses;

    @DataBoundConstructor
    public DrupalTestsBuilder(String uri, String root, String logs, String exceptGroups, String exceptClasses) {
        this.uri = uri;
        this.root = root;
        this.logs = logs;
        this.exceptGroups = exceptGroups;
        this.exceptClasses = exceptClasses;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    	// Make sure logs directory exists.
    	File logsDir = new File(build.getWorkspace().getRemote(), logs);
    	if (!logsDir.exists()) {
    		listener.getLogger().println("[DRUPAL] Creating logs directory "+logs);
    		logsDir.mkdir();
    	}

    	// Enable Simpletest if necessary.
    	File rootDir = new File(build.getWorkspace().getRemote(), root);
    	DrushInvocation drush = new DrushInvocation(new FilePath(rootDir), build.getWorkspace(), launcher, listener);
    	if (drush.isModuleInstalled("simpletest", true)) {
    		listener.getLogger().println("[DRUPAL] Simpletest is already enabled");
    	} else {
    		listener.getLogger().println("[DRUPAL] Simpletest is not enabled. Enabling Simpletest...");
    		drush.enable("simpletest");
    	}
    	
    	// Filter out excluded test groups/classes if necessary.
    	final List<String> targets = new ArrayList<String>();
    	if (StringUtils.isNotEmpty(exceptGroups) || StringUtils.isNotEmpty(exceptClasses)) {
    		final Collection<String> groups = Arrays.asList(StringUtils.split(exceptGroups.toLowerCase(), ","));
    		final Collection<String> classes = Arrays.asList(StringUtils.split(exceptClasses.toLowerCase(), ","));
    		CollectionUtils.forAllDo(drush.getTests(), new Closure() {
				@Override
				public void execute(Object input) {
					DrupalTest test = (DrupalTest) input;
					if (!groups.contains(test.getGroup().toLowerCase()) && !classes.contains(test.getClassName().toLowerCase())) {
						targets.add(test.getClassName());	
					}
				}
			});
    	}
    	Collections.sort(targets);
    	
    	// Run Simpletest.
    	drush.testRun(logsDir, uri, targets);

    	return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * Load the persisted global configuration.
         */
        public DescriptorImpl() {
            load();
        }
        
        /**
         * This builder can be used with all kinds of project types.
         */
        public boolean isApplicable(Class<? extends AbstractProject> aClass) { 
            return true;
        }

        /**
         * Human readable name used in the configuration screen.
         */
        public String getDisplayName() {
            return "Run tests on Drupal";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
        }
        
        /**
         * Field 'uri' should not be empty.
         */
        public FormValidation doCheckUri(@QueryParameter String value) throws IOException {
            if (value.length() == 0) {
            	return FormValidation.error("Please set a URI");
            }
            return FormValidation.ok();
        }

        /**
         * Field 'root' should be a valid directory.
         */
        public FormValidation doCheckRoot(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            if (value.length() == 0) {
            	return FormValidation.warning("Workspace root will be used as Drupal root");
            }
            if (project != null) {
                return FilePath.validateFileMask(project.getSomeWorkspace(), value);
            }
        	return FormValidation.ok();
        }
        
        /**
         * Field 'logs' should not be empty.
         */
        public FormValidation doCheckLogs(@QueryParameter String value) throws IOException {
            if (value.length() == 0) {
            	return FormValidation.error("Please set a logs directory");
            }
            return FormValidation.ok();
        }
        
    }
}

