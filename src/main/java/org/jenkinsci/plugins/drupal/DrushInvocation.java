package org.jenkinsci.plugins.drupal;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.util.ArgumentListBuilder;
import hudson.util.StreamTaskListener;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;

/**
 * TODO do not download drupal root once this plugin is stable (user should be responsible for checking out drupal): https://wiki.jenkins-ci.org/display/JENKINS/Multiple+SCMs+Plugin
 */
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
		// TODO find a way to display stderr in console
		launcher.launch().pwd(build.getWorkspace()).cmds(args).stdout(out).stderr(NullOutputStream.NULL_OUTPUT_STREAM).join();
		return true; // TODO detect drush return codes
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
		args.add("--uri="+uri); // TODO if user did not provide uri, then do not set --uri
		
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
		// TODO add more options to user (see drush help coder-review)
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("coder-review");
		args.add("--minor");
		args.add("--ignores-pass");
		args.add("--checkstyle");
		args.add("--reviews="+StringUtils.join(reviews, ",")); // TODO pom.xml apache stringutils
		for(String projectName: projectNames) {
			// drush coder-review comment ends up with error "use --reviews or --comment."
			// TODO find a workaround
			if (!projectName.equals("comment")) {
				args.add(projectName);
			}
		}
    	File outputFile = new File(outputDir, "coder_review.xml"); // TODO let user set output file
		return execute(args, new StreamTaskListener(outputFile));
	}
	
	/* TODO drop (unused)
	public Collection<DrupalProject> getProjects() throws IOException, InterruptedException {
		File file = new File("/tmp/modules"); // TODO do not use an intermediate file
		file.delete(); // Make sure file does not already exists TODO do not use intermediate file
		
		ArgumentListBuilder args = getArgumentListBuilder();
		args.add("sql-query");
		args.add("select filename, name from system order by filename into outfile '/tmp/modules' fields terminated  by ',' enclosed by '\"' lines terminated by '\\n'"); // TODO do not dump into intermediate file TODO does this work with mariadb, psql etc
		execute(args); // TODO check result of execute()

		Collection<DrupalProject> projects = new HashSet<DrupalProject>();
		CSVParser parser = CSVParser.parse(file, Charset.defaultCharset(), CSVFormat.DEFAULT);
		for (CSVRecord project : parser) {
			projects.add(new DrupalProject(project.get(0).toString(), project.get(1).toString()));
		}

		return projects;
	}
	
	class DrupalProject {
	
		private String filename;
		private String name;
		
		public DrupalProject(String filename, String name) {
			this.filename = filename;
			this.name = name;
		}
		
		public String getFilename() {
			return filename;
		}
		
		public String getName() {
			return name;
		}
		
	}
	
	*/

}
