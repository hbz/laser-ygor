package de.hbznrw.ygor.processing

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.export.GokbExporter
import de.hbznrw.ygor.tools.FileToolkit
import grails.util.Holders
import groovy.util.logging.Log4j
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.commons.lang.StringUtils
import ygor.AutoUpdateService
import ygor.Enrichment
import ygor.EnrichmentService

import javax.annotation.Nonnull
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Matcher
import java.util.regex.Pattern

@Log4j
class SendPackageThreadGokb extends UploadThreadGokb{

  final static Pattern INT_FROM_MESSAGE_REGEX = Pattern.compile("(with|but) (\\d+) TIPPs")
  String gokbJobId
  Map gokbStatusResponse
  boolean integrateWithTitleData
  boolean isUpdate

  SendPackageThreadGokb(@Nonnull Enrichment enrichment, @Nonnull String uri, @Nonnull String user,
                        @Nonnull String password, boolean integrateWithTitleData, YgorFeedback ygorFeedback){
    this.enrichment = enrichment
    this.uri = uri
    this.user = user
    this.password = password
    this.total += enrichment.yellowRecords?.size()
    this.total += enrichment.greenRecords?.size()
    result = []
    gokbStatusResponse = [:]
    this.locale = enrichment.locale
    this.integrateWithTitleData = integrateWithTitleData
    this.isUpdate = enrichment.isUpdate
    this.ygorFeedback = ygorFeedback
    ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.PREPARATION
    ygorFeedback.statusDescription += " Created SendPackageThreadGokb using basic auth."
    log.debug("Created SendPackageThreadGokb using basic auth.")
    status = UploadThreadGokb.Status.PREPARATION
  }

  SendPackageThreadGokb(@Nonnull Enrichment enrichment, @Nonnull String uri, boolean integrateWithTitleData,
                        YgorFeedback ygorFeedback){
    this.enrichment = enrichment
    this.uri = uri
    this.total += enrichment.yellowRecords?.size()
    this.total += enrichment.greenRecords?.size()
    result = []
    gokbStatusResponse = [:]
    this.locale = enrichment.locale
    this.isUpdate = enrichment.isUpdate
    this.integrateWithTitleData = integrateWithTitleData
    this.ygorFeedback = ygorFeedback
    ygorFeedback.statusDescription += " Created SendPackageThreadGokb using token auth."
    status = UploadThreadGokb.Status.PREPARATION
    log.info("Set up send package upload thread with ${this.total} records.")
  }


  @Override
  void run(){
    log.info("Starting package upload thread ...")
    ygorFeedback.statusDescription += " Started SendPackageThreadGokb."
    ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.RUNNING
    ygorFeedback.reportingComponent = this.getClass()
    ygorFeedback.dataComponent = DataContainer.class
    status = UploadThreadGokb.Status.STARTED
    def json
    if (integrateWithTitleData){
      json = enrichment.getAsFile(Enrichment.FileType.PACKAGE_WITH_TITLEDATA, true)
    }
    else{
      json = enrichment.getAsFile(Enrichment.FileType.PACKAGE, true)
    }
    log.info("... exportFile: " + enrichment.resultHash + " -> " + uri)

    if(grails.util.Holders.grailsApplication.config.ygor.uploadLocation && enrichment.resultHash != "" && enrichment.dataContainer.pkgHeader?.token) {
      File uploadFolder = new File("${grails.util.Holders.grailsApplication.config.ygor.uploadLocation.toString()}/${enrichment.resultHash}.raw.zip")

      File zipFile = FileToolkit.zipFiles(enrichment.sessionFolder, enrichment.resultHash)

      Files.copy(zipFile.toPath(), uploadFolder.toPath())

    }


    if (isUpdate){
      uri = uri.replace("\\?", "/${enrichment.dataContainer?.pkgHeader?.uuid}?")
      if (enrichment.addOnly){
        uri = uri.concat("&addOnly=true")
      }
      if (enrichment.resultHash != "" && enrichment.dataContainer.pkgHeader?.token){
        uri = uri.concat("&resultHash=${enrichment.resultHash}")
      }

      if(grails.util.Holders.grailsApplication.config.ygorUploadJsonLocation && enrichment.dataContainer.pkgHeader?.token && enrichment.resultHash != "") {
        File uploadFolder = new File("${grails.util.Holders.grailsApplication.config.ygorUploadJsonLocation.toString()}/${enrichment.resultHash}.packageWithTitleData.json")
        File jsonFile = GokbExporter.getFile(enrichment, Enrichment.FileType.PACKAGE_WITH_TITLEDATA, true)

        Files.copy(jsonFile.toPath(), uploadFolder.toPath())

        status = UploadThreadGokb.Status.SUCCESS
      }else {
        result << GokbExporter.sendUpdate(uri, json.getText(), locale, ygorFeedback)
      }
    }
    else{
      if (enrichment.addOnly){
        uri = uri.concat("&addOnly=true")
      }
      result << GokbExporter.sendText(uri, json.getText(), user, password, locale, ygorFeedback)
    }

    if(result.size() > 0) {
      gokbJobId = result[0].get("info")?.get("job_id")?.toString()
      log.info("Finished package upload thread for KB job id ${gokbJobId}.")
    }
  }


