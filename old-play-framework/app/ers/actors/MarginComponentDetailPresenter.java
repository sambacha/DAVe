package ers.actors;

import akka.actor.UntypedActor;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import models.MarginComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by schojak on 11.12.2014.
 */
public class MarginComponentDetailPresenter extends UntypedActor {
    private Map<String, MarginComponent> mcMap = new HashMap();

    public void onReceive(Object message) {
        System.out.println(String.format("==============> MC detail presenter received %s ", message.toString()));
        if (message instanceof MarginComponent) {
            MarginComponent mc = (MarginComponent)message;
            mcMap.put(mc.functionalKey(), mc);
        } else if (message instanceof Map) {
            Map<String, Object> where = (Map<String, Object>)message;
            List<MarginComponent> matching = MarginComponent.find.where().allEq(where).findList();

            if (matching != null) {
                getSender().tell(matching, getSelf());
            }
            else
            {
                getSender().tell(new ArrayList<MarginComponent>(), getSelf());
            }
        }
    }

}
