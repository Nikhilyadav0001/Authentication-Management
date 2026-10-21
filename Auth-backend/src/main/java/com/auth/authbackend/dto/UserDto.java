package com.auth.authbackend.dto;

import com.auth.authbackend.entity.Provider;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private UUID id;
    private String email;
    private String name;
    private String password;
    @Builder.Default
    private boolean enabled = true;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder.Default
    private Provider provider = Provider.LOCAL;

    @Builder.Default
    private Set<RoleDto> roles = new HashSet<>();

}
