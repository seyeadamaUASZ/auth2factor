package com.sid.gl.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Collection;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name="tb_user")
@AllArgsConstructor
@Getter
@Setter
@ToString
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
    private String password;
    private boolean enabled=true;
    private boolean mfa;
    private String secret;
    private Date dateValSecret;


    @ManyToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    @JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Collection<Role> roles;

    public User(User user) {
        this.id = user.id;
        this.username = user.username;
        this.password = user.password;
        this.roles = user.roles;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.enabled = true;
    }

    public User(){
        super();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
