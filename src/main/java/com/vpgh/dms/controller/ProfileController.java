package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.UserDTO;
import com.vpgh.dms.model.dto.request.ChangePasswordReq;
import com.vpgh.dms.model.dto.request.ProfileUpdateReq;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.SecurityUtil;
import com.vpgh.dms.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Self-service account endpoints for the signed-in user (GET /secure/profile lives in AuthController). */
@RestController
@RequestMapping("/api")
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping(path = "/secure/profile")
    @ApiMessage(key = "api.profile.update", message = "Update profile")
    public ResponseEntity<UserDTO> update(@Valid @ModelAttribute ProfileUpdateReq req) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        User updated = this.userService.updateProfile(currentUser, req);
        return ResponseEntity.ok(this.userService.convertUserToUserDTO(updated));
    }

    @PatchMapping(path = "/secure/profile/password")
    @ApiMessage(key = "api.profile.changePassword", message = "Change password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordReq req) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        this.userService.changePassword(currentUser, req);
        return ResponseEntity.noContent().build();
    }
}
