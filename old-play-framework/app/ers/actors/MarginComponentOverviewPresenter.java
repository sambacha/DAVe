package ers.actors;

import akka.actor.UntypedActor;
import com.avaje.ebean.*;
import com.avaje.ebeaninternal.util.DefaultExpressionList;
import models.MarginComponent;

import java.util.*;

/**
 * Created by schojak on 11.12.2014.
 */
public class MarginComponentOverviewPresenter extends UntypedActor {
    private Map<String, MarginComponent> mcMap = new HashMap();

    public void onReceive(Object message) {
        System.out.println(String.format("==============> MC overview presenter received %s ", message.toString()));
        if (message instanceof MarginComponent) {
            MarginComponent mc = (MarginComponent)message;
            mcMap.put(mc.functionalKey(), mc);
        } else if (message instanceof Map) {
            Map<String, Object> where = (Map<String, Object>)message;

            String sql = "SELECT id, clearer, member, account, clss, ccy, txn_tm, biz_dt, req_id, rpt_id, ses_id, variation_margin, premium_margin, liqui_margin, spread_margin, additional_margin, received FROM MARGIN_COMPONENT_LATEST";

            RawSql rawSql = RawSqlBuilder.parse(sql).create();
            Query<MarginComponent> query = Ebean.find(MarginComponent.class);
            query.setRawSql(rawSql).where().allEq(where);

            List<MarginComponent> matching = query.findList();

            //List<MarginComponent> matching = MarginComponent.find.where().allEq(query).findList();

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
