package org.openhim.mediator.dsub.subscription;

public interface SubscriptionNotifier {
    void notifySubscription(Subscription subscription, String docId);
}
