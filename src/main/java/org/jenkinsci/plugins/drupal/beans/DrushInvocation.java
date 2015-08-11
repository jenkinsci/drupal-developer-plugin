/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Fengtan<https://github.com/fengtan/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jenkinsci.plugins.drupal.beans;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import hudson.util.StreamTaskListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.drupal.config.DrushInstallation;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Invoke Drush commands.
 * 
 * @author Fengtan https://github.com/fengtan/
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
	 * Get a map of projects installed on Drupal, keyed by machine name.
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
	 * Get a list of test classes available.
	 */
	public Collection<DrupalTest> getTests() {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("test-run").add("--format=json");

		OutputStream json = new ByteArrayOutputStream();
		try {
			execute(args, new StreamTaskListener(json));
		} catch (IOException e1) {
			listener.getLogger().println(e1);
			return CollectionUtils.EMPTY_COLLECTION;
		} catch (InterruptedException e2) {
			listener.getLogger().println(e2);
			return CollectionUtils.EMPTY_COLLECTION;
		}
		
		Collection<DrupalTest> tests = new HashSet<DrupalTest>();
		JSONArray entries = (JSONArray) JSONValue.parse(json.toString());
		if (entries == null) {
			listener.getLogger().println("[DRUPAL] Could not list available tests");
			return CollectionUtils.EMPTY_COLLECTION;
		}
		for (Object entry: entries) {
			JSONObject test = (JSONObject) entry;
			tests.add(new DrupalTest(test.get("group").toString(), test.get("class").toString()));
		}
		
		return tests;
	}
	
	/**
	 * Run tests.
	 */
	public boolean testRun(File outputDir, String uri, Collection<String> targets) throws IOException, InterruptedException {
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("test-run");
		args.add("--xml="+outputDir.getAbsolutePath());
		if (StringUtils.isNotEmpty(uri)) {
			args.add("--uri="+uri);
		}
		if (CollectionUtils.isEmpty(targets)) {
			args.add("--all");
		} else {
			args.add(StringUtils.join(targets, ","));
		}
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
