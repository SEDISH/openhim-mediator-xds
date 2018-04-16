package org.openhim.mediator.denormalization;

import akka.actor.UntypedActor;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.messages.ResolveSccRequest;
import org.openhim.mediator.messages.ResolveSccResponse;

public class SCCRequestActor extends UntypedActor {

    private MediatorConfig config;
    ResolveSccRequest originalRequest;

    public SCCRequestActor(MediatorConfig config) {
        this.config = config;
    }

    private void sendSccRequest() {
        ResolveSccResponse message = new ResolveSccResponse(originalRequest, true, "Done");
        originalRequest.getRespondTo().tell(message, getSelf());
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof ResolveSccRequest) {
            originalRequest = (ResolveSccRequest) msg;
            sendSccRequest();
        } else {
            unhandled(msg);
        }
    }
}
