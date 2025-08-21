package com.vpgh.dms.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vpgh.dms.model.dto.UserDTO;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.UserRepository;
import com.vpgh.dms.service.RoleService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.service.specification.UserSpecification;
import com.vpgh.dms.util.PageSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;
    private final RoleService roleService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, Cloudinary cloudinary, RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinary = cloudinary;
        this.roleService = roleService;
    }

    @Override
    public User save(User user) {
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));

        if (user.getFile() != null && !user.getFile().isEmpty()) {
            try {
                Map res = cloudinary.uploader().upload(user.getFile().getBytes(),
                        ObjectUtils.asMap("folder", "dms"));
                user.setAvatar(res.get("secure_url").toString());
            } catch (IOException ex) {
                Logger.getLogger(UserServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return this.userRepository.save(user);
    }

    @Override
    public User handleCreateUser(UserDTO dto) {
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setFile(dto.getFile());
        user.setRole(this.roleService.getRoleById(dto.getRole().getId()));
        return save(user);
    }

    @Override
    public User handleUpdateUser(User user, UserDTO dto) {
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setFile(dto.getFile());
        user.setRole(this.roleService.getRoleById(dto.getRole().getId()));
        return save(user);
    }

    @Override
    public UserDTO convertUserToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAvatar(user.getAvatar());
        dto.setRole(this.roleService.convertRoleToRoleDTO(user.getRole()));
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    @Override
    public boolean existsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    @Override
    public User getUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    @Override
    public Page<User> getAllUsers(Map<String, String> params) {
        Specification<User> combinedSpec = Specification.allOf();
        Pageable pageable = Pageable.unpaged();

        if (params != null) {
            int page = Integer.parseInt(params.get("page"));
            String kw = params.get("kw");

            pageable = PageRequest.of(page - 1, PageSize.USER_PAGE_SIZE.getSize(),
                    Sort.by(Sort.Order.asc("id")));
            if (kw != null && !kw.isEmpty()) {
                Specification<User> spec = UserSpecification.filterByKeyword(params.get("kw"));
                combinedSpec = combinedSpec.and(spec);
            }
        }

        return this.userRepository.findAll(combinedSpec, pageable);
    }

    @Override
    public User getUserById(Integer id) {
        Optional<User> user = this.userRepository.findById(id);
        return user.orElse(null);
    }

    @Override
    public void deleteUserById(Integer id) {
        this.userRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Integer id) {
        return this.userRepository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public long count() {
        return this.userRepository.count();
    }

}
