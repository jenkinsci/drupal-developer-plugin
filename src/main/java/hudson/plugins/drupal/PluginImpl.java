package hudson.plugins.drupal;

import hudson.Plugin;

import java.util.logging.Logger;

/**
 * Entry point of a plugin.
 * 
 * <p>
 * There must be one {@link Plugin} class in each plugin. See javadoc of
 * {@link Plugin} for more about what can be done on this class.
 * 
 * @author Fengtan<https://github.com/Fengtan/>
 */
public class PluginImpl extends Plugin {

  private transient static final Logger logger = Logger.getLogger("jenkins.plugins.drupal");

  // TODO this class is not necessary
  public void start() {
	  logger.info("Starting Drupal plugin");
  }
  
}
