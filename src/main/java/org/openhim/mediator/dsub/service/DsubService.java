package org.openhim.mediator.dsub.service;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;

import java.util.Date;
import java.util.List;

public interface DsubService {

    void createSubscription(String url, String facilityQuery, Date terminateAt);

    void deleteSubscription(String url);

    void notifyNewDocument(String docId, String facilityId);

    void newDocumentForPullPoint(String docId, String facilityId);

    List<String> getDocumentsForPullPoint(String facilityId);
}
