package com.itmentorcommunityplatform.mentorservice.docs;

import com.itmentorcommunityplatform.mentorservice.dto.GuaranteedReviewPriceResponse;
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
        summary = "Add price for guaranteed review",
        description = "Creates a guaranteed review price if it does not exist"
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Guaranteed review price already exists",
                content = @Content
        ),
        @ApiResponse(
                responseCode = "201",
                description = "Guaranteed review price created successfully",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = GuaranteedReviewPriceResponse.class
                        )
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
                description = "Profile or mentor not found",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = """
                                {
                                   "message": "Mentor not found"
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

public @interface PostAddPricesForGuaranteedReviews {
}
