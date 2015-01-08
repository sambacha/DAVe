package ers.actors;

import akka.camel.CamelMessage;
import akka.camel.javaapi.UntypedConsumerActor;
import ers.jaxb.FIXML;
import models.MarginComponent;
import models.TradingSessionStatus;

/**
 * Created by schojak on 11.12.2014.
 */
public class MarginComponentProcessor extends UntypedConsumerActor {
    public String getEndpointUri() {
        return "direct:mc";
    }

    public void onReceive(Object message) {
        System.out.println(String.format("==============> MC processor received %s ", message.toString()));
        if (message instanceof CamelMessage) {
            CamelMessage camelMessage = (CamelMessage) message;
            MarginComponent mc = MarginComponent.parseFromFIXML((FIXML) camelMessage.body());
            mc.save();
            //getContext().actorFor("/user/mcOverviewPresenter").tell(mc, getSelf());
            //getContext().actorFor("/user/mcDetailPresenter").tell(mc, getSelf());
        } else {
            unhandled(message);
        }
    }

}
