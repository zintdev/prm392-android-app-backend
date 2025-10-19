package com.example.backend.repository;

import com.example.backend.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Category> findByNameIgnoreCase(String name);

    List<Category> findByNameContainingIgnoreCase(String name);
}
