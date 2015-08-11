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
import hudson.init.Initializer;
import hudson.model.EnvironmentSpecific;
import hudson.model.TaskListener;
import hudson.model.Node;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolProperty;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.util.FormValidation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public static transient final String DEFAULT = "Default";
    
    private static final Logger LOGGER = Logger.getLogger(DrushInstallation.class.getName());

    /**
     * Get Drush executable.
     */
    public String getDrushExe() {
        return getHome();
    }

    /**
     * Return all installations.
     */
    private static DrushInstallation[] getInstallations(DescriptorImpl descriptor) {
    	DrushInstallation[] installations = null;
        try {
            installations = descriptor.getInstallations();
        } catch (NullPointerException e) {
            installations = new DrushInstallation[0];
        }
        return installations;
    }

    /**
     * Return the default installation.
     */
    public static DrushInstallation getDefaultInstallation() {
        DescriptorImpl drushTools = Jenkins.getInstance().getDescriptorByType(DrushInstallation.DescriptorImpl.class);
        DrushInstallation tool = drushTools.getInstallation(DrushInstallation.DEFAULT);
        if (tool != null) {
            return tool;
        } else {
        	DrushInstallation[] installations = drushTools.getInstallations();
            if (installations.length > 0) {
                return installations[0];
            } else {
                onLoaded();
                return drushTools.getInstallations()[0];
            }
        }
    }
    
	@Override
	public DrushInstallation forEnvironment(EnvVars environment) {
        return new DrushInstallation(getName(), environment.expand(getHome()), Collections.<ToolProperty<?>>emptyList());
	}

	@Override
	public DrushInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new DrushInstallation(getName(), translateFor(node, log), Collections.<ToolProperty<?>>emptyList());
	}

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    @Initializer(after=EXTENSIONS_AUGMENTED)
    public static void onLoaded() {
        // Create default tool installation if needed. Uses "drush" or migrates data from previous versions.
        DescriptorImpl descriptor = (DescriptorImpl) Jenkins.getInstance().getDescriptor(DrushInstallation.class);
        DrushInstallation[] installations = getInstallations(descriptor);

        if (installations != null && installations.length > 0) {
            //No need to initialize if there's already something.
            return;
        }

        String defaultDrushExe = Functions.isWindows() ? "drush.bat" : "drush";
        DrushInstallation tool = new DrushInstallation(DEFAULT, defaultDrushExe, Collections.<ToolProperty<?>>emptyList());
        descriptor.setInstallations(new DrushInstallation[] { tool });
        descriptor.save();
    }

    @Extension
    public static class DescriptorImpl extends ToolDescriptor<DrushInstallation> {

        /**
         * Load the persisted global configuration.
         */
        public DescriptorImpl() {
        	super();
            load();
        }
        
        /**
         * Human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "Drush";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            setInstallations(req.bindJSONToList(clazz, json.get("tool")).toArray(new DrushInstallation[0]));
            save();
            return true;
        }
        
        /**
         * Executable should be a valid path.
         */
        public FormValidation doCheckHome(@QueryParameter File value) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            String path = value.getPath();
            return FormValidation.validateExecutable(path);
        }
        
        /**
         * Get Drush installation.
         */
        public DrushInstallation getInstallation(String name) {
            for(DrushInstallation i : getInstallations()) {
                if(i.getName().equals(name)) {
                    return i;
                }
            }
            if (name.length() > 0) {
                LOGGER.log(Level.WARNING, "invalid drushTool selection {0}", name);
            }
            return null;
        }

        /**
         * Get all Drush installations.
         */
        public List<ToolDescriptor<? extends DrushInstallation>> getApplicableDesccriptors() {
            List<ToolDescriptor<? extends DrushInstallation>> r = new ArrayList<ToolDescriptor<? extends DrushInstallation>>();
            for (ToolDescriptor td : Jenkins.getInstance().<ToolInstallation,ToolDescriptor<?>>getDescriptorList(ToolInstallation.class)) {
                if (DrushInstallation.class.isAssignableFrom(td.clazz))
                    r.add(td);
            }
            return r;
        }

    }


}
