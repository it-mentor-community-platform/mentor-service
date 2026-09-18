package com.itmentorcommunityplatform.mentorservice.docs;

import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.dto.ApiMessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Add guaranteed review price for current mentor",
        description = """
                Adds a guaranteed review price for the currently authenticated mentor.
                A mentor can have only one price for the same project type and programming language.
                """
)
@ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Guaranteed review price created successfully",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = GuaranteedReviewsPrices.class
                        )
                )
        ),

        @ApiResponse(
                responseCode = "400",
                description = "Request validation error",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = ApiMessageResponse.class
                        ),
                        examples = {
                                @ExampleObject(
                                        name = "Invalid project type",
                                        value = """
                                                {
                                                  "message": "Invalid project type MOTION"
                                                }
                                                """
                                )
                        }
                )
        ),

        @ApiResponse(
                responseCode = "403",
                description = "Current user is not a mentor",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = ApiMessageResponse.class
                        ),
                        examples = @ExampleObject(
                                value = """
                                        {
                                          "message": "Current user is not a mentor"
                                        }
                                        """
                        )
                )
        ),

        @ApiResponse(
                responseCode = "409",
                description = "Guaranteed review price already exists",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = ApiMessageResponse.class
                        ),
                        examples = @ExampleObject(
                                value = """
                                        {
                                          "message": "Guaranteed review price already exists"
                                        }
                                        """
                        )
                )
        ),

        @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = ApiMessageResponse.class
                        )
                )
        )
})
public @interface PostAddGuaranteedReviewPrice {
}