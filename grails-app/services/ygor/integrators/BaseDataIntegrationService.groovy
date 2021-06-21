package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.tools.DateToolkit
import ygor.Record
import ygor.field.Field
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.AbstractIdentifier

abstract class BaseDataIntegrationService{

  MappingsContainer mappingsContainer

  protected Record createRecord(List<FieldKeyMapping> idMappings, TreeMap<String, String> item, MultipleProcessingThread owner){
    List<AbstractIdentifier> identifiers
    identifiers = []
    for (idMapping in idMappings){
      for (key in idMapping.kbartKeys){
        if (item[key]){
          Class clazz = owner.identifierByKey[idMapping]
          def identifier = clazz.newInstance(["identifier": item[key]])
          identifiers.add(identifier)
        }
      }
    }
    new Record(identifiers, mappingsContainer)
  }

  protected void createItem(TreeMap<String, String> item, List<FieldKeyMapping> idMappings, MultipleProcessingThread owner, DataContainer dataContainer, boolean addOnly){
    log.debug("Integrating KBart record ${item.toString()}")
    Record record = createRecord(idMappings, item, owner)

    // fill record with all non-identifier fields
    item.each{ key, value ->
      def fieldKeyMapping = mappingsContainer.getMapping(key, MappingsContainer.KBART)
      if (fieldKeyMapping == null){
        fieldKeyMapping = new FieldKeyMapping(false,
            [(MappingsContainer.YGOR) : key,
             (MappingsContainer.KBART): key,
             (MappingsContainer.ZDB)  : "",
             (MappingsContainer.EZB)  : ""])
      }
      MultiField multiField = new MultiField(fieldKeyMapping)
      multiField.addField(new Field(MappingsContainer.KBART, key, value))
      record.addMultiField(multiField)
    }
    record.publicationType = record.multiFields.get("publicationType").getFirstPrioValue().toLowerCase()
    dataContainer.addRecord(record)
    log.debug("... added record ${record.displayTitle} to data container.")
    record.save(dataContainer.enrichmentFolder, dataContainer.resultHash)
    owner.increaseProgress()
    if (!addOnly){
      if (null != DateToolkit.getAsLocalDate(item.get("last_changed"))){
        owner.enrichment.addOnly = true
      }
    }
  }

}
