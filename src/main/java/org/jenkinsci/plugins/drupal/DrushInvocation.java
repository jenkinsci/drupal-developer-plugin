package org.jenkinsci.plugins.drupal;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

public class DrushInvocation {

	protected final AbstractBuild<?, ?> build;
	protected final Launcher launcher;
	protected final BuildListener listener;
	
	public DrushInvocation(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
		this.build = build;
		this.launcher = launcher;
		this.listener = listener;
	}
	
	public boolean execute() {
        listener.getLogger().println("Hello !");
        return true;
	}
	
}
