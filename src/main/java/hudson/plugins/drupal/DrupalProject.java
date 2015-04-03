package hudson.plugins.drupal;

import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.model.Project;
import jenkins.model.Jenkins;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

public class DrupalProject extends Project<DrupalProject, DrupalBuild> implements TopLevelItem {

	public DrupalProject(ItemGroup parent, String name) {
		super(parent, name);
	}

	public TopLevelItemDescriptor getDescriptor() {
		return (TopLevelItemDescriptor) Jenkins.getInstance().getDescriptorOrDie(getClass());
	}

	protected Class<DrupalBuild> getBuildClass() {
		return DrupalBuild.class;
	}
	
    @Restricted(NoExternalUse.class)
    @Extension(ordinal=700)
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends AbstractProjectDescriptor {
        public String getDisplayName() {
            return "Drupal project";
        }

        public DrupalProject newInstance(ItemGroup parent, String name) {
            return new DrupalProject(parent,name);
        }
    }

}
