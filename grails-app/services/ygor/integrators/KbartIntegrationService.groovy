package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.normalizers.DateNormalizer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.readers.KbartReaderConfiguration
import de.hbznrw.ygor.tools.DateToolkit
import ygor.Record
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer

import java.time.LocalDate

class KbartIntegrationService extends BaseDataIntegrationService{

  KbartIntegrationService(MappingsContainer mappingsContainer) {
    this.mappingsContainer = mappingsContainer
  }


  def integrate(MultipleProcessingThread owner, DataContainer dataContainer,
                KbartReaderConfiguration kbartReaderConfiguration) {
    KbartReader reader = owner.baseDataReader.setConfiguration(kbartReaderConfiguration)
    List<FieldKeyMapping> idMappings = [owner.zdbKeyMapping, owner.issnKeyMapping, owner.eissnKeyMapping]
    LocalDate lastUpdate = null
    if (owner.enrichment.isUpdate){
      lastUpdate = LocalDate.parse(DateNormalizer.getDateString(owner.enrichment.lastProcessingDate))
    }
    // addOnly is to be set if there is at least one KBart line containing a valid date stamp
    boolean addOnly = false
    TreeMap<String, String> item = reader.readItemData(lastUpdate, owner.enrichment.ignoreLastChanged)
    while (item != null) {
      // collect all identifiers (zdb_id, online_identifier, print_identifier) from the record
      Record record = createRecordFromItem(item, idMappings, owner, MappingsContainer.KBART)
      storeRecord(record, dataContainer)
      owner.increaseProgress()
      if (!addOnly){
        if (null != DateToolkit.getAsLocalDate(item.get("last_changed"))){
          owner.enrichment.addOnly = true
        }
      }
      item = reader.readItemData(lastUpdate, owner.enrichment.ignoreLastChanged)
    }
    return
  }
}
