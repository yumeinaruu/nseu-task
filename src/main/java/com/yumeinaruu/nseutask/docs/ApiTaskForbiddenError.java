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
        responseCode = "403",
        description = "Only the author or an admin can modify the task (code 2002)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
)
public @interface ApiTaskForbiddenError {
}
