package com.sid.gl.services.impl;

import com.sid.gl.dto.FactorRequest;
import com.sid.gl.dto.LoginRequest;
import com.sid.gl.dto.UserRequest;
import com.sid.gl.dto.UserResponse;
import com.sid.gl.exceptions.BadRequestException;
import com.sid.gl.exceptions.InternalServerException;
import com.sid.gl.exceptions.UserAlreadyExistException;
import com.sid.gl.exceptions.UserNotFoundException;
import com.sid.gl.mappers.FactorMapper;
import com.sid.gl.model.Role;
import com.sid.gl.model.User;
import com.sid.gl.model.UserInfo;
import com.sid.gl.repositories.RoleRepository;
import com.sid.gl.repositories.UserRepository;
import com.sid.gl.services.interfaces.ITopManager;
import com.sid.gl.services.interfaces.IUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements IUser {
     private final BCryptPasswordEncoder passwordEncoder;
     private final UserRepository userRepository;
     private final AuthenticationManager authenticationManager;
     private final JwtTokenManager jwtTokenManager;
     private final ITopManager totpManager;
     private final MailService mailService;

     private final RoleRepository roleRepository;


     @Override
     public List<UserResponse> allUsers() {
          return FactorMapper.builListUserResponse(userRepository.findAll());
     }

     @Override
     public Optional<UserResponse> findByUserName(String username) throws UserNotFoundException {
          Optional<User> optionalUser = userRepository.findUserByUsername(username);
          if(optionalUser.isEmpty())
               throw new UserNotFoundException("User not found");

          return Optional.of(FactorMapper.convertToUserResponse(optionalUser.get()));
     }

     @Override
     public Optional<User> findUserByUserName(String username) throws UserNotFoundException {
          Optional<User> optionalUser = userRepository.findUserByUsername(username);
          if(optionalUser.isEmpty())
               throw new UserNotFoundException("User not found");

          return Optional.of(optionalUser.get());
     }



     @Override
     public UserResponse findUserById(Long id) {
          User user = userRepository.findById(id).orElseThrow(()->new UsernameNotFoundException("user not found"));
          return FactorMapper.convertToUserResponse(user);
     }

     @Override
     public User registerUser(final UserRequest userRequest) throws UserAlreadyExistException, IOException {
          //verifions que le username n'existe pas
          Optional<User> optionalUser = userRepository.findUserByUsername(userRequest.getUsername());
          if(optionalUser.isPresent())
               throw new UserAlreadyExistException("User already exist");

          String passH = passwordEncoder.encode(userRequest.getPassword());
          User user = FactorMapper.convertToUser(userRequest);
          user.setPassword(passH);

          Role role = roleRepository.findByName("USER")
                  .orElseThrow(()->new IllegalArgumentException("Role not found"));

          user.setRoles(new HashSet<>(){{
               add(role);
          }});

          if(user.isMfa()){
               user.setSecret(totpManager.generateSecret());
          }

          User saved = userRepository.save(user);

          String message="";
          String subject="";
          if(saved.isMfa()){
                message="Votre code secret est : "+ saved.getSecret();
                subject = "Votre secret code ";
          }else{
               message="Votre compte a été créé ";
               subject = "Compte créé ";
          }
          mailService.sendEmail(subject,message,saved.getUsername());
          return saved;
     }

     @Override
     public String login(LoginRequest loginRequest) {
          log.info("login request {} ",loginRequest.getUsername()+ " "+ loginRequest.getPassword());
          User user = userRepository.findUserByUsername(loginRequest.getUsername()).get();
          Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

          return user.isMfa() ? "" : jwtTokenManager.generateToken(authentication);
     }

     @Override
     public String verifyCode(FactorRequest request) throws BadRequestException {

          User user = userRepository.findUserByUsername(request.getUsername())
                  .orElseThrow(()->new UsernameNotFoundException("user not found"));

          log.info("code extract {}", user.getSecret());
          if(!totpManager.codeVerify(request.getCode(), user.getSecret()))
               throw new BadRequestException("code not correct");

          return Optional.of(user)
                  .map(UserInfo::new)
                  .map(userDetails -> new UsernamePasswordAuthenticationToken(
                          userDetails, null, userDetails.getAuthorities()))
                  .map(jwtTokenManager::generateToken)
                  .orElseThrow(() ->
                          new InternalServerException("unable to generate access token"));
     }


}
