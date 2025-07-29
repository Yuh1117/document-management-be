package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.UserDTO;
import com.vpgh.dms.util.exception.CustomValidationException;
import com.vpgh.dms.util.exception.IdInvalidException;
import com.vpgh.dms.model.dto.response.PaginationResDTO;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.RoleService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.annotation.ApiMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleService roleService;
    @Autowired
    private Validator validator;

    @PostMapping(path = "/secure/users")
    @ApiMessage(message = "Tạo mới người dùng")
    public ResponseEntity<UserDTO> create(@ModelAttribute @Valid UserDTO reqUser) {
        User user = this.userService.handleCreateUser(reqUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertUserToUserDTO(user));
    }

    @GetMapping(path = "/secure/users")
    @ApiMessage(message = "Lấy danh sách người dùng")
    public ResponseEntity<PaginationResDTO<List<UserDTO>>> list(@RequestParam Map<String, String> params) {
        String page = params.get("page");
        if (page == null || page.isEmpty()) {
            params.put("page", "1");
        }

        Page<User> pageUsers = this.userService.getAllUsers(params);
        List<UserDTO> users = pageUsers.getContent().stream().map(u -> this.userService.convertUserToUserDTO(u))
                .collect(Collectors.toList());

        PaginationResDTO<List<UserDTO>> results = new PaginationResDTO<>();
        results.setResult(users);
        results.setCurrentPage(pageUsers.getNumber() + 1);
        results.setTotalPages(pageUsers.getTotalPages());

        return ResponseEntity.status(HttpStatus.OK).body(results);
    }

    @GetMapping(path = "/secure/users/{id}")
    @ApiMessage(message = "Lấy chi tiết người dùng")
    public ResponseEntity<UserDTO> detail(@PathVariable(value = "id") Integer id) throws IdInvalidException {
        User user = this.userService.getUserById(id);
        if (user == null) {
            throw new IdInvalidException("Không tìm thấy người dùng");
        }

        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertUserToUserDTO(user));
    }

    @PatchMapping(path = "/secure/users/{id}")
    @ApiMessage(message = "Cập nhật người dùng")
    public ResponseEntity<UserDTO> update(@PathVariable(value = "id") Integer id,
                                          @ModelAttribute UserDTO reqUser) throws IdInvalidException {
        reqUser.setId(id);
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(reqUser);

        if (!violations.isEmpty()) {
            List<Map<String, String>> errorList = violations.stream().map(v -> {
                Map<String, String> err = new HashMap<>();
                err.put("field", v.getPropertyPath().toString());
                err.put("message", v.getMessage());
                return err;
            }).collect(Collectors.toList());
            throw new CustomValidationException(errorList);
        }

        User user = this.userService.handleUpdateUser(id, reqUser);
        if (user == null) {
            throw new IdInvalidException("Không tìm thấy người dùng!");
        }

        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertUserToUserDTO(user));
    }

    @DeleteMapping(path = "/secure/users/{id}")
    @ApiMessage(message = "Xóa người dùng")
    public ResponseEntity<Void> delete(@PathVariable(value = "id") Integer id) throws IdInvalidException {
        User user = this.userService.getUserById(id);
        if (user == null) {
            throw new IdInvalidException("Không tìm thấy người dùng!");
        }

        this.userService.deleteUserById(user.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
