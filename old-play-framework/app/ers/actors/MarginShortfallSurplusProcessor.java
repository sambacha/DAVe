package ers.actors;

import akka.camel.CamelMessage;
import akka.camel.javaapi.UntypedConsumerActor;
import ers.jaxb.FIXML;
import models.MarginShortfallSurplus;
import models.TotalMarginRequirement;

/**
 * Created by schojak on 11.12.2014.
 */
public class MarginShortfallSurplusProcessor extends UntypedConsumerActor {
    public String getEndpointUri() {
        return "direct:mss";
    }

    public void onReceive(Object message) {
        System.out.println(String.format("==============> MSS processor received %s ", message.toString()));
        if (message instanceof CamelMessage) {
            CamelMessage camelMessage = (CamelMessage) message;
            MarginShortfallSurplus mss = MarginShortfallSurplus.parseFromFIXML((FIXML) camelMessage.body());
            mss.save();
            //getContext().actorFor("/user/mcOverviewPresenter").tell(mc, getSelf());
            //getContext().actorFor("/user/mcDetailPresenter").tell(mc, getSelf());
        } else {
            unhandled(message);
        }
    }

}
