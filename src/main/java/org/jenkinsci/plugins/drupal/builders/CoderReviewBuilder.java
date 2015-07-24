package org.jenkinsci.plugins.drupal.builders;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.drupal.beans.DrupalProject;
import org.jenkinsci.plugins.drupal.beans.DrushInvocation;
import org.jenkinsci.plugins.drupal.config.DrushInstallation;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Run Coder Review on Drupal.
 * 
 * @author Fengtan https://github.com/Fengtan/
 * 
 */
public class CoderReviewBuilder extends Builder {

	// 'drush dl coder' downloads coder-7.x-1.3 so we will explicitely use 'drush dl coder-7.x-2.5'.  
	private static final String CODER_RELEASE = "coder-7.x-2.5";
	
	public final boolean style;
	public final boolean comment;
	public final boolean sql;
	public final boolean security;
	public final boolean i18n;
	
	public final String root;
	public final String logs;
	public final String except;
	
    @DataBoundConstructor
    public CoderReviewBuilder(boolean style, boolean comment, boolean sql, boolean security, boolean i18n, String root, String logs, String except) {
    	this.style = style;
    	this.comment = comment;
    	this.sql = sql;
    	this.security = security;
    	this.i18n = i18n;
    	this.root = root;
    	this.logs = logs;
    	this.except = except;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    	// Make sure logs directory exists.
    	File logsDir = new File(build.getWorkspace().getRemote(), logs);
    	if (!logsDir.exists()) {
    		listener.getLogger().println("[DRUPAL] Creating logs directory "+logs);
    		logsDir.mkdir();
    	}
    	
    	// Download and enable Coder if necessary.
    	final File rootDir = new File(build.getWorkspace().getRemote(), root);
    	DrushInvocation drush = new DrushInvocation(new FilePath(rootDir), build.getWorkspace(), launcher, listener);
    	if (drush.isModuleInstalled("coder", false)) {
    		listener.getLogger().println("[DRUPAL] Coder already exists");
    	} else {
    		listener.getLogger().println("[DRUPAL] Coder does not exist. Downloading Coder...");
    		drush.download(CODER_RELEASE, "modules");
    	}
    	if (drush.isModuleInstalled("coder_review", true)) {
    		listener.getLogger().println("[DRUPAL] Coder is already enabled");
    	} else {
    		listener.getLogger().println("[DRUPAL] Coder is not enabled. Enabling Coder...");
    		drush.enable("coder_review");
    	}
		
		Collection<String> reviews = new HashSet<String>();
		if (this.style)    reviews.add("style");
		if (this.comment)  reviews.add("comment");
		if (this.sql)      reviews.add("sql");
		if (this.security) reviews.add("security");
		if (this.i18n)     reviews.add("i18n");

		// Remove projects the user wants to exclude.
		// **/*.info matches all modules, themes and installation profiles.
		// Installation profiles cannot be reviewed and will be just ignored by Coder.
		FileSet fileSet = Util.createFileSet(rootDir, "**/*.info", except);
		DirectoryScanner scanner = fileSet.getDirectoryScanner();
		Collection<String> projects = Arrays.asList(scanner.getIncludedFiles());

		// Transform sites/all/modules/mymodule/mymodule.module into mymodule.
		CollectionUtils.transform(projects, new Transformer<String, String>() {
			@Override
			public String transform(String project) {
				return FilenameUtils.getBaseName(project);
			}
		});

		// Run code review.
		drush.coderReview(logsDir, reviews, projects);

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
            return "Run Coder Review on Drupal";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
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
            	return FormValidation.error("Please set a logs directory.");
            }
            return FormValidation.ok();
        }

    }
}

