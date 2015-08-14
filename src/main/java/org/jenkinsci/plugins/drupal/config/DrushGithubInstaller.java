package org.jenkinsci.plugins.drupal.config;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.model.Node;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolInstallerDescriptor;
import hudson.tools.ToolInstallation;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Install Drush from Github.
 * 
 * @author Fengtan https://github.com/fengtan/
 *
 */
public class DrushGithubInstaller extends ToolInstaller {

	private static final String DRUSH_GITHUB_REPO = "drush-ops/drush";
	
	private final String version;
	
	@DataBoundConstructor
	public DrushGithubInstaller(String version) {
		super(null);
		this.version = version;
	}

	// TODO what if drush globally installed
	// TODO what if change drush version
	@Override
	public FilePath performInstallation(ToolInstallation tool, Node node, TaskListener listener) throws IOException, InterruptedException {
		FilePath expectedLocation = preferredLocation(tool, node);
		FilePath marker = expectedLocation.child(".installedByHudson");
		
		// Check if tool is already installed.
		if (marker.exists() && marker.readToString().equals(version)) {
			return expectedLocation;
		}
		
		// Prepare installation.
		expectedLocation.deleteRecursive();
		expectedLocation.mkdirs();
		URL url = null; // TODO
		FilePath file = expectedLocation.child("TODO"); // TODO
		file.copyFrom(url);
		
		// Install tool.
		install(file.getRemote(), listener);
			
		// Clean up and set marker.
		file.delete();
		marker.write(version, null);			
		
		return expectedLocation;
	}
	
	/**
	 * Install Drush locally.
	 */
	public void install(String archivePath, TaskListener listener) {
		listener.getLogger().println("[DRUPAL] Installing Drush "+version+" locally...");
		// TODO
	}
	
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}
	
	@Extension
	public static final class DescriptorImpl extends ToolInstallerDescriptor<ToolInstaller> {
		
        /**
         * Load the persisted global configuration.
         */
		public DescriptorImpl() {
			load();
		}
		
        /**
         * Human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "Install from Github";
        }
        
        @Override
        public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
        	return true; // TODO return toolType==DrushGithubInstaller.class
        }

        // TODO 7.0.0-rc2 should be selected by default
        
    	/**
    	 * List available Drush releases.
    	 */
    	public List<GHRelease> getInstallableReleases() throws IOException {
    		GitHub github = GitHub.connectAnonymously();
    		GHRepository repo = github.getRepository(DRUSH_GITHUB_REPO);
    		return repo.listReleases().asList();
    	}
		
	}

}
