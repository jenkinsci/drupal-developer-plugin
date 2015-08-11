[Jenkins](https://jenkins-ci.org/) plugin to [review code](https://www.drupal.org/project/coder) and [run tests](https://www.drupal.org/simpletest) on [Drupal](https://www.drupal.org/).

This plugin brings 4 Jenkins components:
 * SCM 'Drush Makefile' allows to fetch code based on a [Makefile](https://www.drupal.org/node/1432374)
 * Builder 'Build a Drupal instance' allows to [install Drupal](https://www.drupal.org/documentation/install/developers) into a given database, based on code already fetched
 * Builder 'Review code on Drupal' brings a Jenkins interface for [Coder Review](https://www.drupal.org/project/coder)
 * Builder 'Run tests on Drupal' brings a Jenkins interface for [Simpletest](https://www.drupal.org/simpletest)

TODO screenshot

#### Compilation

 * `git clone https://github.com/fengtan/drupal-plugin`
 * `cd drupal-plugin/`
 * `mvn clean install -DskipTests=true`
 
Alternatively, download a pre-compiled .hpi archive from the [releases page] (https://github.com/fengtan/drupal-plugin/releases)

#### Installation

Install dependencies:
 * TODO http://localhost:8080/pluginManager/
 * TODO Junit installed in core jenkins ?

Assuming Jenkins runs on `http://localhost:8080/`:
 * `wget http://localhost:8080/jnlpJars/jenkins-cli.jar`
 * `java -jar jenkins-cli.jar -s http://localhost:8080/ install-plugin ./target/drupal.hpi -restart`

Alternatively:
 * Go to `http://localhost:8080/pluginManager/advanced`
 * Upload `./target/drupal.hpi`
 * Restart Jenkins

#### Usage

 * TODO
 * Install Drush, possibly using system config page
 * Run MySQL
 * Create a MySQL database
 * Top-level item creates a ready-to-use project to review code and run tests on a vanilla Drupal core (just set the DB URL). Just update the SCM info to run it on your code - use Makefile or any other SCM like [Git](https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin) or [Subversion](https://wiki.jenkins-ci.org/display/JENKINS/Subversion+Plugin)
 * Might want to run Apache / php web server for tests as recommended by simpletest
 * Note that core simpletests run forever. you can monitor progress on /console ; might want to skip core tests
 1. TODO if you have Drupal in your codebase, then checkout into workspace root
 2. TODO if you don't, then checkout Drupal and your codebase using the [Multiple SCMs Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Multiple+SCMs+Plugin) (more efficient) or using the 'Drush Makefile' SCM
 * If create as freestyle project, then configure checkstyle + junit plugins
 * TODO link to @ignore system
 * TODO common values for codereview except (sites/all/modules/contrib etc) + simpletest except

#### Dependencies

 * [Checkstyle Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Checkstyle+Plugin)
 * [JUnit Plugin](https://wiki.jenkins-ci.org/display/JENKINS/JUnit+Plugin)
 * [SCM API Plugin](https://wiki.jenkins-ci.org/display/JENKINS/SCM+API+Plugin)
 * [Drush](http://www.drush.org/en/master/install/) TODO version
 * TODO either Apache or PHP server

#### Troubleshooting

 * Plugin installed but does not show up => make sure dependencies are installed
 * Check /var/log/jenkins/jenkins.log
 * Check console output (http://localhost:8080/job/<myjob>/<id>/console)
 * Make sure you use the last version of dependencies
 * Drupal 7.x
