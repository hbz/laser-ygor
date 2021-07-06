package ygor

import grails.test.spock.IntegrationSpec

import java.lang.reflect.Array

class EnrichmentIntegrationSpec extends IntegrationSpec {

  def enrichmentController
  Enrichment enrichment01
  String sessionFolder

  def setup() {
    enrichmentController = new EnrichmentController()
    sessionFolder = grails.util.Holders.grailsApplication.config.ygor.uploadLocation.toString() + File.separator + "EnrichmentIntegrationSpec"
    enrichment01 = Enrichment.fromFilename(sessionFolder, "./test/resources/KBart01.tsv")
  }

  void "test process"() {
    when:
      enrichmentController.request.parameterMap["resultHash"] = [enrichment01.resultHash] as Array
      enrichmentController.enrichmentService.addSessionEnrichment(enrichment01)
      def result = enrichmentController.process()
    then:
      1 == 1
  }

  def cleanup() {
    int j = 0
  }
}
