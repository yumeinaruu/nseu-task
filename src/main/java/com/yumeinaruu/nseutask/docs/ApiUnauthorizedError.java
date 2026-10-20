package com.yumeinaruu.nseutask.docs;

import com.yumeinaruu.nseutask.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "401",
        description = "Token is missing, invalid or expired (code 3003)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
)
public @interface ApiUnauthorizedError {
}
