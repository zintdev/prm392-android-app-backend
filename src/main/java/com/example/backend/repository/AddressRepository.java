package com.example.backend.repository;

import com.example.backend.domain.entity.Address;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    
    List<Address> findByUserId(Integer userId);
    
    boolean existsByUserIdAndShippingAddressLine1AndShippingCityState(
            Integer userId, String addressLine1, String cityState);

    List<Address> findByUserIdOrderByIsDefaultDesc(Integer userId);

    boolean existsByUserId(@NotNull Integer userId);

    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.isDefault = true")
    void clearDefaultForUser(@Param("userId") Integer userId);
}
