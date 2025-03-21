package com.example.shoppingCart.repository;

import com.example.shoppingCart.entity.User;
import com.example.shoppingCart.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUser(User user);

    List<UserRole> findByUserUserId(Long userId);

    List<UserRole> findByRoleRoleIdInAndUserUserId(List<Long> roleId, Long userId);

    void deleteByRoleRoleIdInAndUserUserId(List<Long> roleId, Long userId);

    void deleteByUserUserId(Long userId);

    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.userId = :userId AND ur.role.roleId = :roleId")
    void deleteByUserUserIdAndRoleRoleId(@Param("userId") Long userId, @Param("roleId") List<Long> roleId);

}
