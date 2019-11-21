package de.hbznrw.ygor.readers

import groovy.util.logging.Log4j
import groovy.util.slurpersupport.GPathResult
import ygor.Record
import ygor.field.FieldKeyMapping

@Log4j
class ZdbReader extends AbstractReader {

  static final IDENTIFIER = 'zdb'

  final static Map QUERY_IDS = [:]
  static {
    QUERY_IDS.put("zdb_id", "query=dnb.zdbid%3D")
    QUERY_IDS.put("online_identifier", "query=dnb.iss%3D")
    QUERY_IDS.put("print_identifier", "query=dnb.iss%3D")
  }
  final static String REQUEST_URL = "http://services.dnb.de/sru/zdb?version=1.1&operation=searchRetrieve&maximumRecords=10"
  final static String QUERY_ONLY_JOURNALS = "%20and%20dnb.frm=O"
  final static String FORMAT_IDENTIFIER = 'PicaPlus-xml'

  ZdbReader() {

  }

  @Override
  List<Map<String, List<String>>> readItemData(String queryString) {
    List<Map<String, List<String>>> result = new ArrayList<>()
    try {
      log.info("query ZDB: " + queryString)
      String text = new URL(queryString).getText()
      def records = new XmlSlurper().parseText(text).depthFirst().findAll { it.name() == 'records' }
      if (records) {
        for (GPathResult record in records) {
          Map<String, List<String>> recordMap = new TreeMap<String, String>()
          def subfields = record.depthFirst().findAll { it.name() == 'subf' }
          subfields.each { subfield ->
            if (subfield.parent().parent().name() == "global") {
              def key = subfield.parent().attributes().get("id").concat(":").concat(subfield.attributes().get("id"))
              def value = subfield.localText()[0]
              List<String> values = recordMap.get(key)
              if (values == null){
                values = []
              }
              values << value
              recordMap.put(key, values)
            }
          }
          if (!recordMap.isEmpty()) {
            result.add(recordMap)
          }
        }
      }
    }
    catch (Exception e) {
      log.error(e)
    }
    result
  }


  private def convertToMap(nodes) {
    nodes.children().collectEntries {
      [it.name(), it.childNodes() ? convertToMap(it) : it.text()]
    }
  }


  static String getAPIQuery(String identifier, List<String> queryIdentifier) {
    return REQUEST_URL +
        "&recordSchema=" + FORMAT_IDENTIFIER +
        "&" + QUERY_IDS.get(queryIdentifier.getAt(0)) + identifier + QUERY_ONLY_JOURNALS
  }
}
