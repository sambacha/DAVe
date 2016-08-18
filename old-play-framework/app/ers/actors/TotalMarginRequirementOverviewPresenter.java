package ers.actors;

import akka.actor.UntypedActor;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import models.MarginComponent;
import models.TotalMarginRequirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by schojak on 11.12.2014.
 */
public class TotalMarginRequirementOverviewPresenter extends UntypedActor {
    private Map<String, TotalMarginRequirement> tmrMap = new HashMap();

    public void onReceive(Object message) {
        System.out.println(String.format("==============> TMR overview presenter received %s ", message.toString()));
        if (message instanceof TotalMarginRequirement) {
            TotalMarginRequirement tmr = (TotalMarginRequirement)message;
            tmrMap.put(tmr.functionalKey(), tmr);
        } else if (message instanceof Map) {
            Map<String, Object> where = (Map<String, Object>)message;

            String sql = "SELECT id, clearer, pool, member, account, ccy, txn_tm, biz_dt, req_id, rpt_id, ses_id, adjusted_margin, unadjusted_margin, received FROM TOTAL_MARGIN_REQUIREMENT_LATEST";

            RawSql rawSql = RawSqlBuilder.parse(sql).create();
            Query<TotalMarginRequirement> query = Ebean.find(TotalMarginRequirement.class);
            query.setRawSql(rawSql).where().allEq(where);

            List<TotalMarginRequirement> matching = query.findList();

            //List<MarginComponent> matching = MarginComponent.find.where().allEq(query).findList();

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
