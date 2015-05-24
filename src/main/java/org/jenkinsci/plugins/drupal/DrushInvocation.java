package org.jenkinsci.plugins.drupal;

import java.io.IOException;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.util.ArgumentListBuilder;

public class DrushInvocation {

	protected final AbstractBuild<?, ?> build;
	protected final Launcher launcher;
	protected final BuildListener listener;
	
	protected final ArgumentListBuilder args = new ArgumentListBuilder();
	
	public DrushInvocation(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
		this.build = build;
		this.launcher = launcher;
		this.listener = listener;
	}
	
	public void setUri(String uri) {
		args.add("--uri").add(uri);
	}
	
	public boolean execute() throws IOException, InterruptedException {
        listener.getLogger().println("Hello !"); // TODO drop
        return (launcher.launch().pwd(build.getWorkspace()).cmds(args).stdout(listener).join() == 0);
	}

}
