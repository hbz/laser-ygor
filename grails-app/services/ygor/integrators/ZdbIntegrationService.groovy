package ygor.integrators

import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import org.apache.commons.lang.StringUtils
import ygor.Record
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.identifier.AbstractIdentifier

import java.text.SimpleDateFormat

class ZdbIntegrationService extends ExternalIntegrationService{

    ZdbIntegrationService(MappingsContainer mappingsContainer){
        super(mappingsContainer)
    }


    def integrate(MultipleProcessingThread owner, DataContainer dataContainer) {
        String processStart = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS").format(new Date())
        // owner.setProgressTotal(1) TODO: value?
        for (Record record in dataContainer.records){
            if (isApiCallMedium(record)){
                record.zdbIntegrationDate = processStart
                Map<String, String> zdbMatch = getBestExistingMatch(owner, record)
                if (zdbMatch){
                    integrateWithExisting(record, zdbMatch, mappingsContainer, MappingsContainer.ZDB)
                }
            }
        }
    }


    private Map<String, String> getBestExistingMatch(MultipleProcessingThread owner, Record record){
        List<Map<String, String>> readData = new ArrayList<>()
        for (String key in owner.KEY_ORDER) {
            AbstractIdentifier id = record."${key}"
            if (id && !StringUtils.isEmpty(id.identifier)){
                readData = owner.zdbReader.readItemData(owner.zdbKeyMapping, id.identifier)
                if (!readData.isEmpty()){
                    break
                }
            }
        }
        return getBestMatchingData(owner, record, readData, 0, MappingsContainer.ZDB, "zdbKeys")
    }
}
