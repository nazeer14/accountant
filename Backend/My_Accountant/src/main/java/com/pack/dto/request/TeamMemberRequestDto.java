package com.pack.dto.request;

import com.pack.enums.TeamRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request payload for adding or updating a team member")
public record TeamMemberRequestDto(

        @Schema(description = "UUID of the user to add to the team", example = "123e4567-e89b-12d3-a456-426614174002")
        @NotNull(message = "User ID is required")
        UUID userId,

        @Schema(description = "Role assigned to the team member", example = "MEMBER")
        @NotNull(message = "Role is required")
        TeamRole role
) {}