package com.vpgh.dms.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.UserRepository;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.service.specification.UserSpecification;
import com.vpgh.dms.util.PageSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private Cloudinary cloudinary;

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
    public boolean existsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    @Override
    public User getUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }

    public static String getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return extractPrincipal(securityContext.getAuthentication());
    }

    @Override
    public Page<User> getAllUsers(Map<String, String> params) {
        int page = Integer.parseInt(params.get("page"));
        String kw = params.get("kw");

        Pageable pageable = PageRequest.of(page - 1, PageSize.USER_PAGE_SIZE.getSize());
        Specification<User> combinedSpec = Specification.allOf();
        if (kw != null && !kw.isEmpty()) {
            Specification<User> spec = UserSpecification.filterByKeyword(params.get("kw"));
            combinedSpec = combinedSpec.and(spec);
        }

        return this.userRepository.findAll(combinedSpec, pageable);
    }

    @Override
    public User getUserById(Integer id) {
        Optional<User> user = this.userRepository.findById(id);
        return user.isPresent() ? user.get() : null;
    }

    @Override
    public void deleteUserById(Integer id) {
        this.userRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Integer id) {
        return this.userRepository.existsByEmailAndIdNot(email, id);
    }
}
