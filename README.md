[Jenkins](https://jenkins-ci.org/) plugin to [review code](https://www.drupal.org/project/coder) and [run tests](https://www.drupal.org/simpletest) on [Drupal](https://www.drupal.org/).

TODO screenshot

#### Quick start

 * TODO
 * Install Checkstyle, Junit, SCM API, (github api ?) and this plugin using the release page
 * Install Drush 7.0.0-rc2 globally
 * Create a database
 * Create a fresh Drupal project, update database connection string, build project
 * Might want to have a look at detailed steps, especially for web server config

#### Compilation

 * `git clone https://github.com/fengtan/drupal-plugin`
 * `cd drupal-plugin/`
 * `git checkout tags/0.1`
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

##### Create Local Database

 * TODO mysql commands (user jenkins)
 * TODO unless sqlite
 * TODO Install Drush ?, possibly using system config page with shell installer:
VERSION=7.0.0-rc2
curl -sSL https://github.com/drush-ops/drush/archive/$VERSION.tar.gz | tar xz --strip-components=1
curl -sSL https://getcomposer.org/installer | php
php composer.phar install
  * tool dir -> '.'
  * see http://docs.drush.org/en/master/install/
  * global install is preferred

##### Create Project

 * Create a new Freestyle project
 * TODO (skip most of config below except web server config, and also makefile is not ideal) Top-level item creates a ready-to-use project to review code and run tests on a vanilla Drupal core (just set the DB URL). Just update the SCM info to run it on your code - use Makefile or any other SCM like [Git](https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin) or [Subversion](https://wiki.jenkins-ci.org/display/JENKINS/Subversion+Plugin)

##### Configure Source Code Management
 * Checkout a Drupal codebase
 * TODO ideally in a subdirectory
 * TODO git example (drupal.git)
 * TODO Makefile example
 * TODO subversion example (subdirectory)
 * TODO Multiple-SCMs example
 1. TODO if you have Drupal in your codebase, then checkout into workspace root
 2. TODO if you don't, then checkout Drupal and your codebase using the [Multiple SCMs Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Multiple+SCMs+Plugin) (more efficient) or using the 'Drush Makefile' SCM

##### Configure Local Web Server

 * Might want to run Apache / php web server for tests as recommended by simpletest
 * Apache config example, should point at Drupal root (possibly workspace root)

##### Configure Builds

 * TODO each build/field
 * TODO configure Mysql
 * Note that core simpletests run forever. you can monitor progress on /console ; might want to skip core tests
 * TODO common values for codereview except (sites/all/modules/contrib etc) + simpletest except
 * Simpletest URI should match web server config
 * TODO link to @ignore system

##### Configure Reports
 
 * TODO checkstyle + junit

##### Build Project

 * Click 'build now', after some time graphs should show up

#### Dependencies

 * [Checkstyle Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Checkstyle+Plugin)
 * [JUnit Plugin](https://wiki.jenkins-ci.org/display/JENKINS/JUnit+Plugin)
 * [SCM API Plugin](https://wiki.jenkins-ci.org/display/JENKINS/SCM+API+Plugin)
 * [Drush](http://www.drush.org/en/master/install/) TODO version
 * TODO github api
 * TODO either Apache or PHP server

#### Troubleshooting

 * Plugin installed but does not show up => make sure dependencies are installed
 * Check /var/log/jenkins/jenkins.log
 * Check console output (http://localhost:8080/job/<myjob>/<id>/console)
 * Make sure you use the last version of dependencies
 * Drupal 7.x
