/*
 * Copyright (c) 2015 Fengtan<https://github.com/fengtan/>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jenkinsci.plugins.drupal.config;

import static hudson.init.InitMilestone.EXTENSIONS_AUGMENTED;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.init.Initializer;
import hudson.model.EnvironmentSpecific;
import hudson.model.TaskListener;
import hudson.model.Node;
import hudson.remoting.Callable;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolProperty;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Handle Drush installations.
 * 
 * @author Fengtan https://github.com/fengtan/
 *
 */
public class DrushInstallation extends ToolInstallation implements NodeSpecific<DrushInstallation>, EnvironmentSpecific<DrushInstallation> {

    @DataBoundConstructor
    public DrushInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    @Override
    public void buildEnvVars(EnvVars env) {
        env.put("DRUSH_HOME", getHome());
    }

    /**
     * Get executable path of this Drush installation on the given target system.
     */
    public String getExecutable(Launcher launcher) throws IOException, InterruptedException {
        return launcher.getChannel().call(new Callable<String,IOException>() {
            public String call() throws IOException {
                File exe = getExeFile();
                return exe.exists() ? exe.getPath() : null;
            }
        });
    }

    /**
     * Get executable file.
     */
    private File getExeFile() {
        String execName = Functions.isWindows() ? "drush.bat" : "drush";
        String home = Util.replaceMacro(getHome(), EnvVars.masterEnvVars);
        return new File(home, execName);
    }

    /**
     * Check if the executable exists.
     */
    public boolean getExists() throws IOException, InterruptedException {
        return getExecutable(new Launcher.LocalLauncher(TaskListener.NULL)) != null;
    }

    @Override
    public DrushInstallation forEnvironment(EnvVars environment) {
        return new DrushInstallation(getName(), environment.expand(getHome()), getProperties().toList());
    }

    @Override
    public DrushInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new DrushInstallation(getName(), translateFor(node, log), getProperties().toList());
    }
    
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }
    
    @Initializer(after=EXTENSIONS_AUGMENTED)
    public static void onLoaded() {
    	// Create default tool installation if needed.
    	DescriptorImpl descriptor = (DescriptorImpl) Jenkins.getInstance().getDescriptor(DrushInstallation.class);
    	DrushInstallation[] installations = descriptor.getInstallations();
    
		// No need to initialize if there's already something.
    	if (installations != null && installations.length > 0) {
    		return;
    	}

    	DrushInstallation tool = new DrushInstallation("Default", "", Collections.<ToolProperty<?>>emptyList());
    	descriptor.setInstallations(new DrushInstallation[] { tool });
    	descriptor.save();
    }

    @Extension
    public static class DescriptorImpl extends ToolDescriptor<DrushInstallation> {

        @Override
        public String getDisplayName() {
            return "Drush";
        }

        @Override
        public List<? extends ToolInstaller> getDefaultInstallers() {
            return Collections.EMPTY_LIST;
        }

        /**
         * Load the persisted global configuration.
         */
        public DescriptorImpl() {
        	super();
        	load();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            setInstallations(req.bindJSONToList(clazz, json.get("tool")).toArray(new DrushInstallation[0]));
            save();
            return true;
        }

        /**
         * Installation name should not be empty.
         */
        public FormValidation doCheckName(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }
        
    }

}