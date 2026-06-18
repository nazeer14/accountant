package com.pack.dto.response;

import com.pack.enums.TeamRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Team member response payload")
public record TeamMemberResponseDto(

        @Schema(description = "Membership record ID")
        UUID id,

        @Schema(description = "Team ID")
        UUID teamId,

        @Schema(description = "Team name")
        String teamName,

        @Schema(description = "User ID of the member")
        UUID userId,

        @Schema(description = "Display name / full name")
        String fullName,

        @Schema(description = "Role of the member in the team")
        TeamRole role,

        @Schema(description = "When this member joined the team")
        OffsetDateTime joinedAt
) {}