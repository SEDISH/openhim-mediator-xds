package org.openhim.mediator.dsub.pull;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.bw_2.UnableToGetMessagesFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.w3c.dom.DOMException;

public interface PullPoint {

    List<String> getDocumentIds();

    List<String> getDocumentIds(Integer maxDocumentIds);
    
    void registerDocument(String documentId);

	void registerDocument(String documentId, String metadata);

    List<NotificationMessageHolderType> getMessages(Integer max) throws UnableToGetMessagesFault, ResourceUnknownFault, ParserConfigurationException, DOMException, UnsupportedEncodingException;

}
