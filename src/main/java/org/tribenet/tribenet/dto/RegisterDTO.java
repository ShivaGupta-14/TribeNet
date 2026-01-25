package org.tribenet.tribenet.dto;

import lombok.Data;
import org.tribenet.tribenet.model.Role;

@Data
public class RegisterDTO {
    private String name;
    private String username;
    private String email;
    private String password;
    private Role role;
}
