[Jenkins](https://jenkins-ci.org/) plugin to [review code](https://www.drupal.org/project/coder) and [run tests](https://www.drupal.org/simpletest) on [Drupal](https://www.drupal.org/).

TODO screenshot
![alt tag](https://raw.github.com/jenkinsci/php-builtin-web-server-plugin/master/screenshot.png)

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
 * TODO

#### Dependencies

 * checkstyle
 * junit
 * scm-api
 * TODO version + link wiki
