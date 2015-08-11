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

import net.sf.json.JSONObject;

import org.jenkinsci.plugins.drupal.beans.DrushInvocation;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Install a Drupal instance.
 * 
 * @author Fengtan https://github.com/fengtan/
 *
 */
public class DrupalInstanceBuilder extends Builder {

    public final String db;
    public final String root;
    public final String profile;
    public final boolean refresh;
    public final boolean updb;
    
    @DataBoundConstructor
    public DrupalInstanceBuilder(String db, String root, String profile, boolean refresh, boolean updb) {
    	this.db = db;
        this.root = root;
        this.profile = profile;
        this.refresh = refresh;
        this.updb = updb;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    	// Create Drupal installation if needed.
    	DrushInvocation drush = new DrushInvocation(new FilePath(new File(root)), build.getWorkspace(), launcher, listener);
    	if (refresh || !drush.status()) {
    		listener.getLogger().println("[DRUPAL] No Drupal installation detected, installing Drupal...");
    		drush.siteInstall(db, profile);	
    	} else {
    		listener.getLogger().println("[DRUPAL] Drupal is already installed, skipping installation");
    	}
    	
    	// Run update.php if needed.
    	if (updb) {
    		drush.upDb();
    	}
    	
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
         * Human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Build a Drupal instance";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
        }
        
        /**
         * Field 'db' should not be empty.
         */
        public FormValidation doCheckDb(@QueryParameter String value) {
            if (value.length() == 0) {
              return FormValidation.error("Please set a database URL");
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

    }
}

