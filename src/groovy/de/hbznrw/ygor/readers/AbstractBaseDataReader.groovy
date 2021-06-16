package de.hbznrw.ygor.readers

import java.time.LocalDate

/**
 * Common class for all kinds of Readers that might be necessary at the point where the kind of base data files
 * to be read is still unknown.
 */
abstract class AbstractBaseDataReader{

  String dataFileName

  abstract Map<String, String> readItemData(LocalDate lastPackageUpdate, boolean ignoreLastChanged)

  abstract void checkFields() throws Exception

  abstract static boolean isValidFile(File file)

  abstract static List<String> getValidEncodings()

  static Class determineReader(File baseDataFile){
    if (KbartReader.isValidFile(baseDataFile)){
      return KbartReader
    }
    if (Onix2Reader.isValidFile(baseDataFile)){
      return Onix2Reader
    }
  }

  static boolean hasFileValidExtension(File file, List<String> validExtensions) throws IllegalFormatException{
    String fileName = file.getAbsolutePath()
    int lastDotIndex = fileName.lastIndexOf(".")
    if (lastDotIndex < 0){
      throw new IllegalFormatException("File name \"$fileName\" missing extension.")
    }
    String extension = fileName.substring(lastDotIndex+1, fileName.size())
    return extension in validExtensions
  }

}
