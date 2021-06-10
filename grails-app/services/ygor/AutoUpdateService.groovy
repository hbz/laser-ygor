package ygor

import de.hbznrw.ygor.processing.YgorFeedback
import de.hbznrw.ygor.tools.JsonToolkit
import de.hbznrw.ygor.tools.UrlToolkit
import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils

@Log4j
class AutoUpdateService {

  static EnrichmentController ENRICHMENT_CONTROLLER = new EnrichmentController()
  static EnrichmentService ENRICHMENT_SERVICE = new EnrichmentService()

  static void addEnrichmentJob(Enrichment enrichment){
    String fileName = enrichment.originPathName.concat("_").concat(UUID.randomUUID().toString())
    FileWriter fileWriter = new FileWriter(grails.util.Holders.grailsApplication.config.ygor.autoUpdateJobsLocation.concat(fileName))
    fileWriter.write(enrichment.asJson(false))
    fileWriter.close()
  }


  static List<URL> getUpdateUrls(Enrichment enrichment){
    if (enrichment == null){
      return new ArrayList<URL>()
    }
    return getUpdateUrls(enrichment.originUrl, enrichment.lastProcessingDate, null)
  }


  static List<URL> getUpdateUrls(String url, String lastProcessingDate, String packageCreationDate){
    if (StringUtils.isEmpty(lastProcessingDate)){
      lastProcessingDate = packageCreationDate
    }
    if (StringUtils.isEmpty(url) || StringUtils.isEmpty(lastProcessingDate)){
      return new ArrayList<URL>()
    }
    if (UrlToolkit.containsDateStamp(url) || UrlToolkit.containsDateStampPlaceholder(url)){
      return UrlToolkit.getUpdateUrlList(url, lastProcessingDate)
    }
    else{
      return Arrays.asList(new URL(url))
    }
  }

}
