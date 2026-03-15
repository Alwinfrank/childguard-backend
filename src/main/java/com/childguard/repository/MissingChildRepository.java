package com.childguard.repository;

import com.childguard.model.MissingChild;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissingChildRepository extends JpaRepository<MissingChild, Long> {

    long countByStatus(String status);

}
