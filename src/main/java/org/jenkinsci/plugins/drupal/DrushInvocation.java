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
	
	public DrushInvocation(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
		this.build = build;
		this.launcher = launcher;
		this.listener = listener;
		// TODO make sure drush is installed
	}
	
	protected ArgumentListBuilder getArgumentListBuilder() {
		return new ArgumentListBuilder("drush").add("--yes").add("--nocolor");
	}
	
	protected boolean execute(ArgumentListBuilder args) throws IOException, InterruptedException {
		// TODO detect drush return codes
		return (launcher.launch().pwd(build.getWorkspace()).cmds(args).stdout(listener).join() == 0);
	}

	public boolean siteInstall(String db) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("site-install");
		args.add("--db-url="+db);
		return execute(args);
	}
	
	// TODO what if codebase already contains coder / has the wrong version of coder
	public boolean download(String module) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("pm-download").add(module);
		return execute(args);
	}
	
	public boolean enable(String module) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("pm-enable").add(module);
		return execute(args);
	}
	
	public boolean coderReview() throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("coder-review");
		return execute(args);
	}
	
	public boolean testRun(String uri) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("test-run");
		args.add("--uri="+uri);
		return execute(args);
	}

}
