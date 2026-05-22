package com.travelplanner.user_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelplanner.user_service.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    @EntityGraph(attributePaths = {"preferences"})
    List<User> findAll();
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
