package com.childguard.service;

import com.childguard.model.MissingChild;
import com.childguard.repository.MissingChildRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MissingChildService {

    private final MissingChildRepository repo;

    public MissingChildService(MissingChildRepository repo) {
        this.repo = repo;
    }

    public MissingChild add(MissingChild child) {
        return repo.save(child);
    }

    public List<MissingChild> getAll() {
        return repo.findAll();
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
