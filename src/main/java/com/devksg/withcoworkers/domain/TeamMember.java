package com.devksg.withcoworkers.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "user_id"}))
@Getter
@NoArgsConstructor
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamMemberStatus status;

    @Builder
    public TeamMember(Team team, User user, TeamMemberRole role, TeamMemberStatus status) {
        this.team = team;
        this.user = user;
        this.role = role != null ? role : TeamMemberRole.MEMBER;
        this.status = status != null ? status : TeamMemberStatus.PENDING;
    }

    public void approve() {
        this.status = TeamMemberStatus.APPROVED;
    }
}
