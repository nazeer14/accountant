package com.pack.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "teams",
        indexes = {
                @Index(name = "idx_team_invite_token", columnList = "invite_token")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "owner_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_team_owner")
    )
    private User owner;

    @Column(name = "team_name", nullable = false, length = 100)
    private String teamName;

    @Column(name = "invite_token", unique = true, length = 100)
    private String inviteToken;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}