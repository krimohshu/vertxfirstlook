package io.vertx.blog.first;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.LinkedHashMap;
import java.util.Map;

public class MyFirstVerticle extends AbstractVerticle {

    private Map<Integer, Whisky> products = new LinkedHashMap<>();

    private void createSomeData() {
        Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
        products.put(bowmore.getId(), bowmore);
        Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
        products.put(talisker.getId(), talisker);
    }

  /*  GET /api/whiskies => get all bottles (getAll)
    GET /api/whiskies/:id => get the bottle with the corresponding id (getOne)
    POST /api/whiskies => add a new bottle (addOne)
    PUT /api/whiskies/:id => update a bottle (updateOne)
    DELETE /api/whiskies/id => delete a bottle (deleteOne)*/


    @Override
    public void start(Future<Void> fut) {
        createSomeData();

        Router router = Router.router(vertx);

        //REST GET API
        router.get("/api/whiskies").handler(this::getAll);

        //REST POST API
        router.route("/api/whiskies*").handler(BodyHandler.create());
        router.post("/api/whiskies").handler(this::addOne);

        //DELETE API
        router.delete("/api/whiskies/:id").handler(this::deleteOne);


        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                    .putHeader("content-type", "text/html")
                    .end("<h1>Hello from my first Vert.x 3 application</h1>");
        });
        router.route("/assets/*").handler(StaticHandler.create("assets").setIndexPage("index.html"));


        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger("http.port", 8082), result -> {
                    if (result.succeeded()) {
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }

    private void deleteOne(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            Integer idAsInteger = Integer.valueOf(id);
            products.remove(idAsInteger);
        }
        routingContext.response().setStatusCode(204).end();
    }

    private void addOne(RoutingContext routingContext) {
        final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(), Whisky.class);
        products.put(whisky.getId(), whisky);
        routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(whisky));



    }

    private void getAll(RoutingContext ctx) {
        ctx.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(products.values()));

    }
}