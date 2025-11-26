package com.pg17xbootj21.repository;

import com.pg17xbootj21.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, String> {
    Optional<Request> findByProtocolAndUserId(String protocol, String userId);
    
    List<Request> findByUserId(String userId);
    
    List<Request> findByUserIdAndStatus(String userId, String status);
}

