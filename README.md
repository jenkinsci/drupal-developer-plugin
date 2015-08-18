[Jenkins](https://jenkins-ci.org/) plugin to [review code](https://www.drupal.org/project/coder) and [run tests](https://www.drupal.org/simpletest) on [Drupal](https://www.drupal.org/).

#### Screenshots

![alt tag](https://raw.github.com/fengtan/drupal-plugin/master/screenshot_admin.png)
![alt tag](https://raw.github.com/fengtan/drupal-plugin/master/screenshot_trends.png)

#### Quick start

 * Install [Drush](http://docs.drush.org/en/master/install/) 7.0.0-rc2 globally
 * Install plugins [Checkstyle](https://wiki.jenkins-ci.org/display/JENKINS/Checkstyle+Plugin), [JUnit](https://wiki.jenkins-ci.org/display/JENKINS/JUnit+Plugin) and [SCM API](https://wiki.jenkins-ci.org/display/JENKINS/SCM+API+Plugin)
 * Upload the [.hpi archive](https://github.com/fengtan/drupal-plugin/releases) on `http://localhost:8080/pluginManager/advanced`
 * Create a Drupal Project and update the Database URL (you need to create the DB yourself)

You may still want to check the detailed instructions below, e.g. regarding the web server configuration.

#### Compilation

 * `git clone https://github.com/fengtan/drupal-plugin`
 * `cd drupal-plugin/`
 * `git checkout tags/drupal-0.1`
 * `mvn clean install -DskipTests=true`
 
Alternatively, download a pre-compiled .hpi archive from the [releases page](https://github.com/fengtan/drupal-plugin/releases)

#### Installation

Install dependencies by going to `http://localhost:8080/pluginManager/`:
 * [Checkstyle Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Checkstyle+Plugin)
 * [JUnit Plugin](https://wiki.jenkins-ci.org/display/JENKINS/JUnit+Plugin) (installed by default on Jenkins)
 * [SCM API Plugin](https://wiki.jenkins-ci.org/display/JENKINS/SCM+API+Plugin)

Install the plugin from the command line:
 * `wget http://localhost:8080/jnlpJars/jenkins-cli.jar`
 * `java -jar jenkins-cli.jar -s http://localhost:8080/ install-plugin ./target/drupal.hpi`
 * `/etc/init.d/jenkins restart`

Or from the web interface:
 * Go to `http://localhost:8080/pluginManager/advanced`
 * Upload `./target/drupal.hpi`
 * Restart Jenkins

#### Usage

##### 1. Create Local Database

 * Just create a local database in MySQL: `CREATE DATABASE jenkins;`
 * SQLite is OK for code reviews but not very efficient for running tests

##### 2. Install Drush

[Install Drush](http://docs.drush.org/en/master/install/) 7.0.0-rc2, for instance:
 * `git clone https://github.com/drush-ops/drush.git /usr/local/tools/drush`
 * `cd /usr/local/tools/drush`
 * `git checkout tags/7.0.0-rc2`
 * `curl -sSL https://getcomposer.org/installer | php`
 * `php composer.phar install`

Make sure Drush is configured on `http://localhost:8080/configure`:
 * If Drush is installed globally, then `Path to Drush home` can be empty (default value)
 * If Drush is installed in a specific location (e.g. `/usr/local/tools/drush/drush.php` is a valid file), then `Path to Drush home` should be `/usr/local/tools/drush`
 * If Drush is not installed, you may configure a Shell installer so Jenkins will install it on the fly:
  * Label: leave empty
  * Command:  
`VERSION=7.0.0-rc2`  
`curl -sSL https://github.com/drush-ops/drush/archive/$VERSION.tar.gz | tar xz --strip-components=1`  
`curl -sSL https://getcomposer.org/installer | php`  
`php composer.phar install`
  * Tool Home: `.`

For some reason the automatic installer seems to run every time Jenkins runs a Drush command, so it is better to install Drush manually than using an automatic intaller.

##### 3. Create Project

Just create a new 'Freestyle' project.

Alternatively you may create a 'Drupal' project which generates a ready-to-use job to review code and run tests on a vanilla Drupal core. If you use this option then you may skip most of the instrutions below: just update the database URL and possibly set up a web server.

##### 4. Configure Source Code Management

Configure the Source Code Management section to fetch a full Drupal code base. Here are a few options:
 * If you just want to run tests on a Drupal core, you may use the [Git Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin):
  * Repository: `http://git.drupal.org/project/drupal.git`
  * Branch Specifier: `tags/7.38`
 * If your own code repository includes a Drupal core, then just pull it
 * If it does not, then you may combine your own repo with the drupal.org repo using the [Multiple SCMs Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Multiple+SCMs+Plugin)
 * Alternatively you may use a `Drush Makefile` source, e.g.:  
`api=2`  
`core=7.x`  
`projects[drupal][version]=7.38`

By default Jenkins pulls code into the workspace root but you might want to put Drupal into a subdirectory to keep things clean (e.g. `$WORKSPACE/drupal`):
 * If using [Git](https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin): set option `Additional Behaviours / Check out to a sub-directory` to `drupal`
 * If using [Subversion](https://wiki.jenkins-ci.org/display/JENKINS/Subversion+Plugin): set option `Local module directory` to `drupal`
 * If using a Drush Makefile: set option `Drupal root directory` to `drupal`

Note that a Drush Makefile source will fetch the code every time a new build runs. Using a regular source like Git or Subversion is probably more efficient.

##### 5. Configure Local Web Server

Some tests may fail if Drupal does not run behind a web server. Here are a couple of solutions:
 * Install the [PHP Built-in Web Server Plugin](https://wiki.jenkins-ci.org/display/JENKINS/PHP+Built-in+Web+Server+Plugin) (requires PHP >= 5.4.0) e.g.:
  * Port: `8000`
  * Host: `localhost`
  * Document root: `drupal` (or leave empty if the Drupal root is the workspace root)
 * Or install Apache locally and make it point at the Drupal root (e.g. `/var/lib/jenkins/jobs/myproject/workspace/drupal`)

##### 6. Configure Builds

Add build steps:
 * `Build a Drupal instance`
 * `Review code on Drupal`
 * `Run tests on Drupal`

The default values should work though you need to update a few things:
 * Update the database URL in step `Build a Drupal instance` to point at your database
 * If you have checked out Drupal into a subdirectory (e.g. `drupal`) then update the Drupal root directory of every step accordingly ; otherwise, just leave it empty
 * The URI of step `Run tests on Drupal` should match what you have configured on your webserver (e.g. `http://localhost:8000`)

Note that if your code base does not include a copy of the Coder module, then step `Review code on Drupal` will automatically download it into `$DRUPAL/modules/`.

##### 7. Configure Code Review/Tests Reports
 
Code review results can be analyzed using the [Checkstyle Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Checkstyle+Plugin):
 * Create a post-build action `Publish Checkstyle analysis results`
 * If the logs directory for the code review is `logs_codereview` then set `Checkstyle results` to `logs_codereview/**`
 * You might want to set the unstable threshold to 0 normal warning, and the failed threshold to 0 high warning

Test results can be analyzed using the [JUnit Plugin](://wiki.jenkins-ci.org/display/JENKINS/JUnit+Plugin):
 * Create a post-build action `Publish JUnit test result report`
 * If the logs directory for the tests is `logs_tests` then set `Test report XMLs` to `logs_tests/**`

##### 8. Build The Project

 * Click on `Build Now`: Jenkins should start reviewing and testing the code base
 * After a few builds complete, trend graphs should show up

#### Dependencies

 * [Checkstyle Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Checkstyle+Plugin)
 * [JUnit Plugin](https://wiki.jenkins-ci.org/display/JENKINS/JUnit+Plugin)
 * [SCM API Plugin](https://wiki.jenkins-ci.org/display/JENKINS/SCM+API+Plugin)
 * [PHP Built-in Web Server Plugin](https://wiki.jenkins-ci.org/display/JENKINS/PHP+Built-in+Web+Server+Plugin) or Apache
 * [Drush](http://www.drush.org/en/master/install/) 7.0.0-rc2
 * Only Drupal 7 is supported

#### Troubleshooting

Q: Where are the log files ?  
A: Jenkins logs: `/var/log/jenkins/jenkins.log` and `http://localhost:8080/log/all`  
   Console output: `http://localhost:8080/job/<my-job>/<id>/console`

Q: The plugin is installed but the build steps do not show up  
A: Make sure dependencies are installed and up to date

Q: Many tests fail with this kind of error:  
   `Test UserEditedOwnAccountTestCase->testUserEditedOwnAccount() failed:`  
   `GET http://localhost/user returned 0 (0 bytes).`  
   `in /var/lib/jenkins/jobs/drupal/workspace/modules/user/user.test on line 2047`  
A: Make sure Drupal runs behind a web server
