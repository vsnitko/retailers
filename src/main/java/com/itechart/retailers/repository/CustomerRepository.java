package com.itechart.retailers.repository;

import com.itechart.retailers.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {


    //Optional<Customer> findByAdminId(Long adminId);

    //Optional<Customer> findByAdminEmail(String email);

    List<Customer> findByOrderByIdDesc();

    List<Customer> findByIsActiveOrderByIdDesc(Boolean isActive);

}
