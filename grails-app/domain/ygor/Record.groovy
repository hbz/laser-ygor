package ygor

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import de.hbznrw.ygor.enums.Status
import de.hbznrw.ygor.normalizers.CaseNormalizer
import de.hbznrw.ygor.normalizers.DateNormalizer
import de.hbznrw.ygor.normalizers.EditionNormalizer
import de.hbznrw.ygor.tools.JsonToolkit
import de.hbznrw.ygor.validators.RecordValidator
import groovy.json.JsonSlurper
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.io.support.ClassPathResource
import org.springframework.context.i18n.LocaleContextHolder as LCH
import ygor.RecordFlag.ErrorCode
import ygor.field.HistoryEvent
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.*


@SuppressWarnings('JpaObjectClassSignatureInspection')
class Record{

  static mapWith = "none" // disable persisting into database

  static ObjectMapper MAPPER = new ObjectMapper()
  static List<String> GOKB_FIELD_ORDER = []
  static {
    MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    GOKB_FIELD_ORDER.addAll(new JsonSlurper().parseText(
        new ClassPathResource("/resources/GokbOutputFieldOrder.json").file.text))
  }
  static JsonFactory JSON_FACTORY = new JsonFactory()
  static{
    JSON_FACTORY.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
  }

  def messageSource = grails.util.Holders.applicationContext.getBean("messageSource")

  String uid
  ZdbIdentifier zdbId
  EzbIdentifier ezbId
  DoiIdentifier doiId
  OnlineIdentifier onlineIdentifier
  PrintIdentifier printIdentifier
  String publicationType
  String displayTitle
  Map multiFields
  String zdbIntegrationDate // TODO : performance check : this information can be replaced by a boolean "isZdbIntegrated"
  String ezbIntegrationDate // TODO : performance check : this information can be replaced by a boolean "isEzbIntegrated"
  String zdbIntegrationUrl
  String ezbIntegrationUrl
  List historyEvents
  Map<AbstractIdentifier, String> duplicates
  Map<ErrorCode, RecordFlag> flags


  static hasMany = [multiFields       : MultiField,
                    historyEvents     : HistoryEvent,
                    duplicates        : String,
                    flags             : RecordFlag]

  static constraints = {
  }


  Record(List<AbstractIdentifier> ids, MappingsContainer container) {
    this(ids, container, UUID.randomUUID().toString())
  }


  Record(List<AbstractIdentifier> ids, MappingsContainer container, String uid) {
    this.uid = uid
    for (id in ids) {
      addIdentifier(id)
    }
    multiFields = [:]
    duplicates = [:]
    historyEvents = []
    for (def ygorMapping in container.ygorMappings) {
      multiFields.put(ygorMapping.key, new MultiField(ygorMapping.value))
    }
    displayTitle = ""
    zdbIntegrationDate = null
    ezbIntegrationDate = null
    zdbIntegrationUrl = null
    ezbIntegrationUrl = null
    flags = [:]
  }


  void addIdentifier(AbstractIdentifier identifier) {
    if (identifier.identifier == null){
      return
    }
    if (identifier instanceof ZdbIdentifier) {
      if (zdbId && identifier.identifier.replaceAll("x", "X") != zdbId.identifier.replaceAll("x", "X")) {
        throw new IllegalArgumentException("${identifier} already set to ${zdbId} for record")
      }
      zdbId = identifier
    }
    else if (identifier instanceof EzbIdentifier) {
      if (ezbId && identifier.identifier != ezbId.identifier) {
        throw new IllegalArgumentException("${identifier} already set to ${ezbId} for record")
      }
      ezbId = identifier
    }
    else if (identifier instanceof DoiIdentifier) {
      if (doiId && identifier.identifier != doiId.identifier) {
        throw new IllegalArgumentException("${identifier} already set to ${doiId} for record")
      }
      doiId = identifier
    }
    else if (identifier instanceof OnlineIdentifier) {
      if (onlineIdentifier && identifier.identifier != onlineIdentifier.identifier) {
        RecordFlag flag = new RecordFlag(Status.MISMATCH, "${onlineIdentifier} %s ${identifier}.",
            "record.identifier.replace", multiFields.get("onlineIdentifier").keyMapping, ErrorCode.ONLINE_ID_REPLACED)
        flag.setColour(RecordFlag.Colour.YELLOW)
        flags.put(flag.errorCode, flag)
      }
      onlineIdentifier = identifier
    }
    else if (identifier instanceof PrintIdentifier) {
      if (printIdentifier && identifier.identifier != printIdentifier.identifier) {
        RecordFlag flag = new RecordFlag(Status.MISMATCH, "${printIdentifier} %s ${identifier}.",
            "record.identifier.replace", multiFields.get("printIdentifier").keyMapping, ErrorCode.PRINT_ID_REPLACED)
        flag.setColour(RecordFlag.Colour.YELLOW)
        flags.put(flag.errorCode, flag)
      }
      printIdentifier = identifier
    }
  }


