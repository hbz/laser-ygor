package de.hbznrw.ygor.validators

import de.hbznrw.ygor.enums.Status
import de.hbznrw.ygor.tools.DateToolkit
import grails.util.Holders
import ygor.Record
import ygor.RecordFlag
import ygor.field.MultiField

import java.time.LocalDate

class RecordValidator{

  def messageSource = Holders.grailsApplication.mainContext.getBean 'messageSource'
  def validateCoverage(Record record, Locale locale) {

    MultiField startDate = record.getMultiField("dateFirstIssueOnline")
    MultiField endDate = record.getMultiField("dateLastIssueOnline")
    MultiField startVolume = record.getMultiField("numFirstVolOnline")
    MultiField endVolume = record.getMultiField("numLastIssueOnline")

    // remove due to inconsistency in data length
    boolean isValidCoverageConfiguration = true
    int startDates = startDate.getPrioValues().minus("").size()
    int startVolumes = startVolume.getPrioValues().minus("").size()
    int endDates = endDate.getPrioValues().minus("").size()
    int endVolumes = endVolume.getPrioValues().minus("").size()

    if (endDates > startDates) isValidCoverageConfiguration = false
    else if (endVolumes > startVolumes) isValidCoverageConfiguration = false
    else if (startVolumes > startDates) isValidCoverageConfiguration = false
    else if (endVolumes > endDates) isValidCoverageConfiguration = false
    else if (startVolumes > 0 && startVolumes != startDates) isValidCoverageConfiguration = false
    else if (endVolumes > 0 && endVolumes != endDates) isValidCoverageConfiguration = false

    if (!isValidCoverageConfiguration){
      RecordFlag flag = new RecordFlag(Status.REMOVE_FLAG,
          messageSource.getMessage('statistic.export.field.removed.coverage', null, locale),
          "statistic.export.field.removed.coverage",
          record.multiFields.get("dateFirstIssueOnline").keyMapping, RecordFlag.ErrorCode.COVERAGE_DATA_REMOVED
      )
      flag.setColour(RecordFlag.Colour.YELLOW)
      record.putFlag(flag)
    }

    // remove due to data error
    LocalDate startDateTime = DateToolkit.getAsLocalDate(startDate.getFirstPrioValue())
    LocalDate endDateTime = DateToolkit.getAsLocalDate(endDate.getFirstPrioValue())
    if (startDateTime != null && endDateTime != null){
      if (startDateTime.isAfter(endDateTime)){
        RecordFlag flag = record.getFlagWithErrorCode(RecordFlag.ErrorCode.ISSUE_ONLINE_DATES_ORDER)
        if (flag == null){
          flag = new RecordFlag(Status.INVALID,
              messageSource.getMessage('record.date.order', null, locale),
              'record.date.order', endDate.keyMapping, RecordFlag.ErrorCode.ISSUE_ONLINE_DATES_ORDER)
        }
        flag.setColour(RecordFlag.Colour.RED)
        record.putFlag(flag)
      }
      else{
        record.flags.remove(RecordFlag.ErrorCode.ISSUE_ONLINE_DATES_ORDER)
      }
    }
  }


  def validateHistoryEvent(Record record) {
    // date
    // from
    // to

    MultiField historyEvents = record.getMultiField("historyEvents")
    /* TODO
    if (historyEvents.size == 0){
        if (historyEvents.status == Status.UNDEFINED || historyEvents.status == Status.DATE_IS_MISSING){
            record.addValidation("historyEvents", Status.REMOVE_FLAG)
        }
    }
    else{
        record.addValidation("historyEvents", Status.HISTORYEVENT_IS_UNDEF)
    }
    */
  }

}
