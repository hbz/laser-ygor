package de.hbznrw.ygor.normalizers

import org.apache.commons.lang.StringUtils

class SplittingNormalizer{

  static List<Character> multiValueDelimiters = ['/', '|', '\\', ',', ';']


  static Character getDelimiter(String values){
    for (Character delimiter in multiValueDelimiters){
      if (values.contains(String.valueOf(delimiter))){
        return delimiter
      }
    }
    return null
  }


  static List<String> splitField(String values, String delimiter = null){
    if (delimiter == null){
      delimiter = getDelimiter(values)
    }
    if (!StringUtils.isEmpty(delimiter) && values.contains(delimiter)){
      List<String> splitValues = values.split(delimiter) as List
      return splitValues*.trim()
    }
    // else
    return [values]
  }
}
