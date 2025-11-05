package com.example.backend.config;

import com.example.backend.domain.enums.UserRole;
import com.example.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(noRollbackFor = Exception.class)
    public void run(String... args) {
        try {
            initAdminUser();
        } catch (Exception e) {
            System.err.println("✗ Failed to initialize admin user: " + e.getMessage());
            // Don't rethrow to avoid breaking application startup
            // Admin user initialization failure should not prevent app from starting
        }
    }

    private void initAdminUser() {
        // Kiểm tra xem admin user với ID=1 đã tồn tại chưa
        boolean adminExists = userRepository.findById(1)
                .map(user -> user.getRole() == UserRole.ADMIN && 
                             "admin".equalsIgnoreCase(user.getUsername()))
                .orElse(false);

        if (!adminExists) {
            // Kiểm tra xem có user nào với username "admin" không
            boolean adminUsernameExists = userRepository.findByUsernameIgnoreCase("admin").isPresent();
            
            if (!adminUsernameExists) {
                try {
                    // Hash password
                    String hashedPassword = passwordEncoder.encode("123456");
                    
                    // Insert admin user với ID=1
                    // Sử dụng OVERRIDING SYSTEM VALUE để insert với ID cụ thể
                    int rowsAffected = entityManager.createNativeQuery(
                        "INSERT INTO users (user_id, username, email, password_hash, phone_number, role, created_at) " +
                        "OVERRIDING SYSTEM VALUE " +
                        "VALUES (1, 'admin', 'trieuttse184410@fpt.edu.vn', :password, '02873005588', 'ADMIN'::user_role, NOW()) " +
                        "ON CONFLICT (user_id) DO NOTHING"
                    )
                    .setParameter("password", hashedPassword)
                    .executeUpdate();
                    
                    if (rowsAffected > 0) {
                        System.out.println("✓ Admin user initialized successfully with ID=1");
                        // Update sequence in a separate transaction to avoid rollback issues
                        updateSequence();
                    } else {
                        System.out.println("⚠ Admin user already exists (conflict)");
                    }
                } catch (Exception e) {
                    System.err.println("✗ Error initializing admin user: " + e.getMessage());
                    e.printStackTrace();
                    // Don't rethrow - allow app to continue even if admin user init fails
                }
            } else {
                System.out.println("⚠ Admin username already exists");
            }
        } else {
            System.out.println("✓ Admin user with ID=1 already exists");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    private void updateSequence() {
        try {
            // Tìm sequence name tự động từ PostgreSQL
            String sequenceName = (String) entityManager.createNativeQuery(
                "SELECT pg_get_serial_sequence('users', 'user_id')"
            ).getSingleResult();
            
            // Nếu không tìm thấy, dùng tên mặc định
            if (sequenceName == null || sequenceName.isEmpty()) {
                sequenceName = "users_user_id_seq";
            }
            
            // Lấy MAX user_id
            Object maxUserIdObj = entityManager.createNativeQuery(
                "SELECT MAX(user_id) FROM users"
            ).getSingleResult();
            
            // Xử lý cả Integer và Long
            Integer maxUserId = null;
            if (maxUserIdObj instanceof Integer) {
                maxUserId = (Integer) maxUserIdObj;
            } else if (maxUserIdObj instanceof Long) {
                maxUserId = ((Long) maxUserIdObj).intValue();
            } else if (maxUserIdObj instanceof Number) {
                maxUserId = ((Number) maxUserIdObj).intValue();
            }
            
            if (maxUserId != null && maxUserId >= 1) {
                int nextValue = Math.max(maxUserId, 1);
                // setval() trả về giá trị nên phải dùng getSingleResult() thay vì executeUpdate()
                entityManager.createNativeQuery(
                    "SELECT setval('" + sequenceName + "', " + nextValue + ", true)"
                ).getSingleResult();
                System.out.println("✓ Sequence updated to continue from " + (nextValue + 1));
            }
        } catch (Exception seqEx) {
            // Nếu set sequence thất bại, chỉ log warning - không ảnh hưởng đến việc tạo admin user
            System.out.println("⚠ Sequence update failed (non-critical): " + seqEx.getMessage());
        }
    }
}

