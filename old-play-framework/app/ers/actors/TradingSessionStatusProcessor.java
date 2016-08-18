package ers.actors;

import akka.camel.CamelMessage;
import akka.camel.javaapi.UntypedConsumerActor;
import ers.jaxb.FIXML;
import models.TradingSessionStatus;

/**
 * Created by schojak on 11.12.2014.
 */
public class TradingSessionStatusProcessor extends UntypedConsumerActor {
    public String getEndpointUri() {
        return "direct:tss";
    }

    public void onReceive(Object message) {
        System.out.println(String.format("==============> TSS processor received %s ", message.toString()));
        if (message instanceof CamelMessage) {
            CamelMessage camelMessage = (CamelMessage) message;
            TradingSessionStatus tss = TradingSessionStatus.parseFromFIXML((FIXML)camelMessage.body());
            tss.save();
            getContext().actorFor("/user/tssPresenter").tell(tss, getSelf());
        } else {
            unhandled(message);
        }
    }

}
