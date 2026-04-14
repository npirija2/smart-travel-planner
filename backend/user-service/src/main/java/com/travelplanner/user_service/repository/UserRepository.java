package com.travelplanner.user_service.repository;

import com.travelplanner.user_service.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
@EntityGraph(attributePaths = {"preferences"})
List<User> findAll();
}
