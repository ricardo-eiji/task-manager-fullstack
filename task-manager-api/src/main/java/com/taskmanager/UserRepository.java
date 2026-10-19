package com.taskmanager;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer>
{
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}

// You don't write any SQL. 
// It's related to the 
// Spring Data builds the queries from the method names.