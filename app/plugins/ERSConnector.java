package plugins;

import akka.actor.*;
import akka.camel.Camel;
import akka.camel.CamelExtension;
import ers.actors.*;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import play.*;
import play.libs.Akka;

public class ERSConnector extends Plugin {
    //private final Application app;

    //private ActorRef myActor;

    public ERSConnector(Application app) {
        //this.app = app;
    }

    public void onStart() {
        try {
            ActorSystem system = Akka.system();
            //ActorRef tss = system.actorOf(Props.create(TradingSessionStatusHandler.class), "tss");
            ActorRef tssProcessor = system.actorOf(Props.create(TradingSessionStatusProcessor.class), "tss");
            ActorRef tssPresenter = system.actorOf(Props.create(TradingSessionStatusPresenter.class), "tssPresenter");

            ActorRef mcProcessor = system.actorOf(Props.create(MarginComponentProcessor.class), "mc");
            ActorRef mcOverviewPresenter = system.actorOf(Props.create(MarginComponentOverviewPresenter.class), "mcOverviewPresenter");
            ActorRef mcDetailPresenter = system.actorOf(Props.create(MarginComponentDetailPresenter.class), "mcDetailPresenter");

            ActorRef tmrProcessor = system.actorOf(Props.create(TotalMarginRequirementProcessor.class), "tmr");
            ActorRef tmrOverviewPresenter = system.actorOf(Props.create(TotalMarginRequirementOverviewPresenter.class), "tmrOverviewPresenter");
            ActorRef tmrDetailPresenter = system.actorOf(Props.create(TotalMarginRequirementDetailPresenter.class), "tmrDetailPresenter");
            //ActorRef mc = system.actorOf(Props.create(MarginComponentHandler.class), "mc");

            Camel camel = CamelExtension.get(system);
            CamelContext camelContext = camel.context();
            camelContext.addComponent("amqp", AMQPComponent.amqpComponentOld("amqp://:@MyCamelApp/?brokerlist='tcp://rgd001.xeop.de:15060?tcp_nodelay='true'&ssl='true'&ssl_cert_alias='cbkfr'&sasl_mechs='EXTERNAL'&trust_store='c:/opt/ers/truststore'&trust_store_password='123456'&key_store='c:/opt/ers/cbkfr.keystore'&key_store_password='123456'&ssl_verify_hostname='false''&sync_publish='all'"));

            final JaxbDataFormat ersDataModel = new JaxbDataFormat(true);
            ersDataModel.setContextPath("ers.jaxb");

            camelContext.addRoutes(new RouteBuilder() {
                public void configure() {
                    //String address = "response.ABCFR_ABCFRALMMACC1.response_queue_1; { create: receiver, assert: never, node: { type: queue, x-declare: { auto-delete: true, exclusive: false, arguments: { 'qpid.policy_type': ring, 'qpid.max_count': 1000, 'qpid.max_size': 1000000, 'qpid.auto_delete_timeout': 60 } }, x-bindings: [ { exchange: 'response', queue: 'response.ABCFR_ABCFRALMMACC1.response_queue_1', key: 'response.ABCFR_ABCFRALMMACC1.response_queue_1' } ] } }";
                    String tssBroadcastAddress = "eurex.tmp.CBKFR.broadcast_tss; { create: receiver, assert: never, node: { type: queue, x-declare: { auto-delete: true, exclusive: false, arguments: { 'qpid.policy_type': ring, 'qpid.max_count': 1000, 'qpid.max_size': 1000000, 'qpid.auto_delete_timeout': 60 } }, x-bindings: [ { exchange: 'eurex.broadcast', queue: 'eurex.tmp.CBKFR.broadcast_tss', key: 'public.MessageType.TradingSessionStatus.#' } ] } }";
                    String mcBroadcastAddress = "eurex.tmp.CBKFR.broadcast_mc; { create: receiver, assert: never, node: { type: queue, x-declare: { auto-delete: true, exclusive: false, arguments: { 'qpid.policy_type': ring, 'qpid.max_count': 1000, 'qpid.max_size': 1000000, 'qpid.auto_delete_timeout': 60 } }, x-bindings: [ { exchange: 'eurex.broadcast', queue: 'eurex.tmp.CBKFR.broadcast_mc', key: 'CBKFR.MessageType.MarginComponents.#' } ] } }";
                    String tmrBroadcastAddress = "eurex.tmp.CBKFR.broadcast_tmr; { create: receiver, assert: never, node: { type: queue, x-declare: { auto-delete: true, exclusive: false, arguments: { 'qpid.policy_type': ring, 'qpid.max_count': 1000, 'qpid.max_size': 1000000, 'qpid.auto_delete_timeout': 60 } }, x-bindings: [ { exchange: 'eurex.broadcast', queue: 'eurex.tmp.CBKFR.broadcast_tmr', key: 'CBKFR.MessageType.TotalMarginRequirement.#' } ] } }";

                    //from("amqp:" + tssBroadcastAddress).unmarshal(ersDataModel).process(new TradingSessionStatusProcessor()).log(body().toString()).to("direct:tss");
                    from("amqp:" + tssBroadcastAddress).unmarshal(ersDataModel).to("direct:tss");
                    //from("amqp:" + mcBroadcastAddress).unmarshal(ersDataModel).process(new MarginComponentProcessor()).log(body().toString()).to("direct:mc");
                    from("amqp:" + mcBroadcastAddress).unmarshal(ersDataModel).to("direct:mc");
                    from("amqp:" + tmrBroadcastAddress).unmarshal(ersDataModel).to("direct:tmr");

                }
            });
        }
        catch (Exception e)
        {
            // Handling exception
        }

        //myActor = Akka.system().actorOf(MyActor.props(), "my-actor");
    }

    /*public static ActorRef getMyActor() {
        //return Play.application().plugin(Actors.class).myActor;
    }*/
}