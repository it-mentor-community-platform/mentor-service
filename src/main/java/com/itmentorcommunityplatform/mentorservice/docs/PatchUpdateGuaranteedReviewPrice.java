package com.itmentorcommunityplatform.mentorservice.docs;

import com.itmentorcommunityplatform.mentorservice.domain.GuaranteedReviewsPrices;
import com.itmentorcommunityplatform.mentorservice.dto.ApiMessageResponse;
import com.itmentorcommunityplatform.mentorservice.dto.UpdatePriceForGuaranteedReview;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Update guaranteed review price for current mentor",
        description = "Updates the guaranteed review price for the currently authenticated mentor. The price is updated for a specific project type and programming language combination.",
        requestBody = @RequestBody(
                required = true,
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UpdatePriceForGuaranteedReview.class)
                )
        )
)
@ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Guaranteed review price updated successfully",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = GuaranteedReviewsPrices.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Request validation error",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiMessageResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "Invalid price",
                                        value = """
                                                {
                                                  "message": "Price must be not null and great that zero"
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
                        schema = @Schema(implementation = ApiMessageResponse.class),
                        examples = @ExampleObject(
                                value = """
                                        {
                                          "message": "The current user is not a mentor or is attempting to update the price of another user's review."
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Review not found",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiMessageResponse.class),
                        examples = @ExampleObject(
                                value = """
                                        {
                                          "message": "Review by current id was not found"
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
                        schema = @Schema(implementation = ApiMessageResponse.class)
                )
        )
})
public @interface PatchUpdateGuaranteedReviewPrice {
}