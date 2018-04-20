package org.openhim.mediator.denormalization;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import ca.uhn.hl7v2.model.v25.message.ACK;
import ca.uhn.hl7v2.parser.PipeParser;
import org.openhim.mediator.datatypes.Acknowledgement;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.ExceptError;
import org.openhim.mediator.engine.messages.MediatorSocketRequest;
import org.openhim.mediator.engine.messages.MediatorSocketResponse;
import org.openhim.mediator.messages.ResolveSccRequest;
import org.openhim.mediator.messages.ResolveSccResponse;

import java.util.UUID;

public class SCCRequestActor extends UntypedActor {

    public static final String SCC_ORCHESTRATION_NAME = "SCC lab order";
    private MediatorConfig config;
    ResolveSccRequest originalRequest;

    public SCCRequestActor(MediatorConfig config) {
        this.config = config;
    }

    private void sendSccRequest() {
        boolean secure = config.getProperty("scc.secure").equalsIgnoreCase("true");

        int port;
        if (secure) {
            port = Integer.parseInt(config.getProperty("scc.securePort"));
        } else {
            port = Integer.parseInt(config.getProperty("scc.port"));
        }
        String host = config.getProperty("scc.host");

        String documentBody = originalRequest.getDocumentBody();

        ActorSelection connector = getContext().actorSelection(config.userPathFor("mllp-connector"));
        MediatorSocketRequest request = new MediatorSocketRequest(
                originalRequest.getRequestHandler(), getSelf(), SCC_ORCHESTRATION_NAME, UUID.randomUUID().toString(),
                host, port, documentBody, secure
        );
        connector.tell(request, getSelf());
    }

    private void processResponse(MediatorSocketResponse msg) {
        String responseBody = msg.getBody();

        ACK ack = new ACK();
        PipeParser pipeParser = new PipeParser();
        try {
            pipeParser.parse(ack, responseBody);
            Acknowledgement acknowledgement = new Acknowledgement(ack);
            ResolveSccResponse message = new ResolveSccResponse(originalRequest, acknowledgement);
            originalRequest.getRespondTo().tell(message, getSelf());
        } catch (Exception ex) {
            msg.getOriginalRequest().getRequestHandler().tell(new ExceptError(ex), getSelf());
        }

    }

    @Override
    public void onReceive(Object msg) {
        if (msg instanceof ResolveSccRequest) {
            originalRequest = (ResolveSccRequest) msg;
            sendSccRequest();
        } else if (msg instanceof MediatorSocketResponse) {
            processResponse((MediatorSocketResponse) msg);
        } else {
            unhandled(msg);
        }
    }
}
