package com.example.backend.repository;

import com.example.backend.domain.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Integer> {
    
    /**
     * Tìm kiếm publishers theo tên (không phân biệt hoa thường)
     */
    List<Publisher> findByNameContainingIgnoreCase(String name);
}
