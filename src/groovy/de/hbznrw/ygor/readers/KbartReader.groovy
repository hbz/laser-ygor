package de.hbznrw.ygor.readers

import de.hbznrw.ygor.normalizers.DateNormalizer
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.apache.commons.csv.QuoteMode
import org.apache.commons.lang.StringUtils
import ygor.field.FieldKeyMapping
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

import java.text.DateFormat
import java.time.LocalDate

class KbartReader {

  static final IDENTIFIER = 'kbart'
  static final KBART_HEADER_ZDB_ID = "zdb_id"
  static final KBART_HEADER_ONLINE_IDENTIFIER = "online_identifier"
  static final KBART_HEADER_PRINT_IDENTIFIER = "print_identifier"
  static final KBART_HEADER_DOI_IDENTIFIER = "doi_identifier"

  private CSVFormat csvFormat
  private CSVParser csv
  private List<String> csvHeader
  private Iterator<CSVRecord> iterator
  private CSVRecord lastItemReturned
  String fileName

  static ValidationTagLib VALIDATION_TAG_LIB = new ValidationTagLib()

  static MANDATORY_KBART_KEYS = [
      'title_url',
      'publication_type'
  ]

  static ALIASES = [
      'notes' : ['coverage_notes'],
      'zdb_id': ['zdb-id', 'ZDB_ID', 'ZDB-ID']
  ]

  KbartReader(){
    // not in use
  }

  KbartReader(def kbartFile) throws Exception{
    InputStreamReader kbartFileReader = (kbartFile instanceof File) ?
        new InputStreamReader(new FileInputStream(kbartFile)) :
        new InputStreamReader(kbartFile.getInputStream())
    String fileData = kbartFileReader.getText()
    init(fileData)
  }

  protected void init(String fileData){
    // remove the BOM from the Data
    fileData = fileData.replace('\uFEFF', '')
    // automatic delimiter adaptation by selection of the character with biggest count
    int maxCount = 0
    String delimiter
    for (String prop : ['comma', 'semicolon', 'tab']){
      int num = StringUtils.countMatches(fileData, resolver.get(prop).toString())
      if (maxCount < num){
        maxCount = num
        delimiter = prop
      }
    }
    char delimiterChar = resolver.get(delimiter)
    csvFormat = CSVFormat.EXCEL.withHeader().withIgnoreEmptyLines().withDelimiter(delimiterChar).withIgnoreSurroundingSpaces()
    try{
      csv = CSVParser.parse(fileData, csvFormat)
    }
    catch (IllegalArgumentException iae){
      String duplicateName = iae.getMessage().minus("The header contains a duplicate name: \"")
      duplicateName = duplicateName.substring(0, duplicateName.indexOf("\""))
      throw new Exception(VALIDATION_TAG_LIB.message(code: 'error.kbart.multipleColumn').toString()
          .replace("{}", duplicateName)
          .concat("<br>").concat(VALIDATION_TAG_LIB.message(code: 'error.kbart.messageFooter').toString()))
    }
    csvHeader = csv.getHeaderMap().keySet() as ArrayList
    iterator = csv.iterator()
  }


  // NOTE: should have been an override of AbstractReader.readItemData(), but the parameters are too different
  Map<String, String> readItemData(FieldKeyMapping fieldKeyMapping, String identifier, LocalDate lastUpdate) {
    // guess, the iterator is in the position to return the desired next record
    CSVRecord next = getNext(lastUpdate)
    if (next && (!identifier || !fieldKeyMapping || next.get(fieldKeyMapping.kbartKeys == identifier))) {
      return returnItem(next)
    }
    // otherwise, re-iterate over all entries
    CSVRecord currentRecord = next
    CSVRecord item
    while ({
      item = getNext(lastUpdate)
      if (item && item.get(fieldKeyMapping.kbartKeys == identifier)) {
        return returnItem(item)
      }
      // following: "do while" continue condition, see https://stackoverflow.com/a/46474198
      item != currentRecord
    }()) continue
    null
    // this last return statement should never be reached
  }

