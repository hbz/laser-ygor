grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.6
grails.project.source.level = 1.6

grails.project.fork = [
    // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
    // compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

    // configure settings for the test-app JVM, uses the daemon by default
    test   : [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon: true],
    // configure settings for the run-app JVM
    run    : [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve: false],
    // configure settings for the run-war JVM
    war    : [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve: false],
    // configure settings for the Console UI JVM
    console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
  // inherit Grails' default dependencies
  inherits("global") {
    // specify dependency exclusions here; for example, uncomment this to disable ehcache:
    // excludes 'ehcache'
  }
  log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
  checksums true // Whether to verify checksums on resolve
  legacyResolve false
  // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

  repositories {
    inherits true // Whether to inherit repository definitions from plugins

    grailsPlugins()
    grailsHome()
    mavenLocal()
    grailsCentral()
    mavenCentral()

  }

  dependencies {
    // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.
    runtime 'mysql:mysql-connector-java:5.1.38'
    test "org.grails:grails-datastore-test-support:1.0.2-grails-2.4"
    // sync with gokb
    runtime 'org.postgresql:postgresql:9.3-1101-jdbc41'
    runtime 'org.codehaus.groovy.modules.http-builder:http-builder:0.7.1'
    runtime 'org.apache.httpcomponents:httpclient:4.3.5'
    runtime 'org.apache.httpcomponents:httpmime:4.3.5'
    runtime 'com.github.albfernandez:juniversalchardet:2.3.0'

    compile 'com.google.guava:guava:21.0'
    compile 'com.fasterxml.jackson.core:jackson-annotations:2.9.8'
    compile 'com.fasterxml.jackson.core:jackson-core:2.9.8'
    compile 'com.fasterxml.jackson.core:jackson-databind:2.9.8'
    compile 'commons-validator:commons-validator:1.6'
    compile 'com.github.miachm.sods:SODS:1.4.0'
  }

  plugins {
    // plugins for the build system only
    build ":tomcat:8.0.33"

    // plugins for the compile step
    compile ":scaffolding:2.1.2"
    compile ':cache:1.1.8'
    // asset-pipeline 2.0+ requires Java 7, use version 1.9.x with Java 6
    compile ":asset-pipeline:2.5.7"
    compile "org.grails.plugins:quartz:1.0.2"

    // plugins needed at runtime but not for compilation
    runtime ":hibernate4:4.3.10" // or ":hibernate:3.6.10.18"
    runtime ":database-migration:1.4.0"
    runtime ":jquery:1.11.1"
    runtime ':twitter-bootstrap:3.3.5'
    runtime "org.grails.plugins:grails-datatables:0.15"
  }
}

/*
 * Commented out when introducing SessionService.setSessionDuration().
 * Has formerly been part of Config.groovy , but according to
 * https://stackoverflow.com/questions/2907516/how-to-configure-a-session-timeout-for-grails-application/18494029#comment51360045_18494029
 * BuildConfig is the right place.
 *
// added for https://github.com/hbz/laser-ygor/issues/53
//       according to https://stackoverflow.com/a/18494029/4420271 Timeout is set to 16 hours.
//       This workaround solution is preferred to others for being versionable most easily.
grails.war.resources = { stagingDir, args ->
  def webXML = new java.io.File("${stagingDir}/WEB-INF/web.xml")
  webXML.text = webXML.text.replaceFirst("<session-timeout>30</session-timeout>", "<session-timeout>960</session-timeout>")
}
*/

