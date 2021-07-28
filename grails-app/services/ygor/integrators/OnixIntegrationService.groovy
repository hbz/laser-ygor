package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.normalizers.DateNormalizer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.Onix2Reader
import org.apache.commons.lang.StringUtils
import ygor.Record
import ygor.field.Field
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField

import java.time.LocalDate

class OnixIntegrationService extends BaseDataIntegrationService{

  final static String INDEX_DELIMITER_REGEX = ":[0-9]+:"
  final static String SIMPLE_DELIMITER = ":"

  OnixIntegrationService(MappingsContainer mappingsContainer) {
    this.mappingsContainer = mappingsContainer
  }


  def integrate(MultipleProcessingThread owner, DataContainer dataContainer) {
    Onix2Reader reader = owner.baseDataReader
    List<FieldKeyMapping> idMappings = [owner.zdbKeyMapping, owner.issnKeyMapping, owner.eissnKeyMapping]
    LocalDate lastUpdate = null
    if (owner.enrichment.isUpdate){
      lastUpdate = LocalDate.parse(DateNormalizer.getDateString(owner.enrichment.lastProcessingDate))
    }
    TreeMap<String, String> item = reader.readItemData(lastUpdate, owner.enrichment.ignoreLastChanged)
    while (item != null) {
      item = filterContributorsForFirstAuthor(item)
      item = filterByLanguageRole(item)
      item = filterByPublishingRole(item)

      // TODO: Ensure to ignore non-specified fields?
      // TODO: Discuss setting "mainsubject" : this can have multiple formats and variations
      //       (see : https://ns.editeur.org/onix36/en/26)
      // TODO: Discuss use of "productwebsite" : this can have variations
      //       (see : https://ns.editeur.org/onix/de/73)
      // TODO: Discuss, which Ygor date field PublicationDate / b003 should be mapped to
      //       (see : https://vlb.de/hilfe/vlb-onix-empfehlungen/onix-im-vlb-uebersicht)

      Record record = createRecordFromItem(item, idMappings, owner, MappingsContainer.ONIX2)
      record = moveValuesToConfiguredMultiFields(record)
      record = setIdentifiers(record)
      storeRecord(record, dataContainer)
      item = reader.readItemData(lastUpdate, owner.enrichment.ignoreLastChanged)
    }
  }


  private TreeMap<String, String> filterContributorsForFirstAuthor(TreeMap<String, String> item){
    item = filterByCriterium(item, "contributor", "b034", "1")
    return item
  }


  private TreeMap<String, String> filterByLanguageRole(TreeMap<String, String> item){
    item = filterByCriterium(item, "language", "b253", "01")
    return item
  }


  private Record setIdentifiers(Record record){
    Set<String> indices = []
    Map<String, List<MultiField>> indexedFieldsToBeRemoved = [:]
    for (String multiFieldName in record.multiFields.keySet()){
      if (multiFieldName.startsWith("productidentifier")){
        String index = StringUtils.substringBetween(multiFieldName, SIMPLE_DELIMITER, SIMPLE_DELIMITER)
        indices.add(index)
      }
    }
    Map<String, String> typeValueMap = [:]
    for (String index in indices){
      MultiField typeField = record.multiFields.get("productidentifier:$index:b221".toString())
      MultiField valueField = record.multiFields.get("productidentifier:$index:b244".toString())
      if (typeField && valueField){
        typeValueMap.put(typeField.getFirstPrioValue(), valueField.getFirstPrioValue())
        indexedFieldsToBeRemoved.put(typeField.getFirstPrioValue(), [typeField, valueField])
      }
    }
    // productidentifier type (b244) entries with type 03 or 15 are ISBNs
    moveIdField(["03", "15", "3"], typeValueMap, record, indexedFieldsToBeRemoved,
        "productidentifier:b244(productidentifier:b221 in [03,15])", "printIdentifier")
    // productidentifier type (b244) entries with type 06 are DOIs
    moveIdField(["06", "6"], typeValueMap, record, indexedFieldsToBeRemoved,
        "productidentifier:b244(productidentifier:b221 = 06)", "doiIdentifier")
    return record
  }


