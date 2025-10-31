package com.example.backend.repository;

import com.example.backend.domain.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    
    List<Address> findByUserId(Integer userId);
    
    boolean existsByUserIdAndShippingAddressLine1AndShippingCityState(
            Integer userId, String addressLine1, String cityState);
}
