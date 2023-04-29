package com.sid.gl.repositories;

import com.google.common.base.Optional;
import com.sid.gl.model.User;
import com.sid.gl.model.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLocationRepository extends JpaRepository<UserLocation,Long> {
    UserLocation findByUserAndCountry(User user, String country);
    List<UserLocation> findByUser_Id(Long id);


}
