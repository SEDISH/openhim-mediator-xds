package org.openhim.mediator.dsub.subscription;

import org.openhim.mediator.engine.MediatorConfig;

public class SoapSubscriptionNotifier implements SubscriptionNotifier {

    private static final String SOAP_NAMESPACE = "org.oasis_open.docs.wsn.b_2";
    private MediatorConfig config;

    public SoapSubscriptionNotifier(MediatorConfig config) {
        this.config = config;
    }

    @Override
    public void notifySubscription(Subscription subscription, String docId) {
        // TODO: SOAP client call
    }
}
