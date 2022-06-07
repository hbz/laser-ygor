package de.hbznrw.ygor.validators

import de.hbznrw.ygor.enums.Status
import de.hbznrw.ygor.tools.DateToolkit
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import ygor.Record
import ygor.RecordFlag
import ygor.field.MultiField

import java.time.LocalDate

class RecordValidator {

  static ValidationTagLib VALIDATION_TAG_LIB = new ValidationTagLib()

  static validateCoverage(Record record) {

    MultiField startDate = record.getMultiField("dateFirstIssueOnline")
    MultiField endDate = record.getMultiField("dateLastIssueOnline")
    MultiField startVolume = record.getMultiField("numFirstVolOnline")
    MultiField endVolume = record.getMultiField("numLastIssueOnline")

    // remove due to inconsistency in data length
    if (!(startDate.getPrioValues().size() == endDate.getPrioValues().size()
          == startVolume.getPrioValues().size() == endVolume.getPrioValues().size())){
      RecordFlag flag = new RecordFlag(Status.REMOVE_FLAG, VALIDATION_TAG_LIB.message(
          code: 'statistic.export.field.removed.coverage').toString(), "statistic.export.field.removed.coverage",
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
          flag = new RecordFlag(Status.INVALID, "${endDate.keyMapping.ygorKey} ${endDate.getFirstPrioValue()} %s",
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


  static validateHistoryEvent(Record record) {
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
