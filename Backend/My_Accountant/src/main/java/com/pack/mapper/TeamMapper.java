package com.pack.mapper;

import com.pack.dto.request.TeamRequestDto;
import com.pack.dto.response.TeamMemberResponseDto;
import com.pack.dto.response.TeamResponseDto;
import com.pack.entity.Team;
import com.pack.entity.TeamMember;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {

    public TeamResponseDto toResponseDto(Team team) {

        if (team == null) {
            return null;
        }

        return new TeamResponseDto(
                team.getId(),
                team.getOwner() != null ? team.getOwner().getId() : null,
                team.getTeamName(),
                team.getInviteToken(),
                team.getActive(),
                0,// memberCount populated in service
                team.getCreatedAt(),
                team.getUpdatedAt()
        );
    }

    public Team toEntity(TeamRequestDto dto) {

        if (dto == null) {
            return null;
        }

        Team team = new Team();
        team.setTeamName(dto.teamName());

        return team;
    }

    public void updateEntityFromDto(TeamRequestDto dto, Team team) {

        if (dto == null || team == null) {
            return;
        }

        if (dto.teamName() != null) {
            team.setTeamName(dto.teamName());
        }
    }

    public TeamMemberResponseDto toMemberResponseDto(TeamMember member) {

        if (member == null) {
            return null;
        }

        return new TeamMemberResponseDto(
                member.getId() !=null ? member.getId():null,
                member.getTeam() != null ? member.getTeam().getId() : null,
                member.getTeam() != null ? member.getTeam().getTeamName() : null,
                member.getUser() != null ? member.getUser().getId() : null,
                member.getUser() != null ? member.getUser().getFullName() : null,
                member.getRole() != null? member.getRole():null,
                member.getCreatedAt()
        );
    }
}