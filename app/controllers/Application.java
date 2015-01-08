package controllers;

import akka.actor.ActorSelection;
import models.TradingSessionStatus;
import play.libs.Json;
import play.libs.Akka;
import play.libs.F;
import play.mvc.*;

import java.util.HashMap;

import static akka.pattern.Patterns.ask;

public class Application extends Controller {

    /*public static Result index() {
        return redirect(routes.Application.tss());
        //return ok(index.render("Your new application is ready."));
    }*/

    public static F.Promise<Result> tss() {
        ActorSelection tssActor = Akka.system().actorSelection("/user/tssPresenter");

        return F.Promise.wrap(ask(tssActor, new Object(), 1000)).map(
                new F.Function<Object, Result>() {
                    public Result apply(Object response) {
                        if (response instanceof TradingSessionStatus) {
                            return ok(Json.toJson(response));
                        }
                        else
                        {
                            return noContent();
                        }
                    }
                }
        );
    }

    public static F.Promise<Result> mcOverview() {
        return mcOverviewWithClearerWithMemberWithAccountWithClass("*", "*", "*", "*");
    }

    public static F.Promise<Result> mcOverviewWithClearer(String clearer) {
        return mcOverviewWithClearerWithMemberWithAccountWithClass(clearer, "*", "*", "*");
    }

    public static F.Promise<Result> mcOverviewWithClearerWithMember(String clearer, String member) {
        return mcOverviewWithClearerWithMemberWithAccountWithClass(clearer, member, "*", "*");
    }

    public static F.Promise<Result> mcOverviewWithClearerWithMemberWithAccount(String clearer, String member, String account) {
        return mcOverviewWithClearerWithMemberWithAccountWithClass(clearer, member, account, "*");
    }

    public static F.Promise<Result> mcOverviewWithClearerWithMemberWithAccountWithClass(String clearer, String member, String account, String clss) {
        ActorSelection mcActor = Akka.system().actorSelection("/user/mcOverviewPresenter");

        HashMap<String, String> query = new HashMap<>();

        if (!clearer.isEmpty() && !clearer.equals("*")) { query.put("clearer", clearer); }
        if (!member.isEmpty() && !member.equals("*")) { query.put("member", member); }
        if (!account.isEmpty() && !account.equals("*")) { query.put("account", account); }
        if (!clss.isEmpty() && !clss.equals("*")) { query.put("clss", clss); }

        return F.Promise.wrap(ask(mcActor, query, 1000)).map(
                new F.Function<Object, Result>() {
                    public Result apply(Object response) {
                        return ok(Json.toJson(response));
                    }
                }
        );
    }

    public static F.Promise<Result> mcDetail(String clearer, String member, String account, String clss, String ccy) {
        ActorSelection mcActor = Akka.system().actorSelection("/user/mcDetailPresenter");

        HashMap<String, String> query = new HashMap<>();

        if (!clearer.isEmpty() && !clearer.equals("*")) { query.put("clearer", clearer); }
        if (!member.isEmpty() && !member.equals("*")) { query.put("member", member); }
        if (!account.isEmpty() && !account.equals("*")) { query.put("account", account); }
        if (!clss.isEmpty() && !clss.equals("*")) { query.put("clss", clss); }
        if (!ccy.isEmpty() && !ccy.equals("*")) { query.put("ccy", ccy); }

        return F.Promise.wrap(ask(mcActor, query, 1000)).map(
                new F.Function<Object, Result>() {
                    public Result apply(Object response) {
                        return ok(Json.toJson(response));
                    }
                }
        );
    }

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
