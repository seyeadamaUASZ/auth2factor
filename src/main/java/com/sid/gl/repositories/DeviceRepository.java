package com.sid.gl.repositories;

import com.sid.gl.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<User,Long> {
}
