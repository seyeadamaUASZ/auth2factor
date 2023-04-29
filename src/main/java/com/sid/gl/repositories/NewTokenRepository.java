package com.sid.gl.repositories;

import com.sid.gl.model.NewLocationToken;
import com.sid.gl.model.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewTokenRepository extends JpaRepository<NewLocationToken, Long> {

    NewLocationToken findByToken(String token);

    NewLocationToken findByUserLocation(UserLocation userLocation);
}
