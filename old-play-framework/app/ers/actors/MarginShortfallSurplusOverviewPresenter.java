package ers.actors;

import akka.actor.UntypedActor;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import models.MarginShortfallSurplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by schojak on 11.12.2014.
 */
public class MarginShortfallSurplusOverviewPresenter extends UntypedActor {
    private Map<String, MarginShortfallSurplus> mssMap = new HashMap();

    public void onReceive(Object message) {
        System.out.println(String.format("==============> MSS overview presenter received %s ", message.toString()));
        if (message instanceof MarginShortfallSurplus) {
            MarginShortfallSurplus mss = (MarginShortfallSurplus)message;
            mssMap.put(mss.functionalKey(), mss);
        } else if (message instanceof Map) {
            Map<String, Object> where = (Map<String, Object>)message;

            String sql = "SELECT id, clearer, pool, pool_type, member, clearing_ccy, ccy, txn_tm, biz_dt, req_id, rpt_id, ses_id, margin_requirement, security_collateral, cash_balance, shortfall_surplus, margin_call, received FROM MARGIN_SHORTFALL_SURPLUS_LATEST";

            RawSql rawSql = RawSqlBuilder.parse(sql).create();
            Query<MarginShortfallSurplus> query = Ebean.find(MarginShortfallSurplus.class);
            query.setRawSql(rawSql).where().allEq(where);

            List<MarginShortfallSurplus> matching = query.findList();

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
