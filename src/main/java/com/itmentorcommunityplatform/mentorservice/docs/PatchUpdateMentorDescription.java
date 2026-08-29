package com.itmentorcommunityplatform.mentorservice.docs;

import com.itmentorcommunityplatform.mentorservice.dto.MentorResponseDto;
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
        summary = "Post request for create Mentor with description",
        description = "Request to update table Mentor/Descriptions")

@ApiResponses({
        @ApiResponse(responseCode = "200",
                description = "Mentor description updated successfully",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = MentorResponseDto.class)
                )),
        @ApiResponse(responseCode = "400",
                description = "Validation error",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = """
                                {
                                  "message": "Description name is required"
                                }
                                """)
                )),
        @ApiResponse(responseCode = "404",
                description = "Mentor not found",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = """
                                {
                                  "message": "Mentor not found!"
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

public @interface PatchUpdateMentorDescription {
}

