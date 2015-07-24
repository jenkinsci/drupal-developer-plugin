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
 * @author Fengtan https://github.com/Fengtan/
 *
 */
public class DrupalInstanceBuilder extends Builder {

    public final String db;
    public final String root;
    public final String profile;
    public final boolean refresh;
    
    // TODO separate plugin to run php webserver:
    // TODO - explain https://www.drupal.org/project/php_server, "you can also run apache on the same server and point at workspace", "uri option should match"
    // TODO - explain must be a different port for each job ; web server will run only during -->
    // TODO - make sure PHP 5.4 exists ; make sure not a well known port ; not a used port ; not empty -->
    // TODO - another default port at random ?
    @DataBoundConstructor
    public DrupalInstanceBuilder(String db, String root, String profile, boolean refresh) {
    	this.db = db;
        this.root = root;
        this.profile = profile;
        this.refresh = refresh;
    }

    // TODO allow to run drush updb if we don't re-install the site for every build
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    	DrushInvocation drush = new DrushInvocation(new FilePath(new File(root)), build.getWorkspace(), launcher, listener);
    	if (refresh || !drush.status()) {
    		listener.getLogger().println("[DRUPAL] No Drupal installation detected, installing Drupal...");
    		drush.siteInstall(db, profile);	
    	} else {
    		listener.getLogger().println("[DRUPAL] Drupal is already installed, skipping installation");
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

