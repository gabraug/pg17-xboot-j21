package com.pg17xbootj21.repository;

import com.pg17xbootj21.model.Access;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessRepository extends JpaRepository<Access, Long> {
    List<Access> findByUserIdAndStatus(String userId, String status);
    
    List<Access> findByUserIdAndModuleIdAndStatus(String userId, String moduleId, String status);
    
    List<Access> findByUserIdAndRequestProtocolAndStatus(String userId, String requestProtocol, String status);
    
    @Modifying
    @Query("UPDATE Access a SET a.status = :newStatus WHERE a.userId = :userId AND a.requestProtocol = :protocol AND a.status = :oldStatus")
    void updateStatusByUserIdAndProtocol(@Param("userId") String userId, @Param("protocol") String protocol, @Param("oldStatus") String oldStatus, @Param("newStatus") String newStatus);
}

