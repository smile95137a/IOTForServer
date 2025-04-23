package com.frontend.res.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleRes {

    private String name;

    private Long id;

    private String uid;
}
