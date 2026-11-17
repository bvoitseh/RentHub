package com.bohakka.renthub.repository;

import com.bohakka.renthub.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<Settings, Integer> {
}