  void normalize(String namespace) {
    EditionNormalizer.normalizeEditionNumber(this)
    setHistoryEventDateType()
    for (MultiField multiField in multiFields.values()) {
      multiField.normalize(namespace)
    }
    CaseNormalizer.normalize(multiFields.get("publicationType"), CaseNormalizer.Case.LOWER, CaseNormalizer.Selection.ALL)
  }


  void setHistoryEventDateType(){
    MultiField heDate = multiFields.get("historyEventDate")
    if (!StringUtils.isEmpty(heDate?.getFirstPrioValue())){
      String heType = multiFields.get("historyEventRelationType")?.getFirstPrioValue()
      if (heType.equals("s")){
        heDate.type = DateNormalizer.START_DATE
      }
      else if (heType.equals("f")){
        heDate.type = DateNormalizer.END_DATE
      }
    }
  }


  void deriveHistoryEventObjects(Enrichment enrichment) {
    // first, re-set history - there might be old events of previous calculations
    historyEvents = []
    for (int index = 0; index < multiFields.get("historyEventDate").getFields(MappingsContainer.ZDB).size(); index++){
      historyEvents << new HistoryEvent(this, index, enrichment)
    }
  }


  @SuppressWarnings('JpaAttributeMemberSignatureInspection')
  boolean isValid() {
    // validate tipp.titleUrl
    MultiField urlMultiField = multiFields.get("titleUrl")
    if (urlMultiField == null || !hasValidPublicationType()) {
      return false
    }
    // check flags
    if (hasFlagOfColour(RecordFlag.Colour.RED)){
      return false
    }
    // check multifields for critical errors
    for (MultiField multiField in multiFields.values()){
      if (multiField.isCriticallyIncorrect(publicationType)){
        return false
      }
    }
    return true
  }


  boolean hasValidPublicationType(){
    if (publicationType == null || !(publicationType in ["serial", "monograph", "other", "database"])){
      return false
    }
    return true
  }


  boolean hasFlagOfColour(RecordFlag.Colour colour){
    for (RecordFlag flag in flags.values()){
      if (flag.colour.equals(colour)){
        return true
      }
    }
    return false
  }


  RecordFlag putFlag(RecordFlag flag){
    flags.put(flag.errorCode, flag)
  }


