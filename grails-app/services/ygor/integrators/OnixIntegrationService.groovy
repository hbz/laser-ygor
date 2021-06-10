package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.Onix2Reader
import ygor.field.MappingsContainer

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
