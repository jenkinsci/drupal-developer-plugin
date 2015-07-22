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

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.drupal.beans.DrushInvocation;
import org.kohsuke.stapler.DataBoundConstructor;

public class DrushMakefileSCM extends SCM {

	private String type;
	private String makefilePath;
	private String makefileInput;
	private String root;
	
	// TODO if root is not specified, should be workspace root
	// TODO help "codebase will be fully recreated when makefile is updated"
	@DataBoundConstructor
	public DrushMakefileSCM(String type, String makefilePath, String makefileInput, String root) {
		this.type = type;
		this.makefilePath = makefilePath;
		this.makefileInput = makefileInput;
		this.root = root;
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
		// TODO check if we need to checkout something
		// TODO if (type has changed) return PollingResult.BUILD_NOW
		// TODO if (type is 'path' and makefile's modifieddate has changed) return PollingResult.BUILD_NOW
		// TODO if (type is 'path' and makefile's content has changed) return PollingResult.BUILD_NOW
		// TODO if (no config data was persisted in the past) return PollingResult.BUILD_NOW 
		// TODO log  all of this
		return PollingResult.NO_CHANGES;
	}

	@Override
	public void checkout(Run<?,?> build, Launcher launcher, FilePath workspace, TaskListener listener, File changelogFile, SCMRevisionState baseline) throws IOException, InterruptedException {
		DrushInvocation drush = new DrushInvocation(new FilePath(new File(root)), workspace, launcher, listener);
		if (StringUtils.equals(type, "input")) {
			// TODO create temporary file (or pipe, if possible)
		}
		drush.make(makefilePath, root);
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
