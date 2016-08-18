package ers.actors;

import akka.actor.UntypedActor;
import models.MarginShortfallSurplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by schojak on 11.12.2014.
 */
public class MarginShortfallSurplusDetailPresenter extends UntypedActor {
    private Map<String, MarginShortfallSurplus> mssMap = new HashMap();

    public void onReceive(Object message) {
        System.out.println(String.format("==============> MSS detail presenter received %s ", message.toString()));
        if (message instanceof MarginShortfallSurplus) {
            MarginShortfallSurplus mss = (MarginShortfallSurplus)message;
            mssMap.put(mss.functionalKey(), mss);
        } else if (message instanceof Map) {
            Map<String, Object> where = (Map<String, Object>)message;
            List<MarginShortfallSurplus> matching = MarginShortfallSurplus.find.where().allEq(where).findList();

            if (matching != null) {
                getSender().tell(matching, getSelf());
            }
            else
            {
                getSender().tell(new ArrayList<MarginShortfallSurplus>(), getSelf());
            }
        }
    }

}
