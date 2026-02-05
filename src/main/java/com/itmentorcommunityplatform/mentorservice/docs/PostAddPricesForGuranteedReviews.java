package com.itmentorcommunityplatform.mentorservice.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
        summary = "Post request for upsert Mentor and Price",
        description = "Request for upsert table Mentor/Price for guaranteed reviews"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Resource update successfully",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = """
                                        {
                                          "message": "Resource update successfully"
                                        }
                                """)
                )
        ),
        @ApiResponse(
                responseCode = "201",
                description = "Resource created successfully",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = """
                                       {
                                         "message": "Resource created successfully"
                                       }
                                """)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Telegram profile url incorrect",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = """
                                {
                                  "message": "Telegram profile url incorrect"
                                }
                                """)
                )),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid project type",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = """
                                {
                                   "message": "Invalid project type MOTION"
                                }
                                """)
                ))
        ,
        @ApiResponse(
                responseCode = "404",
                description = "Telegram url not found",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = """
                                {
                                   "message": "Telegram url not found"
                                }
                                """)
                )),
        @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = """
                                {
                                   "message": "Internal server error"
                                }
                                """)
                ))
}
)

public @interface PostAddPricesForGuranteedReviews {
}
