package ygor

import de.hbznrw.ygor.tools.UrlToolkit
import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils

@Log4j
class AutoUpdateService {

  static void addEnrichmentJob(Enrichment enrichment){
    String fileName = enrichment.originPathName.concat("_").concat(UUID.randomUUID().toString())
    FileWriter fileWriter = new FileWriter(grails.util.Holders.grailsApplication.config.ygor.autoUpdateJobsLocation.concat(fileName))
    fileWriter.write(enrichment.asJson(false))
    fileWriter.close()
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
