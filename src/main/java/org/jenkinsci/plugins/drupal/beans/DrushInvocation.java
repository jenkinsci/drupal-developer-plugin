package org.jenkinsci.plugins.drupal.beans;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import hudson.util.StreamTaskListener;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;

/**
 * TODO do not download drupal root once this plugin is stable (user should be responsible for checking out drupal): https://wiki.jenkins-ci.org/display/JENKINS/Multiple+SCMs+Plugin
 */
public class DrushInvocation {

	protected final FilePath root;
	protected final FilePath workspace;
	protected final Launcher launcher;
	protected final TaskListener listener;
	
	// TODO document
	// TODO drop workspace and merge with root
	public DrushInvocation(FilePath root, FilePath workspace, Launcher launcher, TaskListener listener) {
		this.root = root;
		this.workspace = workspace;
		this.launcher = launcher;
		this.listener = listener;
		// TODO make sure drush is installed
	}
	
	protected ArgumentListBuilder getArgumentListBuilder() {
		return new ArgumentListBuilder("drush").add("--yes").add("--nocolor").add("--root="+root.getRemote());
	}
	
	protected boolean execute(ArgumentListBuilder args) throws IOException, InterruptedException {
		return execute(args, null);
	}

	// TODO global option to set the location of drush executable
	// TODO detect drush return codes ?
	protected boolean execute(ArgumentListBuilder args, TaskListener out) throws IOException, InterruptedException {
		ProcStarter starter = launcher.launch().pwd(workspace).cmds(args);
		if (out == null) {
			starter.stdout(listener);
		} else {
			// Do not display stderr since this breaks the XML formatting on stdout.
			starter.stdout(out).stderr(NullOutputStream.NULL_OUTPUT_STREAM);
		}
		starter.join();
		return true;
	}
	
	public boolean make(String makefile, String buildPath) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("make");
		args.add(makefile);
		args.add(buildPath);
		return execute(args);
	}
	
	
	public boolean siteInstall(String db, String profile) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("site-install");
		args.add(profile);
		args.add("--db-url="+db);
		return execute(args);
	}
	
	// TODO what if codebase already contains coder / has the wrong version of coder ? delete (mention in help)
	// TODO download coder using git ?
	public boolean download(String projects, String destination) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("pm-download").add(projects);
		if (StringUtils.isNotEmpty(destination)) {
			args.add("--destination="+destination);
		}
		return execute(args);
	}
	
	public boolean enable(String extensions) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("pm-enable").add(extensions);
		return execute(args);
	}

	public boolean testRun(File outputDir, String uri) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("test-run");
		if (StringUtils.isNotEmpty(uri)) {
			args.add("--uri="+uri);
		}
		
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
	 * @param projects Drupal projects to review
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean coderReview(File outputDir, Collection<String> reviews, Collection<String> projectNames) throws IOException, InterruptedException {
		// TODO offer more options to user (see drush help coder-review)
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("coder-review");
		args.add("--minor");
		args.add("--ignores-pass");
		args.add("--checkstyle");
		args.add("--reviews="+StringUtils.join(reviews, ","));
		for(String projectName: projectNames) {
			// drush coder-review comment ends up with error "use --reviews or --comment."
			// TODO find a workaround
			if (!projectName.equals("comment")) {
				args.add(projectName);
			}
		}
    	File outputFile = new File(outputDir, "coder_review.xml");
    	return execute(args, new StreamTaskListener(outputFile));
	}

}
