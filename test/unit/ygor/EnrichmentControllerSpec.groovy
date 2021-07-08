package ygor

import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification

@TestFor(EnrichmentController)
class EnrichmentControllerSpec extends Specification{

  void setup(){
    grailsApplication.config.gokbApi.xrPackageUri = "http://localhost:8080/integration/crossReferencePackage"
  }

  @Test
  void testPackageWithTitlesAddOnlyFalse(){

    given:
    ControllersHelper helper = new EnrichmentController()
    grailsApplication

    when:
    String uri = helper.getDestinationUri(grailsApplication, Enrichment.FileType.PACKAGE_WITH_TITLEDATA, false)

    then:
    uri == "http://localhost:8080/integration/crossReferencePackage?async=true"
  }


  @Test
  void testProcess(){

    given:
    String kbartFile = "test/resources/KBart02.tsv"
    MockHttpServletRequest request = new MockHttpServletRequest()
    request.characterEncoding = 'UTF-8'
    GrailsWebRequest webRequest = new GrailsWebRequest(request, new MockHttpServletResponse(), ServletContextHolder.servletContext)
    request.setAttribute(GrailsApplicationAttributes.WEB_REQUEST, webRequest)
    RequestContextHolder.setRequestAttributes(webRequest)

    when:
    true

    then:
    true
  }

}
