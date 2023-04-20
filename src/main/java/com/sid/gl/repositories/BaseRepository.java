package com.sid.gl.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T,E> extends JpaRepository<T,E> {
    T findByName(String name);
}
