package com.sid.gl.utils;

import com.sid.gl.exceptions.Auth2factorNotFoundException;
import com.sid.gl.model.Privilege;
import com.sid.gl.model.Role;
import com.sid.gl.model.User;
import com.sid.gl.repositories.PrivilegeRepository;
import com.sid.gl.repositories.RoleRepository;
import com.sid.gl.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DataInit implements ApplicationListener<ContextRefreshedEvent> {
    private boolean alReadySetup =false;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PrivilegeRepository privilegeRepository;

    private final PasswordEncoder passwordEncoder;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alReadySetup) {
            return;
        }

        // == create initial privileges
        final Privilege readPrivilege = createPrivilegeNotFound("READ_PRIVILEGE");
        final Privilege writePrivilege = createPrivilegeNotFound("WRITE_PRIVILEGE");
        final Privilege passwordPrivilege = createPrivilegeNotFound("CHANGE_PASSWORD_PRIVILEGE");
        // == create initial roles
        final List<Privilege> adminPrivileges = new ArrayList<>(Arrays.asList(readPrivilege, writePrivilege, passwordPrivilege));
        final List<Privilege> userPrivileges = new ArrayList<>(Arrays.asList(readPrivilege, passwordPrivilege));
        final Role adminRole = createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
        createRoleIfNotFound("ROLE_USER", userPrivileges);

        // == create initial user
        createUserIfNotFound("test@test.com", "Test", "Test", "test", new ArrayList<>(Arrays.asList(adminRole)));

        alReadySetup = true;
    }


    @Transactional
    public Privilege createPrivilegeNotFound(final String name){
      Optional<Privilege> optionalPrivilege = privilegeRepository.findByName(name);
      Privilege privilege = new Privilege();
      if(optionalPrivilege.isEmpty()){
          privilege.setName(name);
          privilegeRepository.save(privilege);
      }
        return privilege;
    }

    @Transactional
    public Role createRoleIfNotFound(final String name, final Collection<Privilege> privileges){
        Optional<Role> optionalRole = roleRepository.findByName(name);
        Role role = new Role();
        if(optionalRole.isEmpty()){
            role.setName(name);
        }
        role.setPrivileges(privileges);
        roleRepository.save(role);
        return role;
    }


    @Transactional
    public User createUserIfNotFound(final String username, final String firstName, final String lastName, final String password, final Collection<Role> roles) {
      Optional <User> optionalUser = userRepository.findUserByUsername(username);
        User user = new User();
        if (optionalUser.isEmpty()) {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPassword(passwordEncoder.encode(password));
            user.setUsername(username);
            user.setEnabled(true);
        }
        user.setRoles(roles);
        userRepository.save(user);
        return user;
    }

}
