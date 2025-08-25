package com.vpgh.dms.config;

import com.vpgh.dms.model.entity.Permission;
import com.vpgh.dms.model.entity.Role;
import com.vpgh.dms.model.entity.SystemSetting;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.PermissionService;
import com.vpgh.dms.service.RoleService;
import com.vpgh.dms.service.SystemSettingService;
import com.vpgh.dms.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataInitializer implements CommandLineRunner {
    private final RoleService roleService;
    private final UserService userService;
    private final SystemSettingService systemSettingService;
    private final PermissionService permissionService;

    public DataInitializer(RoleService roleService, UserService userService, SystemSettingService systemSettingService,
                           PermissionService permissionService) {
        this.roleService = roleService;
        this.userService = userService;
        this.systemSettingService = systemSettingService;
        this.permissionService = permissionService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("---> START INIT DATA");

        long countRoles = this.roleService.count();
        long countUser = this.userService.count();
        long countSettings = this.systemSettingService.count();
        long countPermissions = this.permissionService.count();

        if (countPermissions == 0) {
            List<Permission> arr = new ArrayList<>();
            arr.add(new Permission("Tạo mới người dùng", "/api/admin/users", "POST", "USERS"));
            arr.add(new Permission("Lấy danh sách người dùng", "/api/admin/users", "GET", "USERS"));
            arr.add(new Permission("Lấy chi tiết người dùng", "/api/admin/users/{id}", "GET", "USERS"));
            arr.add(new Permission("Cập nhật người dùng", "/api/admin/users/{id}", "PATCH", "USERS"));
            arr.add(new Permission("Xóa người dùng", "/api/admin/users/{id}", "DELETE", "USERS"));

            arr.add(new Permission("Tạo vai trò", "/api/admin/roles", "POST", "ROLES"));
            arr.add(new Permission("Lấy danh sách vai trò", "/api/admin/roles", "GET", "ROLES"));
            arr.add(new Permission("Lấy chi tiết vai trò", "/api/admin/roles/{id}", "GET", "ROLES"));
            arr.add(new Permission("Cập nhật vai trò", "/api/admin/roles/{id}", "PATCH", "ROLES"));
            arr.add(new Permission("Xóa vai trò", "/api/admin/roles/{id}", "DELETE", "ROLES"));

            arr.add(new Permission("Tạo mới quyền", "/api/admin/permissions", "POST", "PERMISSIONS"));
            arr.add(new Permission("Lấy danh sách quyền", "/api/admin/permissions", "GET", "PERMISSIONS"));
            arr.add(new Permission("Lấy chi tiết quyền", "/api/admin/permissions/{id}", "GET", "PERMISSIONS"));
            arr.add(new Permission("Cập nhật quyền", "/api/admin/permissions/{id}", "PATCH", "PERMISSIONS"));
            arr.add(new Permission("Xóa quyền", "/api/admin/permissions/{id}", "DELETE", "PERMISSIONS"));

            arr.add(new Permission("Tạo mới cài đặt", "/api/admin/settings", "POST", "SETTINGS"));
            arr.add(new Permission("Lấy danh sách cài đặt", "/api/admin/settings", "GET", "SETTINGS"));
            arr.add(new Permission("Lấy chi tiết cài đặt", "/api/admin/setting/{id}", "GET", "SETTINGS"));
            arr.add(new Permission("Cập nhật cài đặt", "/api/admin/settings/{id}", "PATCH", "SETTINGS"));
            arr.add(new Permission("Xóa cài đặt", "/api/admin/settings/{id}", "DELETE", "SETTINGS"));

            this.permissionService.saveAll(arr);
        }

        if (countRoles == 0) {
            List<Role> roles = new ArrayList<>();
            roles.add(new Role("ROLE_ADMIN", "administrator",
                    new HashSet<>(this.permissionService.getAllPermission(null).getContent())));
            roles.add(new Role("ROLE_USER", "normal user", null));
            this.roleService.saveAll(roles);
        }

        if (countUser == 0) {
            User user = new User();
            user.setFirstName("min");
            user.setLastName("ad");
            user.setEmail("admin@gmail.com");
            user.setPassword("123456");
            user.setRole(this.roleService.getRoleByName("ROLE_ADMIN"));
            this.userService.save(user);

            User user1 = new User();
            user1.setFirstName("huy");
            user1.setLastName("gia");
            user1.setEmail("huy@gmail.com");
            user1.setPassword("123456");
            user1.setRole(this.roleService.getRoleByName("ROLE_USER"));
            this.userService.save(user1);
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
            s2.setValue("text/plain;text/html;text/css;text/markdown;" +
                    "image/jpeg;image/png;image/gif;" +
                    "audio/mpeg;audio/wav;" +
                    "video/mp4;video/webm;" +
                    "application/pdf;application/zip;" +
                    "application/x-zip-compressed;application/vnd.rar;application/x-7z-compressed" +
                    "application/msword;" +
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document;" +
                    "application/vnd.ms-excel;" +
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;" +
                    "application/vnd.ms-powerpoint;" +
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation;");
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
