package org.jenkinsci.plugins.drupal.beans;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import hudson.util.StreamTaskListener;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.drupal.config.DrushInstallation;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * Invoke Drush commands.
 * 
 * @author Fengtan https://github.com/Fengtan/
 *
 */
public class DrushInvocation {

	protected final FilePath root;
	protected final FilePath workspace;
	protected final Launcher launcher;
	protected final TaskListener listener;
	
	public DrushInvocation(FilePath root, FilePath workspace, Launcher launcher, TaskListener listener) {
		this.root = root;
		this.workspace = workspace;
		this.launcher = launcher;
		this.listener = listener;
	}

	/**
	 * Get default Drush options.
	 */
	protected ArgumentListBuilder getArgumentListBuilder() {
		String drushExe = DrushInstallation.getDefaultInstallation().getDrushExe();
		return new ArgumentListBuilder(drushExe).add("--yes").add("--nocolor").add("--root="+root.getRemote());
	}
	
	/**
	 * Execute a Drush command.
	 */
	protected boolean execute(ArgumentListBuilder args) throws IOException, InterruptedException {
		return execute(args, null);
	}

	// TODO test when workspace root = drupal root
	/**
	 * Execute a Drush command.
	 */
	protected boolean execute(ArgumentListBuilder args, TaskListener out) throws IOException, InterruptedException {
		ProcStarter starter = launcher.launch().pwd(workspace).cmds(args);
		if (out == null) {
			// Output stdout/stderr into listener.
			starter.stdout(listener);
		} else {
			// Output stdout into out.
			// Do not output stderr since this breaks the XML formatting on stdout.
			starter.stdout(out).stderr(NullOutputStream.NULL_OUTPUT_STREAM);
		}
		starter.join();
		return true;
	}
	
	/**
	 * Make a Drupal site using a Makefile.
	 */
	public boolean make(String makefile, String buildPath) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("make");
		args.add(makefile);
		args.add(buildPath);
		return execute(args);
	}
	
	/**
	 * Install a Drupal site using an installation profile.
	 */
	public boolean siteInstall(String db, String profile) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("site-install");
		args.add(profile);
		args.add("--db-url="+db);
		return execute(args);
	}
	
	/**
	 * Download projects/modules into a destination directory.
	 */
	public boolean download(String projects, String destination) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("pm-download").add(projects);
		if (StringUtils.isNotEmpty(destination)) {
			args.add("--destination="+destination);
		}
		return execute(args);
	}
	
	/**
	 * Enable extensions/modules.
	 */
	public boolean enable(String extensions) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("pm-enable").add(extensions);
		return execute(args);
	}
	
	/**
	 * Get a list of projects installed on Drupal.
	 */
	public Collection<DrupalProject> getProjects(boolean modulesOnly, boolean enabledOnly) throws IOException, InterruptedException, ParseException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("pm-list").add("--pipe").add("--format=json");
		if (modulesOnly) {
			args.add("--type=module");
		}
		if (enabledOnly) {
			args.add("--status=enabled");
		}
		File tmpFile = new File("/tmp/modules.json"); // TODO use Jenkins API for temporary files ? use listener output ?
		execute(args, new StreamTaskListener(tmpFile));
		
		Collection<DrupalProject> projects = new HashSet<DrupalProject>();
		JSONObject entries = (JSONObject) JSONValue.parse(new FileReader(tmpFile));
		for (Object name: entries.keySet()) {
			JSONObject entry = (JSONObject) entries.get(name);
			DrupalProject project = new DrupalProject(name.toString(), entry.get("type").toString(), entry.get("status").toString(), entry.get("version").toString());
			projects.add(project);
		}
		
		return projects;
	}
	
	/**
	 * Run tests.
	 */
	public boolean testRun(File outputDir, String uri) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("test-run");
		if (StringUtils.isNotEmpty(uri)) {
			args.add("--uri="+uri);
		}
		
		// TODO-0 args.add("--all");
		args.add("--methods=testSettingsPage");
		args.add("AggregatorConfigurationTestCase");
		
		args.add("--xml="+outputDir.getAbsolutePath());
		return execute(args);
	}
	
	/**
	 * Run a code review.
	 */
	public boolean coderReview(File outputDir, Collection<String> reviews, Collection<String> projectNames) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("coder-review");
		args.add("--minor");
		args.add("--ignores-pass");
		args.add("--checkstyle");
		args.add("--reviews="+StringUtils.join(reviews, ","));
		for(String projectName: projectNames) {
			// drush coder-review comment ends up with error "use --reviews or --comment."
			// TODO-0 find a workaround
			if (!projectName.equals("comment")) {
				args.add(projectName);
			}
		}
    	File outputFile = new File(outputDir, "coder_review.xml");
    	return execute(args, new StreamTaskListener(outputFile));
	}

}
