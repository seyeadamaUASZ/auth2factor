package com.sid.gl.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T,E> extends JpaRepository<T,E> {
   Optional<T> findByName(String name);
}
