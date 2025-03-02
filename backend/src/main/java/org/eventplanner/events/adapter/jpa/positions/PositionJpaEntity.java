package org.eventplanner.events.adapter.jpa.positions;

import java.io.Serializable;

import org.eventplanner.events.domain.entities.Position;
import org.eventplanner.events.domain.values.PositionKey;

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

    @Column(name = "imo_list_name", nullable = false)
    private String imoListRank;

    public static PositionJpaEntity fromDomain(Position position) {
        return new PositionJpaEntity(
            position.getKey().value(),
            position.getName(),
            position.getColor(),
            position.getPriority(),
            position.getImoListRank()
        );
    }

    public Position toDomain() {
        return new Position(new PositionKey(key), name, color, prio, imoListRank);
    }
}