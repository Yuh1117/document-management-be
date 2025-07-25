package com.vpgh.dms.controller;

import com.vpgh.dms.util.exception.IdInvalidException;
import com.vpgh.dms.model.dto.UserDTO;
import com.vpgh.dms.model.dto.response.PaginationResDTO;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.dto.response.UserResDTO;
import com.vpgh.dms.service.RoleService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.annotation.ApiMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Validated
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleService roleService;

    @PostMapping(path = "/secure/users")
    @ApiMessage(message = "Tạo mới người dùng")
    public ResponseEntity<UserResDTO> create(@ModelAttribute @Validated(UserDTO.CreateGroup.class) UserDTO user) {
        User nuser = new User();
        nuser.setFirstName(user.getFirstName());
        nuser.setLastName(user.getLastName());
        nuser.setEmail(user.getEmail());
        nuser.setPassword(user.getPassword());
        nuser.setFile(user.getFile());
        nuser.setRole(this.roleService.getRoleById(user.getRoleId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResDTO(this.userService.save(nuser)));
    }

    @GetMapping(path = "/secure/users")
    @ApiMessage(message = "Lấy danh sách người dùng")
    public ResponseEntity<PaginationResDTO<List<UserResDTO>>> list(@RequestParam Map<String, String> params) {
        String page = params.get("page");
        if (page == null || page.isEmpty()) {
            params.put("page", "1");
        }

        Page<User> pageUsers = this.userService.getAllUsers(params);
        List<UserResDTO> users = pageUsers.getContent().stream().map(UserResDTO::new).collect(Collectors.toList());

        PaginationResDTO<List<UserResDTO>> results = new PaginationResDTO<>();
        results.setResult(users);
        results.setCurrentPage(pageUsers.getNumber() + 1);
        results.setTotalPages(pageUsers.getTotalPages());

        return ResponseEntity.status(HttpStatus.OK).body(results);
    }

    @GetMapping(path = "/secure/users/{id}")
    @ApiMessage(message = "Lấy chi tiết người dùng")
    public ResponseEntity<UserResDTO> detail(@PathVariable(value = "id") Integer id) throws IdInvalidException {
        User user = this.userService.getUserById(id);
        if (user == null) {
            throw new IdInvalidException("Không tìm thấy người dùng");
        }

        return ResponseEntity.status(HttpStatus.OK).body(new UserResDTO(user));
    }

    @PatchMapping(path = "/secure/users/{id}")
    @ApiMessage(message = "Cập nhật người dùng")
    public ResponseEntity<UserResDTO> update(@PathVariable(value = "id") Integer id,
                                             @ModelAttribute @Validated(UserDTO.UpdateGroup.class) UserDTO reqUser)
            throws IdInvalidException {
        User user = this.userService.getUserById(id);
        if (user == null) {
            throw new IdInvalidException("Không tìm thấy người dùng!");
        }

        if (this.userService.existsByEmailAndIdNot(reqUser.getEmail(), user.getId())) {
            throw new IdInvalidException("Email đã tồn tại");
        }

        user.setEmail(reqUser.getEmail());
        user.setPassword(reqUser.getPassword());
        user.setFirstName(reqUser.getFirstName());
        user.setLastName(reqUser.getLastName());
        user.setRole(this.roleService.getRoleById(reqUser.getRoleId()));
        user.setFile(reqUser.getFile());
        return ResponseEntity.status(HttpStatus.OK).body(new UserResDTO(this.userService.save(user)));
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
