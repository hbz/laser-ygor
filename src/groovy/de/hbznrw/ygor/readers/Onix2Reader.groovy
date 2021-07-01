package de.hbznrw.ygor.readers

import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.time.LocalDate
import ygor.EnrichmentService


@Log4j
class Onix2Reader extends AbstractOnixReader{

  int itemCounter
  EnrichmentService enrichmentService = new EnrichmentService()
  static final String IDENTIFIER = 'onix2'
  Node onixData
  char delimiterChar = ','

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
    itemCounter = 0
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
    Map<String, String> result = new HashMap<>()
    Node product = onixData.product[itemCounter++]
    if (product == null){
      return null
    }
    for (Node field in product.value()){
      for (def node in field.value()){
        List<Map.Entry> entries = new ArrayList<>()
        entries = readItemEntries(entries, field.name().localPart, node)
        for (Map.Entry entry in entries){
          result.put(entry.key, entry.value)
        }
      }
    }
    return result
  }


  private List<Map.Entry<String, String>> readItemEntries(List<Map.Entry> entries, String fieldName, Object fieldValue){
    if (fieldValue instanceof String){
      entries.add(new AbstractMap.SimpleEntry<String, Integer>(fieldName, fieldValue))
    }
    if (fieldValue instanceof Node){
      for (def node in fieldValue.value()){
        readItemEntries(entries, "$fieldName:${fieldValue.name().localPart}", node)
      }
    }
    return entries
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
