package com.sid.gl.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.Collection;
import java.util.HashSet;

@Entity
@Table(name="tb_user")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 40)
    private String firstName;
    @Column(length = 50)
    private String lastName;

    @Email
    private String username;
    @Column(length = 60)
    private String password;
    private boolean enabled=false;
    private boolean mfa;
    private String secret;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Collection<Role> roles;

    public User(User user) {
        this.id = user.id;
        this.username = user.username;
        this.password = user.password;
        this.roles = user.roles;
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.enabled = true;
        this.roles = new HashSet<>() {{ new Role("USER"); }};
    }
}
