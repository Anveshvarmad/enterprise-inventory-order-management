package com.enterprise.inventory.repository;

import com.enterprise.inventory.entity.CustomerOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long>, JpaSpecificationExecutor<CustomerOrder> {

    @Override
    @EntityGraph(attributePaths = {"customer", "items", "items.product", "items.warehouse"})
    Optional<CustomerOrder> findById(Long id);
}
