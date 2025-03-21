package com.example.shoppingCart.service;

import com.example.shoppingCart.entity.User;
import com.example.shoppingCart.entity.UserRole;
import com.example.shoppingCart.mapper.RoleMapper;
import com.example.shoppingCart.mapper.UserMapper;
import com.example.shoppingCart.model.RoleModel;
import com.example.shoppingCart.model.UserModel;
import com.example.shoppingCart.repository.UserRepository;
import com.example.shoppingCart.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleMapper roleMapper;

    public List<UserModel> getUsers(String email, String phoneNumber) {
        // Filter by parameter
        List<User> usersList = userRepository.searchUsers(email, phoneNumber);

        List<UserModel> userModelList = usersList.stream().map(user -> {
            UserModel userModel = userMapper.userToUserModel(user);
            List<UserRole> userRoles = userRoleRepository.findByUserUserId(user.getUserId());
            List<RoleModel> roleModelList = userRoles.stream().map(r -> roleMapper.rolesToRolesModel(r.getRole())).toList();
            userModel.setRoles(roleModelList);
            return userModel;
        }).toList();

        return userModelList;
    }
}