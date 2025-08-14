package com.vpgh.dms.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vpgh.dms.model.dto.UserDTO;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserDTOSerializer extends JsonSerializer<User> {

    @Autowired
    private UserService userService;

    @Override
    public void serialize(User user, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (user == null) {
            gen.writeNull();
            return;
        }
        UserDTO dto = this.userService.convertUserToUserDTO(user);

        gen.writeStartObject();
        gen.writeNumberField("id", dto.getId());
        gen.writeStringField("email", dto.getEmail());
        gen.writeStringField("firstName", dto.getFirstName());
        gen.writeStringField("lastName", dto.getLastName());
        gen.writeStringField("avatar", dto.getAvatar());
        gen.writeObjectField("role", dto.getRole());
        gen.writeEndObject();
    }
}