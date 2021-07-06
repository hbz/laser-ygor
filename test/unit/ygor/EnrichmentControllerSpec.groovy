package ygor

import grails.test.mixin.TestFor
import org.junit.Test
import spock.lang.Specification

@TestFor(EnrichmentController)
class EnrichmentControllerSpec extends Specification{

  void setup() {
    grailsApplication.config.gokbApi.xrPackageUri = "http://localhost:8080/integration/crossReferencePackage"
  }

  @Test
  void testPackageWithTitlesAddOnlyFalse() {

    given:
    ControllersHelper helper = new EnrichmentController()
    grailsApplication

    when:
    String uri = helper.getDestinationUri(grailsApplication, Enrichment.FileType.PACKAGE_WITH_TITLEDATA, false)

    then:
    uri == "http://localhost:8080/integration/crossReferencePackage?async=true"
  }

}
