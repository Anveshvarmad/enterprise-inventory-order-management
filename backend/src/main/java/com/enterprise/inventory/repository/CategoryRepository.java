package com.enterprise.inventory.repository;

import com.enterprise.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
