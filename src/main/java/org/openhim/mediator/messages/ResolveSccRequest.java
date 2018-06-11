package org.openhim.mediator.messages;

import akka.actor.ActorRef;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import org.openhim.mediator.engine.messages.MediatorRequestMessage;

public class ResolveSccRequest extends MediatorRequestMessage {

    private final ProvideAndRegisterDocumentSetRequestType.Document document;

    public ResolveSccRequest(ActorRef requestHandler, ActorRef respondTo, String correlationId,
                             ProvideAndRegisterDocumentSetRequestType.Document document) {
        super(requestHandler, respondTo, "resolve-scc", correlationId);
        this.document = document;
    }

    public String getDocumentBody() {
        return (String) document.getContent().get(0);
    }
}
