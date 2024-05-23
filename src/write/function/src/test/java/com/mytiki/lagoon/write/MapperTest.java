/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write;
import com.mytiki.utils.lambda.ApiError;
import com.mytiki.utils.lambda.ApiException;
import com.mytiki.utils.lambda.Mapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.http.HttpStatusCode;

public class MapperTest {

    private final Mapper mapper = new Mapper();

    @Test
    public void Mapper_readValue_success() {
        String json = "{ \"id\":\"dummy\" }";
        ApiError deserialized = mapper.readValue(json, ApiError.class);
        Assertions.assertEquals("dummy", deserialized.getId());
    }

    @Test
    public void Mapper_readValue_fail() {
        String json = "{ \"invalid\" }";
        ApiException ex = Assertions.assertThrows(ApiException.class, () -> mapper.readValue(json, ApiError.class));
        Assertions.assertEquals(ex.getStatus(), HttpStatusCode.BAD_REQUEST);
    }

    @Test
    public void Mapper_writeValueAsString_success() {
        ApiError error = new ApiError();
        error.setId("dummy");
        String serialized = mapper.writeValueAsString(error);
        ApiError deserialized = mapper.readValue(serialized, ApiError.class);
        Assertions.assertEquals("dummy", deserialized.getId());
    }
}

