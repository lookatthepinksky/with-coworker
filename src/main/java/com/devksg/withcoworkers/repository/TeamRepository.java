package com.devksg.withcoworkers.repository;

import com.devksg.withcoworkers.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
