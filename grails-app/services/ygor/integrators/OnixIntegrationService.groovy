package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.normalizers.DateNormalizer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.readers.KbartReaderConfiguration
import de.hbznrw.ygor.readers.Onix2Reader
import de.hbznrw.ygor.tools.DateToolkit
import ygor.Record
import ygor.field.Field
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.AbstractIdentifier

import java.time.LocalDate

class OnixIntegrationService {

    private MappingsContainer mappingsContainer


    OnixIntegrationService(MappingsContainer mappingsContainer) {
        this.mappingsContainer = mappingsContainer
    }


    def integrate(MultipleProcessingThread owner, DataContainer dataContainer) {
        Onix2Reader reader = owner.onixReader

        return
    }
}
