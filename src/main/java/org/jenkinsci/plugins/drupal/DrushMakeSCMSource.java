package org.jenkinsci.plugins.drupal;

import hudson.model.TaskListener;
import hudson.scm.SCM;

import java.io.IOException;

import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;

public class DrushMakeSCMSource extends SCMSource {

	protected DrushMakeSCMSource(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SCM build(SCMHead head, SCMRevision revision) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void retrieve(SCMHeadObserver observer, TaskListener listener) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		
	}

}
