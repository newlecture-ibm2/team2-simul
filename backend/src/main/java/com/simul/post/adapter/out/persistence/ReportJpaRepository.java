package com.simul.post.adapter.out.persistence;

import com.simul.post.domain.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportJpaRepository extends JpaRepository<Report, UUID> {
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
