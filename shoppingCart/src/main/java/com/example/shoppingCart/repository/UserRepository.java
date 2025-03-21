package com.example.shoppingCart.repository;

import com.example.shoppingCart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT * FROM users " +
            "WHERE (:email IS NULL OR email = :email) " +
            "AND (:phoneNumber IS NULL OR phone_number = :phoneNumber)",
            nativeQuery = true)
    List<User> searchUsers(@Param("email") String email, @Param("phoneNumber") String phoneNumber);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.userId = :userId AND ur.role.roleId IN :roleIds")
    void deleteByUserUserIdAndRoleRoleIdIn(Long userId, List<Long> roleIds);

}