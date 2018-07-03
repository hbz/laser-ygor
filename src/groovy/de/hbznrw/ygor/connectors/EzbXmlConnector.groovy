package de.hbznrw.ygor.connectors

import groovy.util.logging.Log4j
import groovy.util.slurpersupport.GPathResult
import de.hbznrw.ygor.processing.Envelope
import de.hbznrw.ygor.enums.*
import de.hbznrw.ygor.interfaces.*


/**
 * Controlling API calls using http://ezb.uni-regensburg.de
 */
@Log4j
class EzbXmlConnector extends AbstractConnector {
	
    static final QUERY_XML_IS  = "jq_type1=IS&jq_term1="
    static final QUERY_XML_ZDB = "jq_type1=ZD&jq_term1="
    
	private String requestUrl       = "http://rzblx1.uni-regensburg.de/ezeit/searchres.phtml?bibid=HBZ"
    
    private String formatIdentifier = 'xmloutput=1&xmlv=3'
    private GPathResult response


	EzbXmlConnector(BridgeInterface bridge) {
		super(bridge)
	}
	

    @Override
    String getAPIQuery(String identifier, String queryIdentifier) {
        return requestUrl + "&" + formatIdentifier + "&" + queryIdentifier + identifier
    }
    
	@Override
	def poll(String identifier, String queryIdentifier) {
		
		try {
            String q = getAPIQuery(identifier, queryIdentifier)
            
            log.info("polling(): " + q)
			String text = new URL(q).getText()
            
			response = new XmlSlurper().parseText(text)
			
		} catch(Exception e) {
            log.error(e)
		}
        // TODO fix return value
	}
         
    @Override
    Envelope query(Query query) {
        try {
            getEnvelope(query)
        } catch(Exception e) {
            return getEnvelopeWithStatus(AbstractEnvelope.STATUS_ERROR)
        }
    }
    
    // FormatAdapterInterface
    
    // <ezb_page>
    //   <ezb_alphabetical_list_searchresult>
    //     <alphabetical_order>
    //       <journals>
    //         <journal jourid="64800">
    
    @Override
    Envelope getEnvelope(Query query) {
        if(response == null)
            return getEnvelopeWithStatus(AbstractEnvelope.STATUS_NO_RESPONSE)
        
        switch(query){
            case Query.EZBID:
                return getEzbID()
                break;
        }
        
        getEnvelopeWithStatus(AbstractEnvelope.STATUS_UNKNOWN_REQUEST)
    }
    
    private Envelope getEzbID() {
        def result = []
        
        response.ezb_alphabetical_list_searchresult.alphabetical_order.journals.journal.each { journal ->
            result << journal.@'jourid'
        }
        getEnvelopeWithMessage(result)
    }
}
