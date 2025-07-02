package org.eventplanner.events.adapter.jpa.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationJpaRepository extends JpaRepository<ConfigurationJpaEntity, String> {
}