  void validateContent(String namespace, Locale locale, RecordValidator recordValidator, boolean isZdbIntegrated = false) {
    this.validateMultifields(namespace)
    recordValidator.validateCoverage(this, locale)
    // RECORD_VALIDATOR.validateHistoryEvent(this) TODO?

    if (multiFields.get("publicationType").getFirstPrioValue().equals("Serial") &&
        !multiFields.get("zdbId").status.toString().equals(Status.VALID.toString())){
      RecordFlag flag = new RecordFlag(Status.WARNING, messageSource.getMessage('statistic.edit.record.zdbmatch',
          null, "ZDB match", locale), 'statistic.edit.record.zdbmatch',
          multiFields.get("zdbId").keyMapping, RecordFlag.ErrorCode.ZDB_MATCH)
      if (!isValid() && isZdbIntegrated){
        flag.setColour(RecordFlag.Colour.RED)
      }
      else{
        flag.setColour(RecordFlag.Colour.YELLOW)
      }
      flags.put(flag.errorCode, flag)
    }

    if (!hasValidPublicationType()){
      RecordFlag flag = new RecordFlag(Status.WARNING, messageSource.getMessage('statistic.edit.record.invalidPublicationType',
          null, "Invalid publication_type", locale), 'statistic.edit.record.invalidPublicationType',
          multiFields.get("publicationType").keyMapping, RecordFlag.ErrorCode.INVALID_PUBLICATION_TYPE)
      flag.setColour(RecordFlag.Colour.RED)
      flags.put(flag.errorCode, flag)
    }

    if (!duplicates.isEmpty()){
      RecordFlag flag = new RecordFlag(Status.WARNING, messageSource.getMessage('statistic.edit.record.duplicateIdentifiers',
          null, "Duplicate identifiers", locale), 'statistic.edit.record.duplicateIdentifiers',
          multiFields.get("zdbId").keyMapping, RecordFlag.ErrorCode.DUPLICATE_IDENTIFIERS)
      flag.setColour(RecordFlag.Colour.YELLOW)
      flags.put(flag.errorCode, flag)
    }

    if (publicationType.equals("serial") && zdbIntegrationUrl == null){
      RecordFlag flag = new RecordFlag(Status.WARNING, messageSource.getMessage('statistic.edit.record.missingZdbAlignment',
          null, "Missing ZDB alignment", locale), 'statistic.edit.record.missingZdbAlignment',
          multiFields.get("zdbId").keyMapping, RecordFlag.ErrorCode.MISSING_ZDB_ALIGNMENT)
      flag.setColour(RecordFlag.Colour.YELLOW)
      flags.put(flag.errorCode, flag)
    }
  }


  void addDuplicates(AbstractIdentifier id, Set<String> recordUids, String enrichmentFolder, String resultHash,
                     MappingsContainer mappingsContainer){
    log.debug("Adding duplicate IDs for record ".concat(multiFields.get("publicationTitleKbart").getFirstPrioValue()).concat(" : " + recordUids.toString()))
    for (String recordUid in recordUids){
      if (recordUid != this.uid){
        Record dup = load(enrichmentFolder, resultHash, recordUid, mappingsContainer)
        log.debug("... ".concat(dup.multiFields.get("publicationTitleKbart").getFirstPrioValue()))
        if (!haveDistinctiveId(this, dup)){
          duplicates.put(id, recordUid)
        }
      }
    }
  }


  static haveDistinctiveId(Record rec1, Record rec2){
    if (rec1.zdbId?.identifier != null && rec2.zdbId?.identifier != null &&
        rec1.zdbId.identifier != rec2.zdbId.identifier){
      return true
    }
    if (rec1.onlineIdentifier?.identifier != null && rec2.onlineIdentifier?.identifier != null &&
        rec1.onlineIdentifier.identifier != rec2.onlineIdentifier.identifier){
      return true
    }
    if (rec1.printIdentifier?.identifier != null && rec2.printIdentifier?.identifier != null &&
        rec1.printIdentifier.identifier != rec2.printIdentifier.identifier){
      return true
    }
    // else
    return false
  }


  RecordFlag getFlagWithErrorCode(ErrorCode errorCode){
    for (RecordFlag flag in flags.values()){
      if (flag.errorCode?.equals(errorCode)){
        return flag
      }
    }
    return null
  }


  Set<RecordFlag> getFlagsByColour(RecordFlag.Colour colour){
    Set<RecordFlag> result = new HashSet<>()
    for (RecordFlag flag in flags.values()){
      if (flag.colour.equals(colour)){
        result.add(flag)
      }
    }
    result
  }


  void addMultiField(MultiField multiField) {
    multiFields.put(multiField.ygorFieldKey, multiField)
  }


  MultiField getMultiField(def ygorFieldKey) {
    multiFields.get(ygorFieldKey)
  }


