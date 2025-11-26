package com.pg17xbootj21.service;

import com.pg17xbootj21.model.Access;
import com.pg17xbootj21.repository.AccessRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccessService {

    private final AccessRepository accessRepository;

    public AccessService(AccessRepository accessRepository) {
        this.accessRepository = accessRepository;
    }

    public List<Access> getAllAccesses() {
        return accessRepository.findAll();
    }

    public List<Access> getActiveAccessesByUserId(String userId) {
        return accessRepository.findByUserIdAndStatus(userId, "ATIVO");
    }

    public boolean hasActiveAccess(String userId, String moduleId) {
        return !accessRepository.findByUserIdAndModuleIdAndStatus(userId, moduleId, "ATIVO").isEmpty();
    }

    public List<String> getActiveModuleIds(String userId) {
        return getActiveAccessesByUserId(userId).stream()
                .map(Access::getModuleId)
                .collect(Collectors.toList());
    }

    public List<Access> getAccessesByProtocol(String userId, String protocol) {
        return accessRepository.findByUserIdAndRequestProtocolAndStatus(userId, protocol, "ATIVO");
    }
}

