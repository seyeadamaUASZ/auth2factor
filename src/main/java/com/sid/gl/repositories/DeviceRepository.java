package com.sid.gl.repositories;

import com.sid.gl.model.DeviceMetadata;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<DeviceMetadata,Long> {
    List<DeviceMetadata> findByUserId(Long userId);

}
