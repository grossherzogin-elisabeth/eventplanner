package org.eventplanner.positions.adapter.jpa;

import java.io.Serializable;

import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "positions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PositionJpaEntity implements Serializable {

    @Id
    @Column(name = "key", nullable = false, updatable = false)
    private String key;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "color", nullable = false)
    private String color;

    @Column(name = "prio", nullable = false)
    private int prio;

    public static PositionJpaEntity fromDomain(Position position) {
        return new PositionJpaEntity(
            position.key().value(),
            position.name(),
            position.color(),
            position.priority()
        );
    }

    public Position toDomain() {
        return new Position(new PositionKey(key), name, color, prio);
    }
}