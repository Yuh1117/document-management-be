package com.vpgh.dms.config;

import com.vpgh.dms.model.entity.Role;
import com.vpgh.dms.model.entity.SystemSetting;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.RoleService;
import com.vpgh.dms.service.SystemSettingService;
import com.vpgh.dms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleService roleService;
    @Autowired
    private UserService userService;
    @Autowired
    private SystemSettingService systemSettingService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("---> START INIT DATA");

        long countRoles = this.roleService.count();
        long countUser = this.userService.count();
        long countSettings = this.systemSettingService.count();

        if (countRoles == 0) {
            List<Role> roles = new ArrayList<>();
            roles.add(new Role("ADMIN", "administrator"));
            roles.add(new Role("USER", "normal user"));
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
            List<SystemSetting> settings = new ArrayList<>();
            settings.add(new SystemSetting("maxFileSize", String.valueOf(10 * 1024 * 1024), "max file size"));
            settings.add(new SystemSetting("allowedFileType",
                    "text/plain;text/html;text/css;image/jpeg;image/png;image/gif;audio/mpeg;audio/wav;video/mp4;video/webm;application/pdf;application/zip",
                    "allowed file type"));

            this.systemSettingService.saveAll(settings);
        }

        if (countRoles > 0 && countUser > 0 && countSettings > 0) {
            System.out.println("---> SKIP INIT DATA");
        } else
            System.out.println("---> END INIT DATA");
    }
}
