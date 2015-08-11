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

package org.jenkinsci.plugins.drupal.scm;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import hudson.scm.SCM;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.drupal.beans.DrushInvocation;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Checkout Drupal source code based on a Drush Makefile. 
 * 
 * @author Fengtan https://github.com/fengtan/
 *
 */
public class DrushMakefileSCM extends SCM {

    // Save Makefile data into this file.  
	private static final String MAKEFILE_FILE = "drupal.make";
	
	private final String makefile;
	private final String root;
	
	@DataBoundConstructor
	public DrushMakefileSCM(String makefile, String root) {
		this.makefile = makefile;
		this.root = root;
	}
	
	public String getMakefile() {
		return makefile;
	}
	
	public String getRoot() {
		return root;
	}
		
    @Override
    public PollingResult compareRemoteRevisionWith(Job<?,?> project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState _baseline) {
        return PollingResult.NO_CHANGES;
    }
	
    @Override
    public void checkout(Run<?,?> build, Launcher launcher, FilePath workspace, TaskListener listener, File changelogFile, SCMRevisionState baseline) throws IOException, InterruptedException {
	    // If necessary, delete destination directory so we can install Drupal (unless Drupal root is workspace root).
	    File rootDir = new File(workspace.getRemote(), root);
	    FilePath rootPath = new FilePath(rootDir);
	    if (rootDir.exists() && !rootPath.getRemote().equals(workspace.getRemote())) {
		    listener.getLogger().println("[DRUPAL] Deleting destination directory "+rootDir.getAbsolutePath());
		    // Make sure drupal/sites/defaults is writable so we can delete its contents.
		    File defaultDir = new File(rootDir, "sites/default");
		    defaultDir.setWritable(true);
		    FileUtils.deleteDirectory(rootDir);
	    }

	    // Save Makefile into local file.
	    File makefileFile = new File(workspace.getRemote(), MAKEFILE_FILE);
	    listener.getLogger().println("[DRUPAL] Saving Makefile into "+makefileFile.getAbsolutePath());
	    FileUtils.writeStringToFile(makefileFile, makefile);

	    // Make Drupal.
	    DrushInvocation drush = new DrushInvocation(rootPath, workspace, launcher, listener);
	    drush.make(makefileFile);
    }
	
    @Override
    public ChangeLogParser createChangeLogParser() {
	   return null;
    }
	
    @Extension
    public static class DescriptorImpl extends SCMDescriptor {
        /**
         * Load the persisted global configuration.
         */
        public DescriptorImpl() {
            super(DrushMakefileSCM.class, null);
            load();
        }
        
        /**
         * Human readable name is used in the configuration screen.
         */
		@Override
		public String getDisplayName() {
		    return "Drush Makefile";
		}
		
		/**
         * Field 'makefile' should not be empty.
         */
        public FormValidation doCheckMakefile(@QueryParameter String value) {
            if (value.length() == 0) {
              return FormValidation.error("Please set a Makefile");
            }
            return FormValidation.ok();
        }
		
        /**
         * Field 'root' cannot be empty ('drush make' expects an empty directory so workspace root cannot be used as Drupal root).
         */
        public FormValidation doCheckRoot(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
        	if (value.length() == 0) {
            	return FormValidation.error("Please set a Drupal root");
            }
        	return FormValidation.ok();
        }
		
	}
	
}
