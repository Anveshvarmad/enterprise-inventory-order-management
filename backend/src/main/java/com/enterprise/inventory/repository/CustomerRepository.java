package com.enterprise.inventory.repository;

import com.enterprise.inventory.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findAllByOrderByFirstNameAscLastNameAsc();
}