  void updateCount(){
    String message = getGokbResponseValue("job_result.message", true)
    if (message != null){
      // get count from finished process
      Matcher matcher = INT_FROM_MESSAGE_REGEX.matcher(message)
      if (matcher.find()){
        Integer foundInt = Integer.valueOf(matcher.group(2))
        if (foundInt != null){
          count = foundInt
        }
      }
      String token = getGokbResponseValue("job_result.updateToken", false)
      String uuid = getGokbResponseValue("job_result.uuid", false)
      if (token != null && uuid != null){
        enrichment.dataContainer?.pkgHeader?.token = token
        enrichment.dataContainer?.pkgHeader?.uuid = uuid
        if (enrichment.autoUpdate == true){
          AutoUpdateService.addEnrichmentJob(enrichment)
        }
      }
      log.debug("Count: $count . Process finished.")
    }
    else{
      String error = getGokbResponseValue("error", false)
      if (error != null){
        count = total
      }
      else{
        // get count from ongoing process
        String countString = getGokbResponseValue("progress", false)
        if (countString != null){
          count = Double.valueOf(countString) / 100.0 * total
        }
      }
      if (count > lastLoggedCount){
        log.debug("Enrichment: $enrichment.originHash . Count: $count . Process unfinished.")
        lastLoggedCount = count
      }
    }
  }


  boolean isInterrupted(){
    String message = getGokbResponseValue("job_result.message", true)
    if (message != null && message.contains("tipps have not been loaded because of validation errors")){
      log.debug("SendPackageThread is interrupted.")
      return true
    }
    message = getGokbResponseValue("result", false)
    if (message != null && message.contains("error")){
      log.debug("SendPackageThread is interrupted.")
      return true
    }
    // else
    return false
  }


  private String getJobId(){
    if (gokbJobId == null && result != null && result.size() > 0 && result[0].get("info") != null){
      gokbJobId = result[0].get("info")?.get("job_id").toString()
    }
    return gokbJobId
  }


  @Override
  String getGokbResponseValue(String responseKey, boolean updateResponse){
    def jobId = getJobId()
    if (jobId == null){
      return null
    }
    if (updateResponse){
      gokbStatusResponse = getGokbStatusResponse(jobId, enrichment?.dataContainer?.pkgHeader?.token)
    }
    String[] path = responseKey.split("\\.")
    def response = gokbStatusResponse
    for (String subField in path){
      response = response.get(subField)
      if (response == null){
        break
      }
    }
    return response
  }


  protected Map getGokbStatusResponse(String jobId, String token){
    if (jobId == null){
      return null
    }
    def uri = Holders.config.gokbApi.xrJobInfo.toString().concat("/").concat(jobId)
    if (!StringUtils.isEmpty(token)){
      uri = uri.concat("?updateToken=").concat(token)
    }
    def http = new HTTPBuilder(uri)
    if (user != null && password != null){
      http.auth.basic user, password
    }
    Map<String, Object> result = new HashMap<>()
    http.request(Method.GET, ContentType.JSON){ req ->
      // TODO : cleanup : unify result structure and adapt method callers accordingly
      response.success = { response, resultMap ->
        if (response.headers.'Content-Type' == 'application/json;charset=UTF-8'){
          if (response.status < 400){
            if (resultMap.result.equals("ERROR")){
              result.put('responseStatus', 'error')
              result.putAll(resultMap)
            }
            else{
              result.put('responseStatus', 'ok')
              result.putAll(resultMap)
            }
          }
          else{
            result.put('responseStatus', 'warning')
            result.putAll(resultMap)
          }
        }
        else{
          result.put('responseStatus', 'authenticationError')
        }
      }
      response.failure = { response, resultMap ->
        if (response.headers.'Content-Type' == 'application/json;charset=UTF-8'){
          result.put('responseStatus', 'error')
          result.putAll(resultMap)
        }
        else{
          result.put('responseStatus', 'authenticationError')
        }
      }
      response.'400'= {resp, resultMap ->
        result.put('responseStatus', 'error')
        result.put('message', resultMap.message)
        result.put('errors', resultMap.errors)
      }
      response.'401'= {resp ->
        result.put('responseStatus', 'authenticationError')
      }
    }
    // log.debug("KB responseStatus : ${result.get('responseStatus')}")
    result
  }


  @Override
  Map getResultsTable(){
    Map results = [:]
    if (integrateWithTitleData){
      results.put("listDocuments.gokb.response.type", "listDocuments.gokb.response.packageWithTitles")
    }
    else{
      results.put("listDocuments.gokb.response.type", "listDocuments.gokb.response.package")
    }
    results.put("listDocuments.gokb.response.status", gokbStatusResponse.get("result"))
    Map jobResult = gokbStatusResponse.get("job_result")
    if (jobResult != null){
      results.put("listDocuments.gokb.response.message", jobResult.get("message"))
      results.put("listDocuments.gokb.response.ok", String.valueOf(count))
      results.put("listDocuments.gokb.response.error", jobResult.get("errors")?.size())
      int i=1
      for (def error in jobResult.get("errors")){
        results.put(String.valueOf(i++), error.toString())
      }
    }
    if ("ERROR" == gokbStatusResponse.get("result")){
      results.put("listDocuments.gokb.response.message", gokbStatusResponse.get("message"))
    }
    if ("ERROR" == gokbStatusResponse.get("job_result")?.get("result")){
      results.put("listDocuments.gokb.response.message", gokbStatusResponse.get("job_result").get("message"))
      results.put("listDocuments.gokb.response.status", "ERROR")
    }
    return results
  }
}