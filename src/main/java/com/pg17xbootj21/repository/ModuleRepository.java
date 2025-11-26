package com.pg17xbootj21.repository;

import com.pg17xbootj21.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends JpaRepository<Module, String> {
}

