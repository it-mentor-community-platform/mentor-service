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
@Operation(summary = "Post request for upsert Mentor with description",
        description = "Request for upsert tables Mentor, Mentor/Descriptions, Programming languages, Mentor/Programming languages, Services and Mentor/Services")

@ApiResponses({
        @ApiResponse(responseCode = "201",
                description = "Mentor created successfully",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = """
                                {
                                  "message": "Update successfully"
                                }
                                """)
                )),
        @ApiResponse(responseCode = "200",
                description = "Mentor updated successfully",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = """
                                {
                                  "message": "Created successfully"
                                }
                                """)
                )),
        @ApiResponse(responseCode = "400",
                description = "Validation error",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = """
                                {
                                  "message": "Description is required"
                                }
                                """)
                )),
        @ApiResponse(responseCode = "500",
                description = "Internal server error",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = """
                                {
                                  "message": "Internal server error"
                                }
                                """)
                ))
})

public @interface PostAddMentorWithDescription {
}
