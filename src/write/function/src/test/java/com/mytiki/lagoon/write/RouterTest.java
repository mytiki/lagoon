/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.lambda.runtime.tests.annotations.Event;
import com.mytiki.lagoon.write.fixture.TestContext;
import com.mytiki.utils.lambda.ApiException;
import com.mytiki.utils.lambda.Router;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import software.amazon.awssdk.http.HttpStatusCode;

public class RouterTest {

    @ParameterizedTest
    @Event(value = "events/http_event.json", type = APIGatewayV2HTTPEvent.class)
    public void Router_Method_Success(APIGatewayV2HTTPEvent event) {
        APIGatewayV2HTTPEvent.RequestContext.Http http = event.getRequestContext().getHttp();
        APIGatewayV2HTTPResponse response = new Router<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>()
                .add("POST", "/my/path", (e, ctx) -> APIGatewayV2HTTPResponse.builder().withStatusCode(200).build())
                .handle(http.getMethod(), http.getPath(), event, new TestContext());
        Assertions.assertEquals(response.getStatusCode(), HttpStatusCode.OK);
    }

    @ParameterizedTest
    @Event(value = "events/http_event.json", type = APIGatewayV2HTTPEvent.class)
    public void Router_Path_Success(APIGatewayV2HTTPEvent event) {
        APIGatewayV2HTTPEvent.RequestContext.Http http = event.getRequestContext().getHttp();
        APIGatewayV2HTTPResponse response = new Router<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>()
                .add("/my/path", (e, ctx) -> APIGatewayV2HTTPResponse.builder().withStatusCode(200).build())
                .handle(http.getPath(), event, new TestContext());
        Assertions.assertEquals(response.getStatusCode(), HttpStatusCode.OK);
    }

    @ParameterizedTest
    @Event(value = "events/http_event.json", type = APIGatewayV2HTTPEvent.class)
    public void Router_Wildcard_Success(APIGatewayV2HTTPEvent event) {
        APIGatewayV2HTTPEvent.RequestContext.Http http = event.getRequestContext().getHttp();
        APIGatewayV2HTTPResponse response = new Router<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>()
                .add("POST", "/my/.*", (e, ctx) -> APIGatewayV2HTTPResponse.builder().withStatusCode(200).build())
                .handle(http.getMethod(), http.getPath(), event, new TestContext());
        Assertions.assertEquals(response.getStatusCode(), HttpStatusCode.OK);
    }

    @ParameterizedTest
    @Event(value = "events/http_event.json", type = APIGatewayV2HTTPEvent.class)
    public void Router_Add_NotFound(APIGatewayV2HTTPEvent event) {
        APIGatewayV2HTTPEvent.RequestContext.Http http = event.getRequestContext().getHttp();
        ApiException ex = Assertions.assertThrows(ApiException.class, () ->
                new Router<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>()
                        .add("POST", "/dummy", (e, ctx) -> APIGatewayV2HTTPResponse.builder().withStatusCode(200).build())
                        .handle(http.getMethod(), http.getPath(), event, new TestContext()));
        Assertions.assertEquals(HttpStatusCode.NOT_FOUND, ex.getStatus());
    }
}

