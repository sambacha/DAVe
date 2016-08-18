package controllers;

import akka.actor.ActorSelection;
import models.TradingSessionStatus;
import play.libs.Json;
import play.libs.Akka;
import play.libs.F;
import play.mvc.*;

import java.util.HashMap;

import static akka.pattern.Patterns.ask;

public class TradingSessionStatusController extends Controller {
    public static F.Promise<Result> tss() {
        ActorSelection tssActor = Akka.system().actorSelection("/user/tssPresenter");

        return F.Promise.wrap(ask(tssActor, new Object(), 1000)).map(
                new F.Function<Object, Result>() {
                    public Result apply(Object response) {
                        if (response instanceof models.TradingSessionStatus) {
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
}
