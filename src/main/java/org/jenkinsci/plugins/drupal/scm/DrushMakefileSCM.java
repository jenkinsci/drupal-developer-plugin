package org.jenkinsci.plugins.drupal.scm;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import hudson.scm.SCM;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.drupal.beans.DrushInvocation;
import org.kohsuke.stapler.DataBoundConstructor;

public class DrushMakefileSCM extends SCM {

	private String root;
	private String type;
	private String makefilePath;
	private String makefileInput;
	
	// TODO if root is not specified, should be workspace root
	// TODO-0 help "codebase will be fully recreated when makefile is updated"
	@DataBoundConstructor
	public DrushMakefileSCM(String root, String type, String makefilePath, String makefileInput) {
		this.root = root;
		this.type = type;
		this.makefilePath = makefilePath;
		this.makefileInput = makefileInput;
	}
	
	public String getType() {
		return type;
	}
	
	public String getMakefilePath() {
		return makefilePath;
	}
	
	public String getMakefileInput() {
		return makefileInput;
	}
	
	public String getRoot() {
		return root;
	}
	
	@Override
	public PollingResult compareRemoteRevisionWith(Job<?,?> project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState _baseline) {
		// TODO does not seem to be called
		// TODO compare with _baseline ?

		// TODO-0 support drush remake ?
		// TODO support when Makefile type==input
		// TODO support when Makefile is http remote
		// If Drupal root does not exist, then build now.
		File rootDir = new File(workspace.getRemote(), root);
		if (!rootDir.exists()) {
			return PollingResult.BUILD_NOW;
		}
		
		// If Makefile was modified after Drupal root was created, then rebuild.
		File makefile = new File(workspace.getRemote(), makefilePath);
		if (makefile.lastModified() > rootDir.lastModified()) {
			return PollingResult.BUILD_NOW;
		}
		
		// TODO log  all of this
		return PollingResult.NO_CHANGES;
	}

	@Override
	public void checkout(Run<?,?> build, Launcher launcher, FilePath workspace, TaskListener listener, File changelogFile, SCMRevisionState baseline) throws IOException, InterruptedException {
		// If necessary, delete destination directory so we can install Drupal.
		// TODO unless root = workspace home ?
		File rootDir = new File(workspace.getRemote(), root);
		if (rootDir.exists()) {
			// Make sure drupal/sites/defaults is writable so we can delete its contents.
			File defaultDir = new File(rootDir, "sites/default");
			listener.getLogger().println("Deleting destination directory "+rootDir.getAbsolutePath());
			defaultDir.setWritable(true);
			FileUtils.deleteDirectory(rootDir);	
		}
		
		// Make Drupal.
		DrushInvocation drush = new DrushInvocation(new FilePath(rootDir), workspace, launcher, listener);
		if (StringUtils.equals(type, "input")) {
			// TODO create temporary file (or pipe, if possible)
		}
		drush.make(makefilePath, root);
	}
	
	@Override
	public ChangeLogParser createChangeLogParser() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Extension
    public static class DescriptorImpl extends SCMDescriptor {

        public DescriptorImpl() {
            super(DrushMakefileSCM.class, null);
            load();
        }

		// TODO-0 validate that makefile exists
        
		@Override
		public String getDisplayName() {
		    return "Drush Makefile";
		}
	}
	
}
