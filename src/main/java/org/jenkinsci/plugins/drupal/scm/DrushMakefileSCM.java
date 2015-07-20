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

import org.jenkinsci.plugins.drupal.beans.DrushInvocation;
import org.kohsuke.stapler.DataBoundConstructor;

public class DrushMakefileSCM extends SCM {

	private String makefile; // Makefile path.
	private String root; // Drupal root path.
	
	// TODO if root is not specified, should be workspace root
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
		// TODO check if we need to checkout something
		return PollingResult.BUILD_NOW;
	}

	@Override
	public void checkout(Run<?,?> build, Launcher launcher, FilePath workspace, TaskListener listener, File changelogFile, SCMRevisionState baseline) throws IOException, InterruptedException {
		DrushInvocation drush = new DrushInvocation(new FilePath(new File(root)), workspace, launcher, listener);
		drush.make(makefile, root);
		// TODO drush remake if already exists
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

		@Override
		public String getDisplayName() {
		    return "Drush Makefile";
		}
	}
	
}
