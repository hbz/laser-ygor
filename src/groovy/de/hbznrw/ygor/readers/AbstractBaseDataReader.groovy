package de.hbznrw.ygor.readers

import java.time.LocalDate

/**
 * Common class for all kinds of Readers that might be necessary at the point where the kind of base data files
 * to be read is still unknown.
 */
abstract class AbstractBaseDataReader{

  abstract Map<String, String> readItemData(LocalDate lastPackageUpdate, boolean ignoreLastChanged);

}
