package com.childguard.service;

import com.childguard.model.FoundChild;
import com.childguard.repository.FoundChildRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoundChildService {

    private final FoundChildRepository repo;

    public FoundChildService(FoundChildRepository repo) {
        this.repo = repo;
    }

    public FoundChild save(FoundChild child) {
        return repo.save(child);
    }

    public List<FoundChild> getAll() {
        return repo.findAll();
    }
}
