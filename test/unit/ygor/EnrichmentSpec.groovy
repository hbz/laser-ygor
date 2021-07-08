package ygor

import de.hbznrw.ygor.processing.YgorFeedback
import de.hbznrw.ygor.readers.KbartReader
import grails.test.mixin.TestFor
import org.apache.commons.io.FileUtils
import org.junit.AfterClass
import org.junit.Test
import spock.lang.IgnoreRest
import spock.lang.Specification
import spock.lang.Stepwise

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Enrichment)
@Stepwise
class EnrichmentSpec extends Specification {

  Path ygorTestBasePath
  static Enrichment enrichment
  String kbartFile

  def setup() {
    ygorTestBasePath = Files.createDirectories(Paths.get("/tmp/ygor/test"))
  }


  @Test
  @IgnoreRest()
  testCreateEnrichment(){
    System.out.println("testCreateEnrichment")

    given:
    kbartFile = "test/resources/KBart02.tsv"
    YgorFeedback ygorFeedback = new YgorFeedback(YgorFeedback.YgorProcessingStatus.PREPARATION,
        "EnrichmentSpec.testCreateEnrichment().", this.getClass(), null, null, null, null)

    when:
    enrichment = new Enrichment(ygorTestBasePath.toFile() ,kbartFile, ygorFeedback)

    then:
    enrichment.enrichmentFolder.startsWith(ygorTestBasePath.toString())
  }


  @Test
  @IgnoreRest()
  testProcessEnrichment(){
    System.out.println("testProcessEnrichment")

    given:
    Map options = [:]
    options.quote = null
    options.quoteMode = null
    options.ygorVersion = "test"
    options.ygorType = "production"
    KbartReader kbartReader = new KbartReader(kbartFile, "TestFile KBart02 EnrichmentSpec")

    when:
    enrichment.ygorFeedback.statusDescription += " EnrichmentSpec.testProcessEnrichment()."
    enrichment.process(options, kbartReader, enrichment.ygorFeedback)

    then:
    Thread.sleep(5000)
    System.out.println(enrichment.dataContainer.records.size())
    true
  }


  @AfterClass
  static def cleanupAfterAllTests() {
    System.out.println("cleanup")
    FileUtils.deleteDirectory(new File(enrichment.enrichmentFolder))
  }
}
