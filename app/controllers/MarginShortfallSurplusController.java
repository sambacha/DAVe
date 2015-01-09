package controllers;

import akka.actor.ActorSelection;
import play.libs.Akka;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.HashMap;

import static akka.pattern.Patterns.ask;

public class MarginShortfallSurplusController extends Controller {
    public static F.Promise<Result> mssOverview() {
        return mssOverviewWithClearerWithPoolWithMemberWithClearingCcy("*", "*", "*", "*");
    }

    public static F.Promise<Result> mssOverviewWithClearer(String clearer) {
        return mssOverviewWithClearerWithPoolWithMemberWithClearingCcy(clearer, "*", "*", "*");
    }

    public static F.Promise<Result> mssOverviewWithClearerWithPool(String clearer, String pool) {
        return mssOverviewWithClearerWithPoolWithMemberWithClearingCcy(clearer, pool, "*", "*");
    }

    public static F.Promise<Result> mssOverviewWithClearerWithPoolWithMember(String clearer, String pool, String member) {
        return mssOverviewWithClearerWithPoolWithMemberWithClearingCcy(clearer, pool, member, "*");
    }

    public static F.Promise<Result> mssOverviewWithClearerWithPoolWithMemberWithClearingCcy(String clearer, String pool, String member, String clearingCcy) {
        ActorSelection mcActor = Akka.system().actorSelection("/user/mssOverviewPresenter");

        HashMap<String, String> query = new HashMap<>();

        if (!clearer.isEmpty() && !clearer.equals("*")) { query.put("clearer", clearer); }
        if (!pool.isEmpty() && !pool.equals("*")) { query.put("pool", pool); }
        if (!member.isEmpty() && !member.equals("*")) { query.put("member", member); }
        if (!clearingCcy.isEmpty() && !clearingCcy.equals("*")) { query.put("account", clearingCcy); }

        return F.Promise.wrap(ask(mcActor, query, 1000)).map(
                new F.Function<Object, Result>() {
                    public Result apply(Object response) {
                        return ok(Json.toJson(response));
                    }
                }
        );
    }

    public static F.Promise<Result> mssDetail(String clearer, String pool, String member, String clearingCcy, String ccy) {
        ActorSelection mcActor = Akka.system().actorSelection("/user/mssDetailPresenter");

        HashMap<String, String> query = new HashMap<>();

        if (!clearer.isEmpty() && !clearer.equals("*")) { query.put("clearer", clearer); }
        if (!pool.isEmpty() && !pool.equals("*")) { query.put("pool", pool); }
        if (!member.isEmpty() && !member.equals("*")) { query.put("member", member); }
        if (!clearingCcy.isEmpty() && !clearingCcy.equals("*")) { query.put("clearingCcy", clearingCcy); }
        if (!ccy.isEmpty() && !ccy.equals("*")) { query.put("ccy", ccy); }

        return F.Promise.wrap(ask(mcActor, query, 1000)).map(
                new F.Function<Object, Result>() {
                    public Result apply(Object response) {
                        return ok(Json.toJson(response));
                    }
                }
        );
    }
}