  List<MultiField> multiFieldsInGokbOrder(){
    multiFields.values().sort{
      multiField -> (GOKB_FIELD_ORDER.indexOf(multiField.ygorFieldKey) > -1 ?
          GOKB_FIELD_ORDER.indexOf(multiField.ygorFieldKey) :
          GOKB_FIELD_ORDER.size())
    }
  }


  private void validateMultifields(String namespace) {
    multiFields.each { String k, MultiField v -> v.validateContent(namespace, this) }
  }


  String asJson(JsonGenerator jsonGenerator) {
    jsonGenerator.writeStartObject()
    jsonGenerator.writeStringField("uid", uid)
    jsonGenerator.writeStringField("zdbId", zdbId?.identifier)
    jsonGenerator.writeStringField("ezbId", ezbId?.identifier)
    jsonGenerator.writeStringField("doiId", doiId?.identifier)
    jsonGenerator.writeStringField("printIdentifier", printIdentifier?.identifier)
    jsonGenerator.writeStringField("onlineIdentifier", onlineIdentifier?.identifier)
    jsonGenerator.writeStringField("publicationType", publicationType)
    if (ezbIntegrationDate) {
      jsonGenerator.writeStringField("ezbIntegrationDate", ezbIntegrationDate)
    }
    if (ezbIntegrationUrl) {
      jsonGenerator.writeStringField("ezbIntegrationUrl", ezbIntegrationUrl)
    }
    if (zdbIntegrationDate) {
      jsonGenerator.writeStringField("zdbIntegrationDate", zdbIntegrationDate)
    }
    if (zdbIntegrationUrl) {
      jsonGenerator.writeStringField("zdbIntegrationUrl", zdbIntegrationUrl)
    }
    jsonGenerator.writeFieldName("multiFields")
    jsonGenerator.writeStartArray()
    for (MultiField mf in multiFields.values()) {
      mf.asJson(jsonGenerator)
    }
    jsonGenerator.writeEndArray()
    if (!duplicates.isEmpty()){
      jsonGenerator.writeFieldName("duplicates")
      jsonGenerator.writeStartObject()
      for (def dup in duplicates){
        if (dup.key != null && dup.value != null){
          jsonGenerator.writeStringField(dup.key.toString(), dup.value.toString())
        }
      }
      jsonGenerator.writeEndObject()
    }
    if (!flags.isEmpty()){
      jsonGenerator.writeFieldName("flags")
      jsonGenerator.writeStartArray()
      for (def flag in flags.values()){
        flag.asJson(jsonGenerator)
      }
      jsonGenerator.writeEndArray()
    }
    jsonGenerator.writeEndObject()
  }


  String asStatisticsJson() {
    Writer writer = new StringWriter()
    JsonGenerator jsonGenerator = new JsonFactory().createGenerator(writer)
    jsonGenerator.writeStartObject()
    jsonGenerator.writeStringField("uid", uid)
    for (MultiField mf in multiFields.values()) {
      jsonGenerator.writeFieldName(mf.ygorFieldKey)
      jsonGenerator.writeStartObject()
      jsonGenerator.writeStringField("value", mf.getFirstPrioValue())
      jsonGenerator.writeStringField("source", mf.getPrioSource())
      jsonGenerator.writeStringField("status", mf.status)
      jsonGenerator.writeEndObject()
    }
    jsonGenerator.writeEndObject()
    jsonGenerator.close()
    writer.toString()
  }


  @SuppressWarnings('JpaAttributeMemberSignatureInspection')
  void setDisplayTitle(){
    List<String> titleFieldNames = ["publicationTitleApis", "publicationTitleKbart",
                                    "publicationTitleVariation", "publicationSubTitle"]
    for (String displayTitleCandidateFieldNames in titleFieldNames){
      String value = multiFields.get(displayTitleCandidateFieldNames).getFirstPrioValue()
      if (!StringUtils.isEmpty(value)){
        displayTitle = value
      }
    }
  }


  RecordFlag getFlag(String uid){
    for (def flag in flags.values()){
      if (uid.equals(flag.uid)){
        return flag
      }
    }
    return null
  }


