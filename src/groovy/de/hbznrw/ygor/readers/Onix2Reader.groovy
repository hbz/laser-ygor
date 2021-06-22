package de.hbznrw.ygor.readers

import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.time.LocalDate
import ygor.EnrichmentService


@Log4j
class Onix2Reader extends AbstractOnixReader{

  EnrichmentService enrichmentService = new EnrichmentService()
  static final String IDENTIFIER = 'onix2'
  Node onixData

  def messageSource = grails.util.Holders.applicationContext.getBean("messageSource")

  static ValidationTagLib VALIDATION_TAG_LIB = new ValidationTagLib()


  Onix2Reader(){
    // not in use
  }


  Onix2Reader(File onixFile, String originalFileName) throws Exception{
    init(onixFile, originalFileName)
  }


  protected void init(File onixFile, String originalFileName) throws IllegalFormatException{
    this.dataFileName = originalFileName
    onixData = new XmlParser().parse(onixFile)
    if (!onixData.header){
      throw new IllegalFormatException("ONIX v2 file is missing header section")
    }
    if (!onixData.product){
      throw new IllegalFormatException("ONIX v2 file is missing product section")
    }
  }


  @Override
  Map<String, String> readItemData(LocalDate lastPackageUpdate, boolean ignoreLastChanged) {
    if (!onixData){
      return null
    }
    Node product = onixData.product

    // TODO: Ensure to ignore non-specified fields
    // TODO: Assert field contributor:B034 to be "1" to ensure to get firstAuthor
    // TODO: Assert LanguageRole / b253 to be "1" when setting language
    // TODO: Assert publisher:b291 to be "01" when setting publisher (see https://ns.editeur.org/onix/de/45)

    // TODO: Discuss setting "mainsubject" : this can have multiple formats and variations
    //       (see : https://ns.editeur.org/onix36/en/26)
    // TODO: Discuss use of "productwebsite" : this can have variations
    //       (see : https://ns.editeur.org/onix/de/73)
    // TODO: Discuss, which Ygor date field PublicationDate / b003 should be mapped to
    //       (see : https://vlb.de/hilfe/vlb-onix-empfehlungen/onix-im-vlb-uebersicht)

  }


  @Override
  void checkFields() throws Exception {
    // TODO
  }


  @Override
  static boolean isValidFile(CommonsMultipartFile file){
    if (!hasFileValidExtension(file, ["xml"] as ArrayList<String>)){
      return false
    }
    return true
  }


  @Override
  static List<String> getValidEncodings(){
    return ["UTF-8", "UTF-16"]
  }

}
