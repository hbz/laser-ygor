package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import de.hbznrw.ygor.export.structure.Meta
import de.hbznrw.ygor.export.structure.PackageHeader
import de.hbznrw.ygor.tools.JsonToolkit
import de.hbznrw.ygor.tools.RecordFileFilter
import de.hbznrw.ygor.validators.RecordValidator
import groovy.util.logging.Log4j
import ygor.Record
import ygor.field.MappingsContainer
import ygor.identifier.AbstractIdentifier

@Log4j
class DataContainer {

  static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance

  Meta info
  PackageHeader pkgHeader
  String  pkgId
  String  pkgIdNamespace
  String  isil
  ObjectNode packageHeader
  Set<String> records
  Map<AbstractIdentifier, Set<String>> recordsPerId
  ArrayNode titles
  ArrayNode tipps
  String curatoryGroup
  File sessionFolder
  String enrichmentFolder
  String resultHash
  MappingsContainer mappingsContainer
  RecordValidator recordValidator


  DataContainer(File sessionFolder, String enrichmentFolder, String resultHash, MappingsContainer mappingsContainer) {
    if (!sessionFolder.isDirectory()){
      throw new IOException("Could not read from record directory.")
    }
    this.sessionFolder = sessionFolder.absoluteFile
    this.enrichmentFolder = enrichmentFolder
    this.resultHash = resultHash
    this.mappingsContainer = mappingsContainer
    info = new Meta(
        date: new Date().format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone('GMT+1')),
        api: [],
        stash: [:],
        namespace_title_id: ""
    )
    pkgHeader = new PackageHeader()

    records = []
    recordsPerId = [:]
    titles = new ArrayNode(NODE_FACTORY)
    tipps = new ArrayNode(NODE_FACTORY)
    recordValidator = new RecordValidator()
  }


  def addRecord(Record record){
    records.add(record.uid)
  }


  Set<Record> getRecords(def id){
    if (id instanceof AbstractIdentifier){
      Set<Record> result  = new HashSet<>()
      for (String uid in recordsPerId.get(id)){
        result << Record.load(enrichmentFolder, resultHash, uid, mappingsContainer)
      }
      return result
    }
    try {
      if (id instanceof String && UUID.fromString(id)){
        return new HashSet() << Record.load(enrichmentFolder, resultHash, id, mappingsContainer)
      }
    }
    catch(IllegalArgumentException iae){
      return null
    }
    return null
  }


  void validateRecords(Locale locale) {
    for (String recId in records) {
      Record record = Record.load(enrichmentFolder.concat(File.separator), resultHash, recId, mappingsContainer)
      record.validateContent(info.namespace_title_id, locale, recordValidator)
      record.save(enrichmentFolder, resultHash)
    }
  }


  static DataContainer fromJson(File sessionFolder, String enrichmentFolder, String resultHash,
                                MappingsContainer mappings, boolean loadRecordData) throws IOException{
    DataContainer result = new DataContainer(sessionFolder, enrichmentFolder, resultHash, mappings)
    if (loadRecordData){
      for (File file : sessionFolder.listFiles(new RecordFileFilter(resultHash))) {
        Record rec = Record.fromJson(JsonToolkit.jsonNodeFromFile(file), mappings)
        result.records.add(rec.uid)
      }
    }
    result
  }


  void markDuplicateIds(){
    log.debug("marking duplicate IDs ...")
    this.sortAllRecordsPerId()
    for (def idRecs in recordsPerId){
      if (idRecs.value.size() > 1){
        for (String recId in idRecs.value){
          Record record = Record.load(enrichmentFolder, resultHash, recId, mappingsContainer)
          record.addDuplicates(idRecs.key, idRecs.value, enrichmentFolder, resultHash, mappingsContainer)
          record.save(enrichmentFolder, resultHash)
        }
      }
    }
    log.debug("marking duplicate IDs finished")
  }


  void sortAllRecordsPerId(){
    recordsPerId = [:]
    for (String recId in records){
      Record rec = Record.load(enrichmentFolder, resultHash, recId, mappingsContainer)
      sortRecordPerId(rec)
    }
  }


  private void sortRecordPerId(Record rec){
    if (rec.zdbId?.identifier){
      addRecordToIdSortation(rec.zdbId, rec)
    }
    if (rec.ezbId?.identifier){
      addRecordToIdSortation(rec.ezbId, rec)
    }
    if (rec.doiId?.identifier){
      addRecordToIdSortation(rec.doiId, rec)
    }
    if (rec.onlineIdentifier?.identifier){
      addRecordToIdSortation(rec.onlineIdentifier, rec)
    }
    if (rec.printIdentifier?.identifier){
      addRecordToIdSortation(rec.printIdentifier, rec)
    }
  }


  void addRecordToIdSortation(AbstractIdentifier id, Record record){
    Set<String> recordList = recordsPerId.get(id)
    if (recordList == null){
      recordList = new HashSet<>()
      recordsPerId.put(id, recordList)
    }
    recordList.add(record.uid)
  }


  void removeRecordFromIdSortation(AbstractIdentifier id, Record record){
    Set<Record> recordList = recordsPerId.get(id)
    if (recordList == null){
      return
    }
    recordList.remove(record)
    if (recordList.isEmpty()){
      recordsPerId.remove(id)
    }
  }
}
