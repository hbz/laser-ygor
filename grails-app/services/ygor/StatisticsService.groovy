package ygor

import com.github.miachm.sods.Color
import com.github.miachm.sods.Range
import com.github.miachm.sods.Sheet
import com.github.miachm.sods.SpreadSheet
import grails.transaction.Transactional
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import ygor.field.MappingsContainer


@Transactional
class StatisticsService {

  static ValidationTagLib VALIDATION_TAG_LIB = new ValidationTagLib()
  static int UUID_LENGTH = 36
  static Color RED = new Color(230, 45, 35)
  static Color YELLOW = new Color(248, 208, 30)

  static File getStatisticsExport(Enrichment enrichment) {
    // enrichment.validateContainer()

    Map<String, String> redRecords = new HashMap<>()
    Map<String, String> yellowRecords = new HashMap<>()
    splitPublicationTitleAndUuid(enrichment.redRecords, redRecords)
    splitPublicationTitleAndUuid(enrichment.yellowRecords, yellowRecords)

    Map<String, Integer> kbartFields = new HashMap<>()
    enrichment.mappingsContainer.kbartMappings.keySet().eachWithIndex{ String fieldName, int i ->
      kbartFields.put(fieldName, i)
    }

    SpreadSheet spreadSheet = new SpreadSheet()
    Sheet sheet = new Sheet(VALIDATION_TAG_LIB.message(code: 'statistic.export.tableName').toString())
    sheet.appendColumns(kbartFields.size())

    // set header
    ["Publication Title", "Field Name", "Problem"].eachWithIndex{ String columnName, int i ->
      Range range = sheet.getRange(0, i)
      range.setValue(columnName)
      range.setFontBold(true)
    }

    appendFlags(sheet, enrichment, redRecords, yellowRecords)

    sheet.setColumnWidth(0, 90D)
    sheet.setColumnWidth(1, 30D)
    sheet.setColumnWidth(2, 80D)
    spreadSheet.appendSheet(sheet)
    File exportFile = new File("${grails.util.Holders.grailsApplication.config.ygorStatisticStorageLocation.toString()}/${enrichment.resultHash}.statistics.ods")
    spreadSheet.save(exportFile)
    return exportFile
  }


  private static void appendFlags(Sheet sheet, Enrichment enrichment, HashMap<String, String> redRecords,
                                  HashMap<String, String> yellowRecords){
    for (recUid in enrichment.dataContainer.records){
      Record rec
      if (redRecords.get(recUid)){
        // append red flags first
        rec = Record.load(enrichment.enrichmentFolder, enrichment.resultHash, recUid, enrichment.mappingsContainer)
        appendSheetLine(rec, sheet, enrichment, RED, RecordFlag.Colour.RED)
      }
      if (yellowRecords.get(recUid)){
        // append yellow flags second
        if (!rec){
          rec = Record.load(enrichment.enrichmentFolder, enrichment.resultHash, recUid, enrichment.mappingsContainer)
        }
        appendSheetLine(rec, sheet, enrichment, YELLOW, RecordFlag.Colour.YELLOW)
      }
    }
  }


  private static void appendSheetLine(Record rec, sheet, enrichment, Color sheetColor, RecordFlag.Colour flagColour){
    Set<RecordFlag> flags = rec.getFlagsByColour(flagColour)
    flags.each { RecordFlag flag ->
      sheet.appendRows(1)
      Range range = sheet.getRange(sheet.getMaxRows() - 1, 0)
      range.setValue(rec.multiFields.get("publicationTitleKbart").getFirstPrioValue())
      range.setBackgroundColor(sheetColor)
      range = sheet.getRange(sheet.getMaxRows() - 1, 1)
      range.setValue(enrichment.mappingsContainer.getMapping(flag.ygorFieldKey, MappingsContainer.YGOR).kbartKeys.get(0))
      range.setBackgroundColor(sheetColor)
      range = sheet.getRange(sheet.getMaxRows() - 1, 2)
      range.setValue(flag.text)
      range.setBackgroundColor(sheetColor)
    }
  }


  private static Set<String> splitPublicationTitleAndUuid(def yellowRecords, def result){
    yellowRecords.keySet().each { String titlePlusId ->
      if (titlePlusId.length() > UUID_LENGTH){
        result.put(titlePlusId.substring(
            titlePlusId.length() - UUID_LENGTH, titlePlusId.length()),
            titlePlusId.substring(0, titlePlusId.length() - UUID_LENGTH))
      }
    }
  }
}