  private void moveIdField(List<String> types, LinkedHashMap<String, String> typeValueMap, Record record,
                           LinkedHashMap<String, List<MultiField>> indexedFieldsToBeRemoved,
                           String fromFieldKey, String toFieldKey){
    for (String type in types){
      if (typeValueMap.get(type)){
        MultiField idField = record.multiFields.get(toFieldKey)
        Field value = new Field(MappingsContainer.ONIX2, fromFieldKey,
            typeValueMap.get(type))
        idField.addField(value)
        for (def removeFields in indexedFieldsToBeRemoved){
          if (removeFields.key in types){
            for (MultiField removeField in removeFields.value){
              record.multiFields.remove(removeField.ygorFieldKey)
            }
          }
        }
        break
      }
    }
  }


  private TreeMap<String, String> filterByPublishingRole(TreeMap<String, String> item){
    // see https://ns.editeur.org/onix/de/45
    item = filterByCriterium(item, "publisher", "b291", "01")
    return item
  }


  private Record moveValuesToConfiguredMultiFields(Record record){
    List<MultiField> indexFieldsToBeRemoved = []
    // Iterate over index-delimited MultiFields
    for (def multiFieldEntry in record.multiFields){
      if (multiFieldEntry.key.matches(".*".concat(INDEX_DELIMITER_REGEX).concat(".*"))){
        // Check if there is a corresponding non-index-delimited MultiField
        String to = multiFieldEntry.key.replaceAll(INDEX_DELIMITER_REGEX, SIMPLE_DELIMITER)
        FieldKeyMapping mapping = mappingsContainer.onix2Mappings.get(to)
        if (mapping == null){
          // there is no configured target field --> nothing to do
          continue
        }
        MultiField toField = record.multiFields.get(mapping.ygorKey)
        if (toField && !toField.ygorFieldKey.equals(multiFieldEntry.key)){
          // then copy the simple value Field to that MultiField
          List<Field> onix2Fields = multiFieldEntry.value.getFields(MappingsContainer.ONIX2)
          for (Field onix2Field in onix2Fields){
            onix2Field.key = onix2Field.key.replaceAll(INDEX_DELIMITER_REGEX, SIMPLE_DELIMITER)
            toField.addField(onix2Field)
          }
          // and list the MultiField for removal / remove it
          indexFieldsToBeRemoved.add(multiFieldEntry.value)
        }
      }
    }
    for (MultiField removeField in indexFieldsToBeRemoved){
      record.multiFields.remove(removeField.ygorFieldKey)
    }
    return record
  }


  private TreeMap<String, String> filterByCriterium(TreeMap<String, String> item, String commonNode,
                                                    String criteriumField, String criteriumValue){
    List<String> filterIndices = new ArrayList<>()
    List<String> filterCandidates = new ArrayList<>()
    int pathLength = 0
    for (def entry in item){
      String path = new String(entry.key)
      boolean validPath = true
      String index = null

      for (String nodePathWord in commonNode.split(SIMPLE_DELIMITER)){
        if (path.startsWith("$nodePathWord:")){
          path = path.substring(nodePathWord.length() + 1)
          if (path.matches("^[0-9]+:.*")){
            index = path.substring(0, path.indexOf(SIMPLE_DELIMITER))
            path = path.substring(path.indexOf(SIMPLE_DELIMITER))
          }
        }
        else{
          validPath = false
        }
      }
      if (validPath){
        filterCandidates.add(entry.key)
        if (path.substring(1).equals(criteriumField)){
          if (entry.value != criteriumValue){
            pathLength = entry.key.length() - path.length()
            filterIndices.add(index)
          }
        }
      }
    }
    Iterator iterator = filterCandidates.iterator()
    while (iterator.hasNext()){
      String filterCandidateKey = iterator.next()
      for (String filterNumber in filterIndices){
        int lastIndex = pathLength
        int firstIndex = lastIndex-filterNumber.length()
        if (filterCandidateKey.substring(firstIndex, lastIndex).equals(filterNumber)){
          item.remove(filterCandidateKey)
        }
      }
    }
    return item
  }

}
