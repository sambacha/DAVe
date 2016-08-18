package ers.actors;

import akka.actor.UntypedActor;
import models.TradingSessionStatus;

/**
 * Created by schojak on 11.12.2014.
 */
public class TradingSessionStatusPresenter extends UntypedActor {
    private TradingSessionStatus lastTss = null;

    public void onReceive(Object message) {
        System.out.println(String.format("==============> TSS presenter received %s ", message.toString()));
        if (message instanceof TradingSessionStatus) {
            lastTss = (TradingSessionStatus)message;
        } else {
            if (lastTss != null) {
                getSender().tell(lastTss, getSelf());
            }
            else
            {
                getSender().tell(new Object(), getSelf());
            }
        }
    }

}
