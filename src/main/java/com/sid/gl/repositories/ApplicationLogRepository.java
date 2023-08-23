package com.sid.gl.repositories;

import com.sid.gl.model.ApplicationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationLogRepository extends JpaRepository<ApplicationLog,Long> {
}
