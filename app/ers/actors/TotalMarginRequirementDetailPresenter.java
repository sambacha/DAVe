package ers.actors;

import akka.actor.UntypedActor;
import models.TotalMarginRequirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by schojak on 11.12.2014.
 */
public class TotalMarginRequirementDetailPresenter extends UntypedActor {
    private Map<String, TotalMarginRequirement> tmrMap = new HashMap();

    public void onReceive(Object message) {
        System.out.println(String.format("==============> TMR detail presenter received %s ", message.toString()));
        if (message instanceof TotalMarginRequirement) {
            TotalMarginRequirement tmr = (TotalMarginRequirement)message;
            tmrMap.put(tmr.functionalKey(), tmr);
        } else if (message instanceof Map) {
            Map<String, Object> where = (Map<String, Object>)message;
            List<TotalMarginRequirement> matching = TotalMarginRequirement.find.where().allEq(where).findList();

            if (matching != null) {
                getSender().tell(matching, getSelf());
            }
            else
            {
                getSender().tell(new ArrayList<TotalMarginRequirement>(), getSelf());
            }
        }
    }

}
