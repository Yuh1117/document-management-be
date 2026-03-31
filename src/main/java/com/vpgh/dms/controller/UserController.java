package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.UserDTO;
import com.vpgh.dms.util.exception.CustomValidationException;
import com.vpgh.dms.util.exception.NotFoundException;
import com.vpgh.dms.model.dto.response.PaginationResDTO;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.annotation.ApiMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final Validator validator;

    public UserController(UserService userService, Validator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @PostMapping(path = "/admin/users")
    @ApiMessage(key = "api.user.create", message = "Create user")
    public ResponseEntity<UserDTO> create(@ModelAttribute @Valid UserDTO reqUser) {
        User user = this.userService.handleCreateUser(reqUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertUserToUserDTO(user));
    }

    @GetMapping(path = "/admin/users")
    @ApiMessage(key = "api.user.list", message = "List users")
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

    @GetMapping(path = "/admin/users/{id}")
    @ApiMessage(key = "api.user.detail", message = "Get user details")
    public ResponseEntity<UserDTO> detail(@PathVariable(value = "id") Integer id) {
        User user = this.userService.getUserById(id);
        if (user == null) {
            throw new NotFoundException("error.user.notFound");
        }

        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertUserToUserDTO(user));
    }

    @PatchMapping(path = "/admin/users/{id}")
    @ApiMessage(key = "api.user.update", message = "Update user")
    public ResponseEntity<UserDTO> update(@PathVariable(value = "id") Integer id,
                                          @ModelAttribute UserDTO reqUser) {

        User user = this.userService.getUserById(id);
        if (user == null) {
            throw new NotFoundException("error.user.notFound");
        }

        reqUser.setId(user.getId());
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

        return ResponseEntity.status(HttpStatus.OK)
                .body(this.userService.convertUserToUserDTO(this.userService.handleUpdateUser(user, reqUser)));
    }

    @DeleteMapping(path = "/admin/users/{id}")
    @ApiMessage(key = "api.user.delete", message = "Delete user")
    public ResponseEntity<Void> delete(@PathVariable(value = "id") Integer id) {
        User user = this.userService.getUserById(id);
        if (user == null) {
            throw new NotFoundException("error.user.notFound");
        }

        this.userService.deleteUserById(user.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
