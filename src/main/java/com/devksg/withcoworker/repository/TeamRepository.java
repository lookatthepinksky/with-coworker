package com.devksg.withcoworker.repository;

import com.devksg.withcoworker.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
