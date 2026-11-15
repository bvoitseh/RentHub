package com.bohakka.renthub.admin.repository;

import com.bohakka.renthub.admin.domain.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<Settings, Integer> {
}
