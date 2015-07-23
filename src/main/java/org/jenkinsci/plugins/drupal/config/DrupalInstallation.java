package org.jenkinsci.plugins.drupal.config;

import hudson.Extension;
import hudson.tools.ToolProperty;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;

import java.util.List;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class DrupalInstallation extends ToolInstallation {

    @DataBoundConstructor
    public DrupalInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }


    @Extension
    public static class DescriptorImpl extends ToolDescriptor<DrupalInstallation> {

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            super.configure(req, json);
            save();
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Drush";
        }
    }

}
