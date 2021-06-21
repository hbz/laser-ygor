package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.normalizers.DateNormalizer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.Onix2Reader
import de.hbznrw.ygor.tools.DateToolkit
import ygor.Record
import ygor.field.Field
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.AbstractIdentifier

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
    // addOnly is to be set if there is at least one KBart line containing a valid date stamp
    boolean addOnly = false
    TreeMap<String, String> item = reader.readItemData(lastUpdate, owner.enrichment.ignoreLastChanged)
    while (item != null) {
      // collect all identifiers (zdb_id, online_identifier, print_identifier) from the record
      createItem(item, idMappings, owner, dataContainer, addOnly)
      item = reader.readItemData(lastUpdate, owner.enrichment.ignoreLastChanged)
    }
    return
  }

}
