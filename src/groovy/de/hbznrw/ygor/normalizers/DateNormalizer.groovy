package de.hbznrw.ygor.normalizers

import de.hbznrw.ygor.tools.DateToolkit
import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.regex.Matcher
import java.util.regex.Pattern


@Log4j
class DateNormalizer {

  static String START_DATE = "StartDate"
  static String END_DATE = "EndDate"
  static Pattern BRACKET_PATTERN = Pattern.compile("^\\[(.*)]-?\$")
  static Pattern DATE_SPAN_PATTERN = Pattern.compile(
      "^([\\d]{4}-[\\d]{4})|" +
      "([\\d]{4}-[\\d]{2}-[\\d]{4}-[\\d]{2})|" +
      "([\\d]{4}(-[\\d]{2}){2}-[\\d]{4}(-[\\d]{2}){2})|" +
      "([\\d]{2}\\.[\\d]{4}-[\\d]{2}\\.[\\d]{4})|" +
      "(([\\d]{2}\\.){2}[\\d]{4}-([\\d]{2}\\.){2}[\\d]{4})\$")
  static Pattern DATE_SPAN_GROUPER =
      Pattern.compile("(([\\d]{2}\\.){0,2}[\\d]{4}(-[\\d]{2}){0,2})-(([\\d]{2}\\.){0,2}[\\d]{4}(-[\\d]{2}){0,2})")

  static DateTimeFormatter TARGET_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  static String normalizeDate(String str, String dateType) {
    if (!str) {
      return str
    }
    str = removeBlanksAndBrackets(str)
    str = pickPartFromDateSpan(str, dateType)
    str = completeEndDate(str, dateType)
    LocalDate localDate = DateToolkit.getAsLocalDate(str)
    if (localDate != null){
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      str = localDate.format(formatter)
    }
    str
  }


  static String completeEndDate(String str, String dateType){
    if (dateType.equals(END_DATE)){
      if (str.matches("^[\\d]{4}\$")){
        return str.concat("-12-31")
      }
      if (str.matches("^[\\d]{4}-[\\d]{2}\$")){
        YearMonth yearMonth = new YearMonth(Integer.valueOf(str.substring(0,4)), Integer.valueOf(str.substring(5)))
        LocalDate endOfMonth = yearMonth.atEndOfMonth()
        return TARGET_FORMATTER.format(endOfMonth)
      }
    }
    str
  }


  private static String pickPartFromDateSpan(String str, String dateType){
    // Take only start part or end part of something like "01.01.2000-31.12.2000"
    if (str.matches(DATE_SPAN_PATTERN)){
      Matcher dateSpanGrouperMatcher = DATE_SPAN_GROUPER.matcher(str)
      dateSpanGrouperMatcher.matches()
      if (dateType.equals(START_DATE)){
        str = dateSpanGrouperMatcher.group(1)
      }
      else if (dateType.equals(END_DATE)){
        str = dateSpanGrouperMatcher.group(4)
      }
    }
    str
  }


  private static String removeBlanksAndBrackets(String str){
    str = str.trim()
    def bracketMatcher = BRACKET_PATTERN.matcher(str)
    if (bracketMatcher.find()){
      str = bracketMatcher.group(1)
    }
    str
  }


  static getDateString(String aString){
    if (StringUtils.isEmpty((aString))){
      return null
    }
    if (aString.contains("T")){
      return aString.substring(0, aString.indexOf("T"))
    }
    return aString
  }
}
