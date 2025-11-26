package com.pg17xbootj21.repository;

import com.pg17xbootj21.model.RequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestHistoryRepository extends JpaRepository<RequestHistory, Long> {
}

