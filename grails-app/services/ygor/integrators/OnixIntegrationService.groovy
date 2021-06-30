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
      Record record = createRecordFromItem(item, idMappings, owner, MappingsContainer.ONIX2)
      storeRecord(record, dataContainer)

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

      item = reader.readItemData(lastUpdate, owner.enrichment.ignoreLastChanged)
    }
    return
  }

}
