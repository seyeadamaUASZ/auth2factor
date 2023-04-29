package com.sid.gl.services.impl;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.sid.gl.dto.FactorRequest;
import com.sid.gl.dto.LoginRequest;
import com.sid.gl.dto.UserRequest;
import com.sid.gl.dto.UserResponse;
import com.sid.gl.exceptions.BadRequestException;
import com.sid.gl.exceptions.InternalServerException;
import com.sid.gl.exceptions.UserAlreadyExistException;
import com.sid.gl.exceptions.UserNotFoundException;
import com.sid.gl.mappers.FactorMapper;
import com.sid.gl.model.*;
import com.sid.gl.repositories.NewTokenRepository;
import com.sid.gl.repositories.RoleRepository;
import com.sid.gl.repositories.UserLocationRepository;
import com.sid.gl.repositories.UserRepository;
import com.sid.gl.services.interfaces.ITopManager;
import com.sid.gl.services.interfaces.IUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

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

     @Autowired
     private Environment env;

     @Autowired
     @Qualifier("GeoIPCountry")
     private DatabaseReader databaseReader;

     @Autowired
     private UserLocationRepository userLocationRepository;

     @Autowired
     private NewTokenRepository newLocationTokenRepository;

     @Autowired
     private DeviceService deviceService;

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

          return optionalUser;

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

          Set<Role> roles = new HashSet<>();
          roles.add(role);
          user.setRoles(roles);

          if(user.isMfa()){
               user.setSecret(totpManager.generateSecret());
               user.setDateValSecret(new Date());
          }
          User saved = userRepository.save(user);
          sendMail(saved);
          return saved;
     }

     private void sendMail(User user) throws IOException {
          String message;
          String subject;
          if(user.isMfa()){
               message="Votre code secret est : "+ user.getSecret();
               subject = "Votre secret code ";
          }else{
               message="Votre compte a été créé ";
               subject = "Compte créé ";
          }
          mailService.sendEmail(subject,message,user.getUsername());
     }

     @Override
     public String login(LoginRequest loginRequest, HttpServletRequest request) throws IOException, GeoIp2Exception {
          log.info("login request {} ",loginRequest.getUsername()+ " "+ loginRequest.getPassword());
          User user = userRepository.findUserByUsername(loginRequest.getUsername())
                  .orElseThrow(()->new UserNotFoundException("user not found !!!"));
          Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
          //on verifie aussi la device ou ajouter un nouveau device
          if(authentication.getPrincipal() instanceof User && isGeoIpLibEnabled()){
               deviceService.verifyDevice((User) authentication.getPrincipal(),request);
          }//set secret date val duration if mfa
          if(user.isMfa()){
               user.setDateValSecret(new Date());
               userRepository.save(user);
          }
          return user.isMfa() ? "" : jwtTokenManager.generateToken(authentication);
     }

     @Override
     public String verifyCode(FactorRequest request) throws BadRequestException {
          User user = userRepository.findUserByUsername(request.getUsername())
                  .orElseThrow(()->new UsernameNotFoundException("user not found"));

          log.info("code extract {}", user.getSecret());
          Date time = user.getDateValSecret();
          time.setTime(time.getTime() + 2L * 60000);

          if(!totpManager.codeVerify(request.getCode(), user.getSecret(),time))
               throw new BadRequestException("code not correct or expirated time !!!");

          return Optional.of(user)
                  .map(UserInfo::new)
                  .map(userDetails -> new UsernamePasswordAuthenticationToken(
                          userDetails, null, userDetails.getAuthorities()))
                  .map(jwtTokenManager::generateToken)
                  .orElseThrow(() ->
                          new InternalServerException("unable to generate access token"));
     }

     @Override
     public NewLocationToken isNewLocation(String username, String ip) {
          if(isGeoIpLibEnabled()) {
               return null;
          }
          try {
               final InetAddress ipAddress = InetAddress.getByName(ip);
               final String country = databaseReader.country(ipAddress)
                       .getCountry()
                       .getName();
               log.info("country location {} ....... ",country);
               final User user = userRepository.findUserByUsername(username)
                       .orElseThrow(()->new UserNotFoundException("user not found !!"));
               final UserLocation loc = userLocationRepository.findByUserAndCountry(user,country);
               if ((loc == null) || !loc.isEnabled()) {
                    return createNewLocationToken(country, user);
               }
          } catch (final Exception e) {
               return null;
          }
         return null;
     }

     @Override
     public void addUserLocation(User user, String ip) {
          if(isGeoIpLibEnabled()){
               return ;
          }
          try {
               final InetAddress ipAddress=
                       InetAddress.getByName(ip);
               final String country = databaseReader.country(ipAddress)
                       .getCountry()
                       .getName();
               UserLocation location = new UserLocation(country,user);
               location.setEnabled(true);
               userLocationRepository.save(location);
          } catch (final Exception e) {
               throw new RuntimeException(e);
          }

     }

     private boolean isGeoIpLibEnabled() {
          return !Boolean.parseBoolean(env.getProperty("geo.ip.lib.enabled"));
     }

     private NewLocationToken createNewLocationToken(String country,User user){
          UserLocation location = new UserLocation(country,user);
          location = userLocationRepository.save(location);
          final NewLocationToken token = new NewLocationToken(UUID.randomUUID()
                  .toString(), location);
          return newLocationTokenRepository.save(token);
     }

     @Override
     public List<UserLocation> listLocationsUser(Long id) {
          return userLocationRepository.findByUser_Id(id);
     }
}
