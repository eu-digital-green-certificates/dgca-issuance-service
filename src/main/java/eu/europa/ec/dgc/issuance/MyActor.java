package eu.europa.ec.dgc.issuance;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;

@Slf4j
public class MyActor extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object msg) {
        if (msg instanceof MediatorHTTPRequest) {
            log.info("MyActor was accessed!");
            String body = ((MediatorHTTPRequest) msg).getBody();
            FinishRequest response = new FinishRequest("A message from my new mediator: " + body,
                "text/plain", HttpStatus.SC_OK);
            ((MediatorHTTPRequest) msg).getRequestHandler().tell(response, getSelf());
        } else {
            unhandled(msg);
        }
    }
}
