package de.hbznrw.ygor.readers

import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import java.time.LocalDate
import ygor.EnrichmentService


@Log4j
class Onix2Reader extends AbstractOnixReader{

  EnrichmentService enrichmentService = new EnrichmentService()
  static final def IDENTIFIER = 'onix2'

  def messageSource = grails.util.Holders.applicationContext.getBean("messageSource")

  static ValidationTagLib VALIDATION_TAG_LIB = new ValidationTagLib()


  Onix2Reader(){
    // not in use
  }


  Onix2Reader(def onixFile, String originalFileName) throws Exception{
    init(onixFile, originalFileName)
  }


  protected void init(File onixFile, String originalFileName){

  }


  Map<String, String> readItemData(LocalDate lastPackageUpdate, boolean ignoreLastChanged) {
    return null
  }


  static boolean isValidFile(File file){
    // TODO
    return false
  }


  static List<String> getValidEncodings(){
    return ["UTF-8", "UTF-16"]
  }

}
