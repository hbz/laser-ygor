package ygor.field

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.io.support.ClassPathResource

@Log4j
class MappingsContainer {

  static mapWith = "none" // disable persisting into database

  final public static String YGOR = "ygor"
  final public static String KBART = "kbart"
  final public static String ONIX2 = "onix2"
  final public static String ZDB = "zdb"
  final public static String EZB = "ezb"
  final public static String KB = "kb"
  final public static String TYPE = "type"
  final public static String MULTI_VALUE = "multivalue"
  final public static String FLAGS = "flags"
  final public static DEFAULT_SOURCE_PRIO = [KBART, ONIX2, ZDB, EZB]

  final private static JsonSlurper SLURPY = new JsonSlurper()
  private static String DEFAULT_MAPPINGS_FILE = "resources/YgorFieldKeyMapping.json"
  private String mappingsFile

  Map<String, FieldKeyMapping> ygorMappings
  Map<String, FieldKeyMapping> kbartMappings
  Map<String, FieldKeyMapping> onix2Mappings
  Map<String, FieldKeyMapping> zdbMappings
  Map<String, FieldKeyMapping> ezbMappings
  static hasMany = [ygorMappings: FieldKeyMapping, kbartMappings: FieldKeyMapping, onix2Mappings:FieldKeyMapping,
                    zdbMappings : FieldKeyMapping, ezbMappings: FieldKeyMapping]

  MappingsContainer() {
    initialize(DEFAULT_MAPPINGS_FILE)
  }

  MappingsContainer(String mappingsFile) {
    try {
      initialize(mappingsFile)
      this.mappingsFile = mappingsFile
    }
    catch (MissingFieldException mfe) {
      log("Incomplete mapping.\n" + mfe)
    }
  }


  private def initialize(String mappingsFile) throws MissingFieldException {
    ygorMappings = [:]
    kbartMappings = [:]
    onix2Mappings = [:]
    zdbMappings = [:]
    ezbMappings = [:]
    //readMappingsFile(new File(mappingsFile))
    readMappingsFile(new ClassPathResource(mappingsFile).file)
  }


  private def readMappingsFile(File mappingsFile) throws MissingFieldException {
    if (mappingsFile) {
      def json = SLURPY.parse(mappingsFile)
      json.each { map ->
        FieldKeyMapping mapping = jsonNodeToMapping(map)
        putMapping(mapping)
      }
    }
  }


  static FieldKeyMapping jsonNodeToMapping(def json) throws MissingFieldException {
    // ygor key must exist and is not allowed to have an empty value
    if (StringUtils.isEmpty(json.ygor)) {
      throw new MissingFieldException("Missing YgorFieldKey entry in ".concat(json.toString()), FieldKeyMapping.class)
    }
    new FieldKeyMapping(false, json)
  }


  private def putMapping(FieldKeyMapping mapping) {
    if (!StringUtils.isEmpty(mapping.ygorKey)) {
      putPartToMapping(ygorMappings, mapping, mapping.ygorKey)
    }
    if (mapping.kbartKeys instanceof Collection<?> || !StringUtils.isEmpty(mapping.kbartKeys)) {
      putPartToMapping(kbartMappings, mapping, mapping.kbartKeys)
    }
    if (mapping.onix2Keys instanceof Collection<?> || !StringUtils.isEmpty(mapping.onix2Keys)) {
      putPartToMapping(onix2Mappings, mapping, mapping.onix2Keys)
    }
    if (mapping.zdbKeys instanceof Collection<?> || !StringUtils.isEmpty(mapping.zdbKeys)) {
      putPartToMapping(zdbMappings, mapping, mapping.zdbKeys)
    }
    if (mapping.ezbKeys instanceof Collection<?> || !StringUtils.isEmpty(mapping.ezbKeys)) {
      putPartToMapping(ezbMappings, mapping, mapping.ezbKeys)
    }
  }


  private void putPartToMapping(Map<String, FieldKeyMapping> mappingsPart, FieldKeyMapping mapping, def key) {
    if (key instanceof String) {
      // add simple key mapping
      mappingsPart.put(key, mapping)
    } else if (key instanceof Collection<?>) {
      // add multiple key mapping
      for (String item : key) {
        mappingsPart.put(item, mapping)
      }
    }
  }


  /**
   * @param key The key to identify the mapping.
   * @param type One of {@value #YGOR}, {@value #KBART}, {@value #ZDB} or {@value #EZB}
   * @return A mapping with keyMapping for each FieldKeyMapping type.
   */
  FieldKeyMapping getMapping(String key, String type) {
    if (type == YGOR) {
      return ygorMappings.get(key)
    }
    if (type == KBART) {
      return kbartMappings.get(key)
    }
    if (type == ONIX2) {
      return onix2Mappings.get(key)
    }
    if (type == ZDB) {
      return zdbMappings.get(key)
    }
    if (type == EZB) {
      return ezbMappings.get(key)
    }
  }


  void asJson(JsonGenerator jsonGenerator) {
    jsonGenerator.writeStartObject()
    jsonGenerator.writeStringField("mappingsFile", String.valueOf(mappingsFile))

    jsonGenerator.writeFieldName("ygorMappings")
    jsonGenerator.writeStartArray()
    for (FieldKeyMapping fkm in ygorMappings.values()) {
      fkm.asJson(jsonGenerator)
    }
    jsonGenerator.writeEndArray()
    jsonGenerator.writeEndObject()
  }


  static MappingsContainer fromJson(JsonNode jsonNode) {
    MappingsContainer mc = new MappingsContainer()
    for (JsonNode mapping in jsonNode.path("ygorMappings").iterator()) {
      FieldKeyMapping fkm = FieldKeyMapping.fromJson(mapping)
      mc.putMapping(fkm)
    }
    return mc
  }
}
