package ers.actors;

import akka.camel.CamelMessage;
import akka.camel.javaapi.UntypedConsumerActor;
import ers.jaxb.FIXML;
import models.MarginComponent;
import models.TotalMarginRequirement;

/**
 * Created by schojak on 11.12.2014.
 */
public class TotalMarginRequirementProcessor extends UntypedConsumerActor {
    public String getEndpointUri() {
        return "direct:tmr";
    }

    public void onReceive(Object message) {
        System.out.println(String.format("==============> TMR processor received %s ", message.toString()));
        if (message instanceof CamelMessage) {
            CamelMessage camelMessage = (CamelMessage) message;
            TotalMarginRequirement tmr= TotalMarginRequirement.parseFromFIXML((FIXML) camelMessage.body());
            tmr.save();
            //getContext().actorFor("/user/mcOverviewPresenter").tell(mc, getSelf());
            //getContext().actorFor("/user/mcDetailPresenter").tell(mc, getSelf());
        } else {
            unhandled(message);
        }
    }

}
