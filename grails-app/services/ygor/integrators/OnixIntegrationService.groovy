package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.normalizers.DateNormalizer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.Onix2Reader
import ygor.Record
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer

import java.time.LocalDate

class OnixIntegrationService extends BaseDataIntegrationService{

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

      Record record = createRecordFromItem(item, idMappings, owner, MappingsContainer.ONIX2)
      storeRecord(record, dataContainer)

      // TODO: Ensure to ignore non-specified fields?


      // TODO: Assert LanguageRole / b253 to be "1" when setting language
      // TODO: Assert publisher:b291 to be "01" when setting publisher (see https://ns.editeur.org/onix/de/45)

      // TODO: Discuss setting "mainsubject" : this can have multiple formats and variations
      //       (see : https://ns.editeur.org/onix36/en/26)
      // TODO: Discuss use of "productwebsite" : this can have variations
      //       (see : https://ns.editeur.org/onix/de/73)
      // TODO: Discuss, which Ygor date field PublicationDate / b003 should be mapped to
      //       (see : https://vlb.de/hilfe/vlb-onix-empfehlungen/onix-im-vlb-uebersicht)

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


  private TreeMap<String, String> filterByCriterium(TreeMap<String, String> item, String commonNode,
                                                    String criteriumField, String criteriumValue){
    List<String> filterIndices = new ArrayList<>()
    List<String> filterCandidates = new ArrayList<>()
    int pathLength = 0
    for (def entry in item){
      String path = new String(entry.key)
      boolean validPath = true
      String index = null

      for (String nodePathWord in commonNode.split(":")){
        if (path.startsWith("$nodePathWord:")){
          path = path.substring(nodePathWord.length() + 1)
          if (path.matches("^[0-9]+:.*")){
            index = path.substring(0, path.indexOf(":"))
            path = path.substring(path.indexOf(":"))
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
