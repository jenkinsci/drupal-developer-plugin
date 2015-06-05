package org.jenkinsci.plugins.drupal;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.util.ArgumentListBuilder;
import hudson.util.StreamTaskListener;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;

public class DrushInvocation {

	protected final File root;
	protected final AbstractBuild<?, ?> build;
	protected final Launcher launcher;
	protected final BuildListener listener;
	
	// TODO document
	public DrushInvocation(File root, AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
		this.root = root;
		this.build = build;
		this.launcher = launcher;
		this.listener = listener;
		// TODO make sure drush is installed
	}
	
	protected ArgumentListBuilder getArgumentListBuilder() {
		return new ArgumentListBuilder("drush").add("--yes").add("--nocolor").add("--verbose").add("--root="+root.getAbsolutePath());
	}
	
	protected boolean execute(ArgumentListBuilder args) throws IOException, InterruptedException {
		return execute(args, listener);
	}

	protected boolean execute(ArgumentListBuilder args, TaskListener out) throws IOException, InterruptedException {
		// Do not display stderr since this breaks the XML formatting on stdout.
		// TODO pom.xml dependency on apache commons ? NullOutputStream
		launcher.launch().pwd(build.getWorkspace()).cmds(args).stdout(out).stderr(NullOutputStream.NULL_OUTPUT_STREAM).join();
		return true; // TODO detect drush return codes
	}

	public boolean siteInstall(String db) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("site-install");
		args.add("--db-url="+db);
		return execute(args);
	}
	
	// TODO what if codebase already contains coder / has the wrong version of coder
	public boolean download(String projects) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("pm-download").add(projects);
		// Downloading Drupal generates a folder "drupal-x-y". We want a folder simply named "drupal".
		if (projects.equals("drupal")) {
			args.add("--drupal-project-rename=drupal");
		}
		return execute(args);
	}
	
	public boolean enable(String extensions) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("pm-enable").add(extensions);
		return execute(args);
	}

	public boolean testRun(String uri, File outputDir) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("test-run");
		args.add("--uri="+uri);
		
		// TODO
		// args.add("--all");
		args.add("--methods=testSettingsPage");
		args.add("AggregatorConfigurationTestCase");
		
		args.add("--xml="+outputDir.getAbsolutePath());
		return execute(args);
	}
	
	/**
	 * 
	 * @param outputDir
	 * @param reviews See drush coder-review --reviews (set of i18n, style, etc)
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean coderReview(File outputDir, Set reviews) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("coder-review");
		args.add("--minor");
		args.add("--ignores-pass");
		args.add("--checkstyle");
		args.add("--reviews="+StringUtils.join(reviews, ",")); // TODO pom.xml apache stringutils
    	File outputFile = new File(outputDir, "coder_review.xml"); // TODO let user set output file
		return execute(args, new StreamTaskListener(outputFile));
	}

}
