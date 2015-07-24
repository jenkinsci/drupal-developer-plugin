package org.jenkinsci.plugins.drupal.beans;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import hudson.util.StreamTaskListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
	 * Run update.php.
	 */
	public boolean upDb() throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("updatedb");
		return execute(args);
	}
	
	/**
	 * Make a Drupal site using a Makefile.
	 */
	public boolean make(File makefile) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("make");
		args.add(makefile.getAbsolutePath());
		args.add(root.getRemote());
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
	public Map<String, DrupalExtension> getProjects(boolean modulesOnly, boolean enabledOnly) {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("pm-list").add("--pipe").add("--format=json");
		if (modulesOnly) {
			args.add("--type=module");
		}
		if (enabledOnly) {
			args.add("--status=enabled");
		}
		
		OutputStream json = new ByteArrayOutputStream();
		try {
			execute(args, new StreamTaskListener(json));
		} catch (IOException e1) {
			listener.getLogger().println(e1);
			return MapUtils.EMPTY_MAP;
		} catch (InterruptedException e2) {
			listener.getLogger().println(e2);
			return MapUtils.EMPTY_MAP;
		}		
		
		Map<String, DrupalExtension> projects = new HashMap<String, DrupalExtension>();
		JSONObject entries = (JSONObject) JSONValue.parse(json.toString());
		if (entries == null) {
			listener.getLogger().println("[DRUPAL] Could not list available projects");
			return MapUtils.EMPTY_MAP;
		}
		for (Object name: entries.keySet()) {
			JSONObject entry = (JSONObject) entries.get(name);
			DrupalExtension project = new DrupalExtension(name.toString(), entry.get("type").toString(), entry.get("status").toString(), entry.get("version").toString());
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
	 * Return true if the site is already installed, false otherwise.
	 */
	public boolean status() {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("status").add("--format=json");

		OutputStream json = new ByteArrayOutputStream();
		try {
			execute(args, new StreamTaskListener(json));
		} catch (IOException e1) {
			listener.getLogger().println(e1);
			return false;
		} catch (InterruptedException e2) {
			listener.getLogger().println(e2);
			return false;
		}

		JSONObject values = (JSONObject) JSONValue.parse(json.toString());
		if (values == null) {
			listener.getLogger().println("[DRUPAL] Could not determine the site status.");
			return false;
		}
		
		return values.containsKey("db-name");
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
		// TODO allow user to exclude classes/groups ? e.g. core tests
		args.add("--all");
		args.add("--xml="+outputDir.getAbsolutePath());
		return execute(args);
	}
	
	/**
	 * Run a code review.
	 */
	public boolean coderReview(File outputDir, Collection<String> reviews, final Collection<String> projectNames, boolean ignoresPass) throws IOException, InterruptedException {	
		// Make sure Coder is enabled.
		DrupalExtension coder = getProjects(true, true).get("coder");
		if (coder == null) {
			listener.getLogger().println("[DRUPAL] Coder does not exist: aborting code review");
			return false;
		}
		
		// Build command depending on Coder's version.
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("coder-review");
		if (coder.getVersion().startsWith("7.x-2")) {
			args.add("--minor");
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

		// Ignores pass if needed.
		// This option works only with coder-7.x-2.4+.
		if (ignoresPass) {
			if (coder.getVersion().startsWith("7.x-2") && (Integer.parseInt(coder.getVersion().replaceFirst("7\\.x-2\\.", "")) >= 4)) {
				args.add("--ignores-pass");	
			} else {
				listener.getLogger().println("[DRUPAL] 'Ignores pass' option is available only with Coder-7.x-2.4+, ignoring option");
			}
		}

		// In coder-7.x-2.x, 'drush coder-review comment' fails with error "use --reviews or --comment". Same for i18n.
		// Ignore projects involved in conflicts.
		Collection<String> conflicts = CollectionUtils.intersection(projectNames, reviews);
		if (!conflicts.isEmpty()) {
			listener.getLogger().println("[DRUPAL] Ignoring project(s) conflicting with Coder options: "+StringUtils.join(conflicts, ", "));
		}
		for (String projectName: projectNames) {
			if (!conflicts.contains(projectName)) {
				args.add(projectName);	
			}
		}

		// Run command.
    	File outputFile = new File(outputDir, "coder_review.xml");
    	return execute(args, new StreamTaskListener(outputFile));
	}

}