  static Record fromJson(JsonNode json, MappingsContainer mappings) {
    List<AbstractIdentifier> ids = new ArrayList<>()
    ids.add(new ZdbIdentifier(JsonToolkit.fromJson(json, "zdbId"), mappings.getMapping("zdbId", MappingsContainer.YGOR)))
    ids.add(new EzbIdentifier(JsonToolkit.fromJson(json, "ezbId"), mappings.getMapping("ezbId", MappingsContainer.YGOR)))
    ids.add(new DoiIdentifier(JsonToolkit.fromJson(json, "doiId"), mappings.getMapping("doiId", MappingsContainer.YGOR)))
    ids.add(new OnlineIdentifier(JsonToolkit.fromJson(json, "onlineIdentifier"), mappings.getMapping("onlineIdentifier", MappingsContainer.YGOR)))
    ids.add(new PrintIdentifier(JsonToolkit.fromJson(json, "printIdentifier"), mappings.getMapping("printIdentifier", MappingsContainer.YGOR)))
    String uid = JsonToolkit.fromJson(json, "uid")
    Record result = new Record(ids, mappings, uid)
    Iterator it = ((ArrayNode) (json.path("multiFields"))).iterator()
    while (it.hasNext()) {
      ObjectNode nextNode = it.next()
      String ygorKey = JsonToolkit.fromJson(nextNode, "ygorKey")
      result.addMultiField(MultiField.fromJson(nextNode, mappings.getMapping(ygorKey, MappingsContainer.YGOR)))
    }
    String ezbIntegrationDate = JsonToolkit.fromJson(json, "ezbIntegrationDate")
    if (ezbIntegrationDate) {
      result.ezbIntegrationDate = ezbIntegrationDate
    }
    String ezbIntegrationUrl = JsonToolkit.fromJson(json, "ezbIntegrationUrl")
    if (ezbIntegrationUrl) {
      result.ezbIntegrationUrl = ezbIntegrationUrl
    }
    String zdbIntegrationDate = JsonToolkit.fromJson(json, "zdbIntegrationDate")
    if (zdbIntegrationDate) {
      result.zdbIntegrationDate = zdbIntegrationDate
    }
    String zdbIntegrationUrl = JsonToolkit.fromJson(json, "zdbIntegrationUrl")
    if (zdbIntegrationUrl) {
      result.zdbIntegrationUrl = zdbIntegrationUrl
    }
    String publicationType = JsonToolkit.fromJson(json, "publicationType")
    if (publicationType) {
      result.publicationType = publicationType
    }
    result.duplicates = [:]
    JsonNode duplicates = json.path("duplicates")
    if (duplicates instanceof ObjectNode){
      Iterator<String> fieldNames = duplicates.fieldNames()
      if (fieldNames != null){
        while (fieldNames.hasNext()){
          String fieldName = fieldNames.next()
          String value = ((TextNode) duplicates.path(fieldName))?.asText()
          if (fieldName != null && value != null){
            result.duplicates.put(AbstractIdentifier.fromString(fieldName, mappings), value)
          }
        }
      }
    }
    result.flags = [:]
    Collection flags = JsonToolkit.fromJson(json, "flags")
    if (flags != null){
      for (def flag in flags){
        RecordFlag rf = RecordFlag.fromJson(MAPPER.readTree(flag))
        result.flags.put(rf.errorCode, rf)
      }
    }
    result
  }


  void save(String enrichmentFolder, String resultHash){
    StringWriter stringWriter = new StringWriter()
    JsonGenerator jsonGenerator = JSON_FACTORY.createGenerator(stringWriter)
    File targetFile = new File(enrichmentFolder.concat(resultHash).concat("_").concat(uid))
    this.asJson(jsonGenerator)
    jsonGenerator.close()
    PrintWriter printWriter = new PrintWriter(targetFile, "UTF-8")
    printWriter.println(stringWriter.toString())
    printWriter.close()
    stringWriter.close()
  }


  static Record load(String enrichmentFolder, String resultHash, String uid, MappingsContainer mappings){
    return fromJson(JsonToolkit.jsonNodeFromFile(new File(enrichmentFolder.concat(resultHash).concat("_").concat(uid))), mappings)
  }

}
