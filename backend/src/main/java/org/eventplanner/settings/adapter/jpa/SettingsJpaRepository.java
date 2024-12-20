package org.eventplanner.settings.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsJpaRepository extends JpaRepository<SettingsJpaEntity, String> {
}