  private Map<String, String> returnItem(CSVRecord item) {
    if (!item) {
      return null
    }
    def splitItem = item.values()
    if (splitItem.length != csvHeader.size()) {
      log.info('Crappy record ignored, "size != header size" for: ' + item)
      return null
    }
    Map<String, String> resultMap = new HashMap<>()
    boolean hasContentYet = false
    for (int i = 0; i < csvHeader.size(); i++) {
      resultMap.put(csvHeader.get(i), splitItem[i])
      if (!hasContentYet && StringUtils.isNotBlank(splitItem[i])){
        hasContentYet = true
      }
    }
    if (!hasContentYet){
      return null
    }
    lastItemReturned = item
    // Fix coverage_depth = Volltext
    if (resultMap.get("coverage_depth")?.equalsIgnoreCase("volltext")) {
      resultMap.put("coverage_depth", "fulltext")
    }
    resultMap
  }


  CSVRecord getNext(LocalDate lastPackageUpdate) {
    if (lastPackageUpdate == null || !csvHeader.contains("last_changed")){
      if (iterator.hasNext()) {
        return iterator.next()
      }
    }
    else{
      while (iterator.hasNext()) {
        def next = iterator.next()
        LocalDate itemLastUpdate = LocalDate.parse(DateNormalizer.getDateString(next.get("last_changed")))
        if (!itemLastUpdate.isBefore(lastPackageUpdate)){
          return next
        }
      }
      null
    }
  }


  void checkHeader() throws Exception {
    def missingKeys = []
    if (!csvHeader) {
      throw new Exception(VALIDATION_TAG_LIB.message(code: 'error.kbart.missingHeader').toString()
          .concat("<br>").concat(VALIDATION_TAG_LIB.message(code: 'error.kbart.messageFooter').toString()))
    }
    // check mandatory fields
    List<String> headerFields = new ArrayList<>()
    headerFields.addAll(csvHeader)
    MANDATORY_KBART_KEYS.each { kbk ->
      if (!headerFields.contains(kbk)) {
        boolean isMissing = true
        for (def alias : ALIASES[kbk]) {
          if (headerFields.contains(alias)) {
            isMissing = false
          }
        }
        if (isMissing) {
          missingKeys << kbk.toString()
        }
      }
    }
    if (missingKeys.size() > 0) {
      throw new Exception(VALIDATION_TAG_LIB.message(code: 'error.kbart.missingColumns').toString()
          .replace("{}", missingKeys.toString())
          .concat("<br>").concat(VALIDATION_TAG_LIB.message(code: 'error.kbart.messageFooter').toString()))
    }
    // replace aliases
    for (Map.Entry<String, List<String>> alias : ALIASES) {
      if (!csvHeader.contains(alias.getKey())) {
        for (String value : alias.getValue()) {
          if (csvHeader.contains(value)) {
            csvHeader.set(csvHeader.indexOf(value), alias.getKey())
          }
        }
      }
    }
  }


  private CSVParser getCSVParserFromReader(Reader reader) {
    new CSVParser(reader, csvFormat)
  }


  KbartReader setConfiguration(KbartReaderConfiguration configuration) {
    if (null != configuration.quote) {
      if ('null' == configuration.quote) {
        csvFormat = csvFormat.withQuote(null)
      } else {
        csvFormat = csvFormat.withQuote((char) configuration.quote)
      }
    }
    if (null != configuration.quoteMode) {
      csvFormat = csvFormat.withEscape((char) '^')
      csvFormat = csvFormat.withQuoteMode((QuoteMode) configuration.quoteMode)
    }
    if (null != configuration.recordSeparator) {
      csvFormat = csvFormat.withRecordSeparator(configuration.recordSeparator)
    }
    csvFormat = csvFormat.withAllowMissingColumnNames(true)
    csvFormat = csvFormat.withIgnoreHeaderCase(true)
    this
  }

  static def resolver = [
      'comma'      : ',',
      'semicolon'  : ';',
      'tab'        : '\t',
      'doublequote': '"',
      'singlequote': "'",
      'nullquote'  : 'null',
      'all'        : QuoteMode.ALL,
      'nonnumeric' : QuoteMode.NON_NUMERIC,
      'none'       : QuoteMode.NONE
  ]

}
