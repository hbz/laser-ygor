package de.hbznrw.ygor.processing

import com.google.common.base.Throwables
import de.hbznrw.ygor.export.GokbExporter
import de.hbznrw.ygor.readers.EzbReader
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.readers.KbartReaderConfiguration
import de.hbznrw.ygor.readers.ZdbReader
import groovy.util.logging.Log4j
import ygor.Enrichment
import ygor.EnrichmentService
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.identifier.OnlineIdentifier
import ygor.identifier.PrintIdentifier
import ygor.identifier.ZdbIdentifier
import ygor.integrators.EzbIntegrationService
import ygor.integrators.KbartIntegrationService
import ygor.integrators.ZdbIntegrationService

@Log4j
class MultipleProcessingThread extends Thread {

  static final KEY_ORDER = ["zdbId", "onlineIdentifier", "printIdentifier"]

  public identifierByKey = [:]

  FieldKeyMapping zdbKeyMapping
  FieldKeyMapping issnKeyMapping
  FieldKeyMapping eissnKeyMapping

  public isRunning

  private Enrichment enrichment
  private apiCalls
  private quote
  private quoteMode
  private recordSeparator
  private addOnly
  private platform
  private kbartFile

  private double progressCurrent = 0.0
  private double progressIncrement

  private KbartReader kbartReader
  private ZdbIntegrationService zdbIntegrationService
  private EzbIntegrationService ezbIntegrationService

  MultipleProcessingThread(Enrichment en, HashMap options, KbartReader kbartReader) throws YgorProcessingException{
    enrichment = en
    apiCalls = EnrichmentService.decodeApiCalls(options.get('options'))
    quote = options.get('quote')
    quoteMode = options.get('quoteMode')
    recordSeparator = options.get('recordSeparator')
    addOnly = options.get('addOnly')
    platform = options.get('platform')
    kbartFile = en.originPathName
    this.kbartReader = kbartReader
    zdbKeyMapping = en.mappingsContainer.getMapping("zdbId", MappingsContainer.YGOR)
    issnKeyMapping = en.mappingsContainer.getMapping("printIdentifier", MappingsContainer.YGOR)
    eissnKeyMapping = en.mappingsContainer.getMapping("onlineIdentifier", MappingsContainer.YGOR)
    identifierByKey = [(zdbKeyMapping)  : ZdbIdentifier.class,
                       (issnKeyMapping) : PrintIdentifier.class,
                       (eissnKeyMapping): OnlineIdentifier.class]
  }


  @Override
  void run(){
    isRunning = true
    progressCurrent = 0.0
    if (null == enrichment.originPathName)
      System.exit(0)
    enrichment.setStatus(Enrichment.ProcessingState.WORKING)
    log.info('Starting MultipleProcessingThread integrate... '.concat(String.valueOf(getId())))
    try {
      ezbIntegrationService = new EzbIntegrationService(enrichment.mappingsContainer)
      zdbIntegrationService = new ZdbIntegrationService(enrichment.mappingsContainer)
      for (String call : apiCalls) {
        switch (call) {
          case KbartReader.IDENTIFIER:
            KbartReaderConfiguration conf =
                new KbartReaderConfiguration(quote, quoteMode, recordSeparator)
            KbartIntegrationService kbartIntegrationService = new KbartIntegrationService(enrichment.mappingsContainer)
            calculateProgressIncrement(enrichment.enrichmentFolder)
            kbartIntegrationService.integrate(this, enrichment.dataContainer, conf)
            break
          case EzbReader.IDENTIFIER:
            enrichment.isEzbIntegrated = true
            ezbIntegrationService.integrate(this, enrichment.dataContainer)
            break
          case ZdbReader.IDENTIFIER:
            enrichment.isZdbIntegrated = true
            zdbIntegrationService.integrate(this, enrichment.dataContainer)
            break
        }
      }
      log.info('Done MultipleProcessingThread '.concat(String.valueOf(getId())))
    }
    catch (YgorProcessingException e) { // TODO Throw it in ...IntegrationService and / or ...Reader
      enrichment.setStatusByCallback(Enrichment.ProcessingState.ERROR)
      enrichment.setMessage(e.toString().substring(YgorProcessingException.class.getName().length() + 2))
      log.error(e.getMessage())
      log.error(e.printStackTrace())
      log.error('Aborted MultipleProcessingThread '.concat(String.valueOf(getId())))
      return
    }
    catch (Exception e) {
      enrichment.setStatusByCallback(Enrichment.ProcessingState.ERROR)
      log.error(e.getMessage())
      log.error('Aborted MultipleProcessingThread '.concat(String.valueOf(getId())))
      def stacktrace = Throwables.getStackTraceAsString(e).replaceAll("\\p{C}", " ")
      enrichment.setMessage(stacktrace + " ..")
      return
    }

    processUiSettings()

    GokbExporter.extractPackageHeader(enrichment)    // to enrichment.dataContainer.packageHeader
    enrichment.dataContainer.markDuplicateIds()
    enrichment.classifyAllRecords()
    enrichment.save()
    enrichment.setStatusByCallback(Enrichment.ProcessingState.FINISHED)
  }


  void stopEnrichment(){
    log.info('Stopping MultipleProcessingThread '.concat(String.valueOf(getId())))
    interrupt()
    if (zdbIntegrationService != null){
      zdbIntegrationService.interrupt()
    }
    if (ezbIntegrationService != null){
      ezbIntegrationService.interrupt()
    }
    isRunning = false
    log.info('Stopped MultipleProcessingThread '.concat(String.valueOf(getId())))
  }


  private void processUiSettings() {
    FieldKeyMapping tippNameMapping =
        enrichment.setTippPlatformNameMapping(enrichment.dataContainer?.pkgHeader?.nominalPlatform.name)
    enrichment.enrollMappingToRecords(tippNameMapping)
    FieldKeyMapping tippUrlMapping =
        enrichment.setTippPlatformUrlMapping(enrichment.dataContainer?.pkgHeader?.nominalPlatform.url)
    enrichment.enrollMappingToRecords(tippUrlMapping)
  }


  private void calculateProgressIncrement(String enrichmentFolder){
    String file = new File(kbartFile).absolutePath.equals(kbartFile) ? kbartFile : enrichmentFolder.concat(kbartFile)
    double lines = (double) (countLines(file) - 1)
    if (lines > 0) {
      progressIncrement = 100.0 / lines / (double) apiCalls.size()
      // division by 3 for number of tasks (Kbart, ZDB, EZB)
    } else {
      progressIncrement = 1 // dummy assignment
    }
  }


  private static int countLines(String filename) throws IOException {
    InputStream is = new BufferedInputStream(new FileInputStream(filename))
    try {
      byte[] c = new byte[1024]
      int readChars = is.read(c)
      if (readChars == -1) return 0
      int count = 0
      while (readChars == 1024) {
        for (int i = 0; i < 1024;) {
          if (c[i++] == '\n') ++count
        }
        readChars = is.read(c)
      }
      while (readChars != -1) {
        System.out.println(readChars)
        for (int i = 0; i < readChars; ++i) {
          if (c[i] == '\n') ++count
        }
        readChars = is.read(c)
      }
      return count == 0 ? 1 : count
    }
    finally {
      is.close()
    }
  }


  void increaseProgress() {
    progressCurrent += progressIncrement
    if (progressCurrent > 99){
      // Don't increase to 100 as this can irritate the user in case of waiting
      progressCurrent = 99
    }
    enrichment.setProgress(progressCurrent)
  }


  Enrichment getEnrichment() {
    enrichment
  }
}
