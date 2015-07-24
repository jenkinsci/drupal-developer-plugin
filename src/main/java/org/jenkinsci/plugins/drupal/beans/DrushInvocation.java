package org.jenkinsci.plugins.drupal.beans;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import hudson.util.StreamTaskListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.drupal.config.DrushInstallation;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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
	 * Get a map of projects installed on Drupal.
	 */
	public Map<String, DrupalProject> getProjects(boolean modulesOnly, boolean enabledOnly) {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("pm-list").add("--pipe").add("--format=json");
		if (modulesOnly) {
			args.add("--type=module");
		}
		if (enabledOnly) {
			args.add("--status=enabled");
		}
		File jsonFile;
		try {
			// TODO piping the results of execute() into the JSON parser might be more efficient than using a temporary file.
			jsonFile = File.createTempFile("drupal", "projects");
			execute(args, new StreamTaskListener(jsonFile));
		} catch (IOException e1) {
			listener.getLogger().println(e1);
			return MapUtils.EMPTY_MAP;
		} catch (InterruptedException e2) {
			listener.getLogger().println(e2);
			return MapUtils.EMPTY_MAP;
		}
		
		Map<String, DrupalProject> projects = new HashMap<String, DrupalProject>();
		JSONObject entries;
		try {
			entries = (JSONObject) JSONValue.parse(new FileReader(jsonFile));
		} catch (FileNotFoundException e) {
			listener.getLogger().println(e);
			return MapUtils.EMPTY_MAP;
		}
		for (Object name: entries.keySet()) {
			JSONObject entry = (JSONObject) entries.get(name);
			DrupalProject project = new DrupalProject(name.toString(), entry.get("type").toString(), entry.get("status").toString(), entry.get("version").toString());
			projects.put(name.toString(), project);
		}
		
		return projects;
	}
	
	/**
	 * Check if a module exists / is enabled
	 */
	public boolean isModuleInstalled(String name, boolean enabledOnly) {
		return getProjects(true, enabledOnly).keySet().contains(name);
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
		// Make sure Coder is enabled.
		DrupalProject coder = getProjects(true, true).get("coder");
		if (coder == null) {
			listener.getLogger().println("[DRUPAL] Coder does not exist: aborting code review");
			return false;
		}
		
		// Build command depending on Coder's version.
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("coder-review");
		if (coder.getVersion().startsWith("7.x-2")) {
			args.add("--minor");
			args.add("--ignores-pass"); // TODO not always supported => expose to user ? "only if you use coder-7.x-XX+
			args.add("--checkstyle");
			args.add("--reviews="+StringUtils.join(reviews, ","));	
		} else if (coder.getVersion().startsWith("7.x-1")) {
			args.add("minor");
			args.add("checkstyle");
			for (String review: reviews) {
				args.add(review);
			}
		} else {
			listener.getLogger().println("[DRUPAL] Unsupported Coder version "+coder.getVersion());
			return false;
		}
		
		// 'drush coder-review comment' fails with error "use --reviews or --comment."
		// TODO find a workaround.
		// TODO same for i18n with coder-7.x-1.x ?
		for(String projectName: projectNames) {
			if (!projectName.equals("comment")) {
				args.add(projectName);
			}
		}
		
		// Run command.
    	File outputFile = new File(outputDir, "coder_review.xml");
    	return execute(args, new StreamTaskListener(outputFile));
	}

}
