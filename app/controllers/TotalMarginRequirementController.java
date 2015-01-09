package controllers;

import akka.actor.ActorSelection;
import models.TradingSessionStatus;
import play.libs.Akka;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.HashMap;

import static akka.pattern.Patterns.ask;

public class TotalMarginRequirementController extends Controller {
    public static F.Promise<Result> tmrOverview() {
        return tmrOverviewWithClearerWithPoolWithMemberWithAccount("*", "*", "*", "*");
    }

    public static F.Promise<Result> tmrOverviewWithClearer(String clearer) {
        return tmrOverviewWithClearerWithPoolWithMemberWithAccount(clearer, "*", "*", "*");
    }

    public static F.Promise<Result> tmrOverviewWithClearerWithPool(String clearer, String pool) {
        return tmrOverviewWithClearerWithPoolWithMemberWithAccount(clearer, pool, "*", "*");
    }

    public static F.Promise<Result> tmrOverviewWithClearerWithPoolWithMember(String clearer, String pool, String member) {
        return tmrOverviewWithClearerWithPoolWithMemberWithAccount(clearer, pool, member, "*");
    }

    public static F.Promise<Result> tmrOverviewWithClearerWithPoolWithMemberWithAccount(String clearer, String pool, String member, String account) {
        ActorSelection mcActor = Akka.system().actorSelection("/user/tmrOverviewPresenter");

        HashMap<String, String> query = new HashMap<>();

        if (!clearer.isEmpty() && !clearer.equals("*")) { query.put("clearer", clearer); }
        if (!pool.isEmpty() && !pool.equals("*")) { query.put("pool", pool); }
        if (!member.isEmpty() && !member.equals("*")) { query.put("member", member); }
        if (!account.isEmpty() && !account.equals("*")) { query.put("account", account); }

        return F.Promise.wrap(ask(mcActor, query, 1000)).map(
                new F.Function<Object, Result>() {
                    public Result apply(Object response) {
                        return ok(Json.toJson(response));
                    }
                }
        );
    }

    public static F.Promise<Result> tmrDetail(String clearer, String pool, String member, String account, String ccy) {
        ActorSelection mcActor = Akka.system().actorSelection("/user/tmrDetailPresenter");

        HashMap<String, String> query = new HashMap<>();

        if (!clearer.isEmpty() && !clearer.equals("*")) { query.put("clearer", clearer); }
        if (!pool.isEmpty() && !pool.equals("*")) { query.put("pool", pool); }
        if (!member.isEmpty() && !member.equals("*")) { query.put("member", member); }
        if (!account.isEmpty() && !account.equals("*")) { query.put("account", account); }
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
