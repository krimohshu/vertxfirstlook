package io.vertx.blog.first;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.vertx.ext.unit.Async;


import java.net.ServerSocket;

import static org.junit.Assert.*;

@RunWith(VertxUnitRunner.class)
public class MyFirstVerticleTest {

    private Vertx vertx;

    Integer port ;
    ServerSocket socket = null;

    @Before
    public void setUp(TestContext context) throws Exception {
        vertx = Vertx.vertx();
        socket = new ServerSocket(0);

        port = socket.getLocalPort();
        System.out.println("Following port is free at this time for testing =" + port);
        socket.close();
        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject().put("http.port", port)
                );
        vertx.deployVerticle(MyFirstVerticle.class.getName(), options,
                context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) throws Exception {
        vertx.close(context.asyncAssertSuccess());

    }

    @Test
    public void testMyApplication(TestContext context) {
        final Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/",
                response -> {
                    response.handler(body -> {
                        context.assertTrue(body.toString().contains("Hello"));
                        async.complete();
                    });
                });
    }


}