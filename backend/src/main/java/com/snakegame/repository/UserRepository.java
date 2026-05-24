package com.snakegame.repository;

import com.snakegame.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIpAddress(String ipAddress);
    List<User> findTop100ByOrderByScoreDesc();
    List<User> findTop100ByOrderByMaxScoreDesc();
    List<User> findAllByOrderByIdAsc();
}
