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


  protected Record createRecordFromItem(TreeMap<String, String> item, List<FieldKeyMapping> idMappings,
                                        MultipleProcessingThread owner, String source){
    log.debug("Integrating $source record ${item.toString()}")
    Record record = createRecord(idMappings, item, owner)

    // fill record with all non-identifier fields
    item.each{ key, value ->
      def fieldKeyMapping = mappingsContainer.getMapping(key, source)
      if (fieldKeyMapping == null){
        if (source == MappingsContainer.KBART){
          fieldKeyMapping = new FieldKeyMapping(false,
              [(MappingsContainer.YGOR) : key,
               (MappingsContainer.KBART): key,
               (MappingsContainer.ONIX2): "",
               (MappingsContainer.ZDB)  : "",
               (MappingsContainer.EZB)  : ""])
        }
        else if (source == MappingsContainer.ONIX2){
          fieldKeyMapping = new FieldKeyMapping(false,
              [(MappingsContainer.YGOR) : key,
               (MappingsContainer.ONIX2): key,
               (MappingsContainer.KBART): "",
               (MappingsContainer.ZDB)  : "",
               (MappingsContainer.EZB)  : ""])
        }
      }
      MultiField multiField = new MultiField(fieldKeyMapping)
      multiField.addField(new Field(source, key, value))
      record.addMultiField(multiField)
    }
    record.publicationType = record.multiFields.get("publicationType").getFirstPrioValue().toLowerCase()
    record
  }


  protected void storeRecord(Record record, DataContainer dataContainer){
    dataContainer.addRecord(record)
    log.debug("... added record ${record.displayTitle} to data container.")
    record.save(dataContainer.enrichmentFolder, dataContainer.resultHash)
  }

}
