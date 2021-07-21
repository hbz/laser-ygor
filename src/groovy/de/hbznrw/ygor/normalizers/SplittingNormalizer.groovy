package de.hbznrw.ygor.normalizers

class SplittingNormalizer{

  static List<String> multiValueDelimiters = ["/", "|", "\\", ",", ";", "(", ")"]

  static List<String> splitField(String language){
    for (String delimiter in multiValueDelimiters){
      if (language.contains(delimiter)){
        List<String> languages = language.split(delimiter) as List
        languages*.trim()
        return languages
      }
    }
    // else
    return [language]
  }
}
