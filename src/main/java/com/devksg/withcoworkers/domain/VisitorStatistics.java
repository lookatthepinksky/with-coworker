package com.devksg.withcoworkers.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "visitor_statistics")
@Getter
@NoArgsConstructor
public class VisitorStatistics {

    @Id
    @Column(name = "id")
    private LocalDate id;

    @Column(name = "visitor_count", nullable = false)
    private int visitorCount = 0;

    public VisitorStatistics(LocalDate date) {
        this.id = date;
        this.visitorCount = 0;
    }

    public void increment() {
        this.visitorCount++;
    }
}
