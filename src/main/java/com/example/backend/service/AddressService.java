package com.example.backend.service;

import com.example.backend.domain.entity.Address;
import com.example.backend.domain.entity.User;
import com.example.backend.dto.address.UpdateAddressRequest;
import com.example.backend.dto.address.CreateAddressRequest;
import com.example.backend.dto.address.AddressResponse;
import com.example.backend.exception.custom.AddressAlreadyExistsException;
import com.example.backend.exception.custom.AddressNotFoundException;
import com.example.backend.repository.AddressRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    // CREATE
    public AddressResponse create(CreateAddressRequest req) {
        //Kiểm tra user tồn tại
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Kiểm tra địa chỉ trùng
        if (addressRepository.existsByUserIdAndShippingAddressLine1AndShippingCityState(
                req.getUserId(), req.getShippingAddressLine1(), req.getShippingCityState())) {
            throw new AddressAlreadyExistsException("Address already exists for this user");
        }

        //Kiểm tra xem user đã có địa chỉ nào chưa
        boolean hasAddress = addressRepository.existsByUserId(req.getUserId());

        //Nếu chưa có, đặt isDefault = true
        boolean isDefault = !hasAddress;

        Address address = Address.builder()
                .user(user)
                .fullName(req.getFullName())
                .phoneNumber(req.getPhoneNumber())
                .isDefault(isDefault)
                .shippingAddressLine1(req.getShippingAddressLine1())
                .shippingAddressLine2(req.getShippingAddressLine2())
                .shippingCityState(req.getShippingCityState())
                .isDefault(isDefault)
                // ===============
                .createdAt(OffsetDateTime.now())
                .build();

        //Lưu và trả về
        Address saved = addressRepository.save(address);
        return mapToResponse(saved);
    }


    // READ BY USER ID
    @Transactional(readOnly = true) // Thêm readOnly
    public List<AddressResponse> getAddressByUserId(Integer userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDesc(userId)
                .stream().map(this::mapToResponse).toList();
    }


    // READ ONE
    @Transactional(readOnly = true) // Thêm readOnly
    public AddressResponse getById(Integer id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException(id));
        return mapToResponse(address);
    }

    // UPDATE
    public AddressResponse update(Integer id, UpdateAddressRequest req) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException(id));

        // Update user if provided
        if (req.getUserId() != null) {
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            address.setUser(user);
        }
//
        if (req.getFullName() != null) {
            address.setFullName(req.getFullName());
        }

        if (req.getPhoneNumber() != null) {
            address.setPhoneNumber(req.getPhoneNumber());
        }

        //Xử lý logic (chỉ 1 default)
        if (Boolean.TRUE.equals(req.getIsDefault())) {
            // Nếu address này được set isDefault = true thì
            // tất cả address khác của user về false
            addressRepository.clearDefaultForUser(address.getUser().getId());
            address.setIsDefault(true);
        } else if (req.getIsDefault() != null) {
            address.setIsDefault(false);
        }
//
        if (req.getShippingAddressLine1() != null) {
            address.setShippingAddressLine1(req.getShippingAddressLine1());
        }
        if (req.getShippingAddressLine2() != null) {
            address.setShippingAddressLine2(req.getShippingAddressLine2());
        }
        if (req.getShippingCityState() != null) {
            address.setShippingCityState(req.getShippingCityState());
        }
        
        // Logic nghiệp vụ: Nếu set địa chỉ này là default
        if (req.getIsDefault() != null) {
            if (req.getIsDefault()) {
                // Set các địa chỉ khác là false
                handleSetDefault(address.getUser().getId(), address.getId());
                address.setIsDefault(true);
            } else {
                // Không cho phép set default address thành false (phải set 1 cái khác làm default)
                if (address.getIsDefault()) {
                    throw new IllegalArgumentException("Cannot set default address to false. Set another address as default instead.");
                }
                address.setIsDefault(false);
            }
        }
        // ====================

        return mapToResponse(addressRepository.save(address));
    }

    // DELETE
    public void delete(Integer id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException(id));
        
        // Logic nghiệp vụ: Không cho xóa địa chỉ default
        if (address.getIsDefault()) {
             throw new IllegalArgumentException("Cannot delete default address. Set another address as default first.");
        }

        addressRepository.deleteById(id);
    }

    // MAPPER
    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .userId(address.getUser().getId())
                .fullName(address.getFullName())
                .phoneNumber(address.getPhoneNumber())
                .isDefault(address.getIsDefault())
                .shippingAddressLine1(address.getShippingAddressLine1())
                .shippingAddressLine2(address.getShippingAddressLine2())
                .shippingCityState(address.getShippingCityState())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                // ===============
                .build();
    }

    // === HÀM HỖ TRỢ MỚI ===
    /**
     * Sets all other addresses for the user to isDefault = false.
     * @param userId The user ID
     * @param excludeAddressId The address ID to exclude (e.g., the one being updated), can be null.
     */
    private void handleSetDefault(Integer userId, Integer excludeAddressId) {
        // TODO: Bạn cần thêm phương thức này vào AddressRepository
        // @Modifying
        // @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.id != :excludeAddressId")
        // void clearOtherDefaultAddresses(@Param("userId") Integer userId, @Param("excludeAddressId") Integer excludeAddressId);
        
        // Hoặc nếu excludeAddressId là null (khi create):
        // @Modifying
        // @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
        // void clearAllDefaultAddresses(@Param("userId") Integer userId);

        // Giả lập logic (bạn cần implement trong Repository):
        List<Address> addresses = addressRepository.findByUserId(userId);
        for (Address addr : addresses) {
            if (addr.getIsDefault() && (excludeAddressId == null || !addr.getId().equals(excludeAddressId))) {
                addr.setIsDefault(false);
                addressRepository.save(addr); // Cách này không hiệu quả, nên dùng @Query
            }
        }
    }
    // =======================
}