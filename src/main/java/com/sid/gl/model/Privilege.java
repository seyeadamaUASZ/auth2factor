package com.sid.gl.model;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name="tb_privilege")
public class Privilege {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "privileges")
    private Collection<Role> roles;
}
