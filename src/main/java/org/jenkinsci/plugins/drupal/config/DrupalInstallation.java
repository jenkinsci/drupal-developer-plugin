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

public class DrupalInstallation extends ToolInstallation implements NodeSpecific<DrupalInstallation>, EnvironmentSpecific<DrupalInstallation> {

    @DataBoundConstructor
    public DrupalInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    /** Constant <code>DEFAULT="Default"</code> */
    public static transient final String DEFAULT = "Default";
    
    private static final Logger LOGGER = Logger.getLogger(DrupalInstallation.class.getName());


    /**
     * getDrushExe.
     *
     * @return {@link java.lang.String} that will be used to execute drush (e.g. "drush" or "/usr/bin/drush")
     */
    public String getDrushExe() {
        return getHome();
    }

    private static DrupalInstallation[] getInstallations(DescriptorImpl descriptor) {
    	DrupalInstallation[] installations = null;
        try {
            installations = descriptor.getInstallations();
        } catch (NullPointerException e) {
            installations = new DrupalInstallation[0];
        }
        return installations;
    }

    /**
     * Returns the default installation.
     *
     * @return default installation
     */
    public static DrupalInstallation getDefaultInstallation() {
        DescriptorImpl drushTools = Jenkins.getInstance().getDescriptorByType(DrupalInstallation.DescriptorImpl.class);
        DrupalInstallation tool = drushTools.getInstallation(DrupalInstallation.DEFAULT);
        if (tool != null) {
            return tool;
        } else {
        	DrupalInstallation[] installations = drushTools.getInstallations();
            if (installations.length > 0) {
                return installations[0];
            } else {
                onLoaded();
                return drushTools.getInstallations()[0];
            }
        }
    }
    
	@Override
	public DrupalInstallation forEnvironment(EnvVars environment) {
        return new DrupalInstallation(getName(), environment.expand(getHome()), Collections.<ToolProperty<?>>emptyList());
	}

	@Override
	public DrupalInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new DrupalInstallation(getName(), translateFor(node, log), Collections.<ToolProperty<?>>emptyList());
	}

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    @Initializer(after=EXTENSIONS_AUGMENTED)
    public static void onLoaded() {
        //Creates default tool installation if needed. Uses "drush" or migrates data from previous versions

        DescriptorImpl descriptor = (DescriptorImpl) Jenkins.getInstance().getDescriptor(DrupalInstallation.class);
        DrupalInstallation[] installations = getInstallations(descriptor);

        if (installations != null && installations.length > 0) {
            //No need to initialize if there's already something
            return;
        }

        String defaultDrushExe = Functions.isWindows() ? "drush.bat" : "drush";
        DrupalInstallation tool = new DrupalInstallation(DEFAULT, defaultDrushExe, Collections.<ToolProperty<?>>emptyList());
        descriptor.setInstallations(new DrupalInstallation[] { tool });
        descriptor.save();
    }

    @Extension
    public static class DescriptorImpl extends ToolDescriptor<DrupalInstallation> {

        public DescriptorImpl() {
        	super();
            load();
        }
        
        @Override
        public String getDisplayName() {
            return "Drush";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            setInstallations(req.bindJSONToList(clazz, json.get("tool")).toArray(new DrupalInstallation[0]));
            save();
            return true;
        }
        
        public FormValidation doCheckHome(@QueryParameter File value) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            String path = value.getPath();

            return FormValidation.validateExecutable(path);
        }
        
        public DrupalInstallation getInstallation(String name) {
            for(DrupalInstallation i : getInstallations()) {
                if(i.getName().equals(name)) {
                    return i;
                }
            }
            if (name.length() > 0) {
                LOGGER.log(Level.WARNING, "invalid drushTool selection {0}", name);
            }
            return null;
        }

        public List<ToolDescriptor<? extends DrupalInstallation>> getApplicableDesccriptors() {
            List<ToolDescriptor<? extends DrupalInstallation>> r = new ArrayList<ToolDescriptor<? extends DrupalInstallation>>();
            for (ToolDescriptor td : Jenkins.getInstance().<ToolInstallation,ToolDescriptor<?>>getDescriptorList(ToolInstallation.class)) {
                if (DrupalInstallation.class.isAssignableFrom(td.clazz))
                    r.add(td);
            }
            return r;
        }

    }


}
