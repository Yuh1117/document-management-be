package com.vpgh.dms.config;

import com.vpgh.dms.model.entity.Permission;
import com.vpgh.dms.model.entity.Role;
import com.vpgh.dms.model.entity.SystemSetting;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.PermissionService;
import com.vpgh.dms.service.RoleService;
import com.vpgh.dms.service.SystemSettingService;
import com.vpgh.dms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleService roleService;
    @Autowired
    private UserService userService;
    @Autowired
    private SystemSettingService systemSettingService;
    @Autowired
    private PermissionService permissionService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("---> START INIT DATA");

        long countRoles = this.roleService.count();
        long countUser = this.userService.count();
        long countSettings = this.systemSettingService.count();
        long countPermissions = this.permissionService.count();

        if (countPermissions == 0) {
            List<Permission> arr = new ArrayList<>();
            arr.add(new Permission("Tạo mới người dùng", "/api/v1/users", "POST", "USERS"));
            arr.add(new Permission("Lấy danh sách người dùng", "/api/v1/users", "GET", "USERS"));
            arr.add(new Permission("Lấy chi tiết người dùng", "/api/v1/users/{id}", "GET", "USERS"));
            arr.add(new Permission("Cập nhật người dùng", "/api/v1/users", "PATCH", "USERS"));
            arr.add(new Permission("Xóa người dùng", "/api/v1/users/{id}", "DELETE", "USERS"));

            arr.add(new Permission("Tạo vai trò", "/api/v1/roles", "POST", "ROLES"));
            arr.add(new Permission("Lấy danh sách vai trò", "/api/v1/roles", "GET", "ROLES"));
            arr.add(new Permission("Lấy chi tiết vai trò", "/api/v1/roles/{id}", "GET", "ROLES"));
            arr.add(new Permission("Cập nhật vai trò", "/api/v1/roles", "PATCH", "ROLES"));
            arr.add(new Permission("Xóa vai trò", "/api/v1/roles/{id}", "DELETE", "ROLES"));

            arr.add(new Permission("Tạo mới quyền", "/api/v1/permissions", "POST", "PERMISSIONS"));
            arr.add(new Permission("Lấy danh sách quyền", "/api/v1/permissions", "GET", "PERMISSIONS"));
            arr.add(new Permission("Lấy chi tiết quyền", "/api/v1/permissions/{id}", "GET", "PERMISSIONS"));
            arr.add(new Permission("Cập nhật quyền", "/api/v1/permissions", "PATCH", "PERMISSIONS"));
            arr.add(new Permission("Xóa quyền", "/api/v1/permissions/{id}", "DELETE", "PERMISSIONS"));

            this.permissionService.saveAll(arr);
        }

        if (countRoles == 0) {
            List<Role> roles = new ArrayList<>();
            roles.add(new Role("ADMIN", "administrator",
                    new HashSet<>(this.permissionService.getAllPermission(null).getContent())));
            roles.add(new Role("USER", "normal user", null));
            this.roleService.saveAll(roles);
        }

        if (countUser == 0) {
            User user = new User();
            user.setFirstName("huy");
            user.setLastName("van");
            user.setEmail("admin@gmail.com");
            user.setPassword("123456");
            user.setRole(this.roleService.getRoleByName("ADMIN"));
            this.userService.save(user);
        }

        if (countSettings == 0) {
            User user = this.userService.getUserByEmail("admin@gmail.com");

            SystemSetting s1 = new SystemSetting();
            s1.setKey("maxFileSize");
            s1.setValue(String.valueOf(10 * 1024 * 1024));
            s1.setDescription("max file size");
            s1.setCreatedBy(user);

            SystemSetting s2 = new SystemSetting();
            s2.setKey("allowedFileType");
            s2.setValue("text/plain;text/html;text/css;image/jpeg;image/png;image/gif;audio/mpeg;audio/wav;video/mp4;video/webm;" +
                    "application/pdf;application/zip");
            s2.setDescription("allowed file type");
            s2.setCreatedBy(user);

            List<SystemSetting> settings = new ArrayList<>(Arrays.asList(s1, s2));
            this.systemSettingService.saveAll(settings);
        }

        if (countRoles > 0 && countUser > 0 && countSettings > 0) {
            System.out.println("---> SKIP INIT DATA");
        } else
            System.out.println("---> END INIT DATA");
    }
}
