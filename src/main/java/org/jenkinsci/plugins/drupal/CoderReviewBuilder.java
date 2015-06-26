package org.jenkinsci.plugins.drupal;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link CoderReviewBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Fengtan
 */
public class CoderReviewBuilder extends Builder {

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
    	logsDir.mkdir(); // TODO what if already exists

    	// Run Coder Review.
    	final File rootDir = new File(build.getWorkspace().getRemote(), root);
    	DrushInvocation drush = new DrushInvocation(rootDir, build, launcher, listener);
  		// TODO do not download module is already exists -- makes the task slow
		drush.download("coder-7.x-2.5"); // TODO coder version should be selectable from UI
		drush.enable("coder_review"); // TODO unless already enabled
		
		Collection<String> reviews = new HashSet<String>();
		// TODO any chance to have Jelly return directly a Set ?
		if (this.style) reviews.add("style");
		if (this.comment) reviews.add("comment");
		if (this.sql) reviews.add("sql");
		if (this.security) reviews.add("security");
		if (this.i18n) reviews.add("i18n");
		
		// Get all projects.
		Collection<DrupalProject> projectsAvailable = drush.getProjects();

		CollectionUtils.transform(arg0, arg1);

		// Remove projects the user wants to exclude.
		FileSet fileSet = Util.createFileSet(rootDir, StringUtils.join(projectsAvailable, ","), except);
		DirectoryScanner scanner = fileSet.getDirectoryScanner();
		String[] projectsReviewed = scanner.getIncludedFiles();

final Logger l = Logger.getLogger(CoderReviewBuilder.class.getName()); // TODO tmp
l.warning("projectsReviewed: "+StringUtils.join(projectsReviewed, ", "));
l.warning("projectsAvailable: "+StringUtils.join(projectsAvailable, ", "));
		
		/*
		// TODO drop CollectionUtils from pom.xml ?
		CollectionUtils.filter(projects, new Predicate<DrupalProject>() {
			public boolean evaluate(DrupalProject project) {
				try {
					//FilePath projectFilePath = new FilePath(new File(rootDir, project.getFilename()));
					//String msg = projectFilePath.getParent().validateAntFileMask(except);
					//return (msg == null);
					
					
					return true;
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			}
		});
		*/

		// TODO drush.coderReview(logsDir, reviews, projects);

    	return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link CoderReviewBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/drupal/DrupalInstanceBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        // TODO doCheckRoot to make sure Drupal exists ?
        
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Run Coder Review on Drupal"; // TODO "Run code review on Drupal" ?
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
        }
    }
}

