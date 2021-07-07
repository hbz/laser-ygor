package ygor

import de.hbznrw.ygor.processing.YgorFeedback
import grails.test.mixin.TestFor
import org.apache.commons.io.FileUtils
import org.junit.Test
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Enrichment)
class EnrichmentSpec extends Specification {

  Path ygorTestBasePath
  Enrichment enrichment

  def setup() {
    ygorTestBasePath = Files.createDirectories(Paths.get("/tmp/ygor/test"))
  }

  @Test
  testCreateEnrichment(){
    given:
    String kbartFile = "test/resources/KBart02.tsv"
    YgorFeedback ygorFeedback = new YgorFeedback(YgorFeedback.YgorProcessingStatus.PREPARATION,
        "EnrichmentSpec.testCreateEnrichment(). ", this.getClass(), null, null, null, null)

    when:
    enrichment = new Enrichment(ygorTestBasePath.toFile() ,kbartFile, ygorFeedback)

    then:
    enrichment.enrichmentFolder.startsWith(ygorTestBasePath.toString())
  }



  def cleanup() {
    FileUtils.deleteDirectory(new File(enrichment.enrichmentFolder))
  }
}
