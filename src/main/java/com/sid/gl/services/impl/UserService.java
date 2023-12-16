package com.sid.gl.services.impl;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.sid.gl.dto.*;
import com.sid.gl.exceptions.BadRequestException;
import com.sid.gl.exceptions.InternalServerException;
import com.sid.gl.exceptions.UserAlreadyExistException;
import com.sid.gl.exceptions.Auth2factorNotFoundException;
import com.sid.gl.mappers.FactorMapper;
import com.sid.gl.model.*;
import com.sid.gl.repositories.NewTokenRepository;
import com.sid.gl.repositories.RoleRepository;
import com.sid.gl.repositories.UserLocationRepository;
import com.sid.gl.repositories.UserRepository;
import com.sid.gl.services.interfaces.ITopManager;
import com.sid.gl.services.interfaces.IUser;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.util.UriComponentsBuilder;


import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

//TODO add test on this Service
@Service
@Slf4j
public class UserService implements IUser {
     private final  BCryptPasswordEncoder passwordEncoder;
     private final UserRepository userRepository;
     private final  AuthenticationManager authenticationManager;
     private final JwtTokenManager jwtTokenManager;
     private final ITopManager totpManager;
     private final MailService mailService;
     private final RoleRepository roleRepository;
     private final DeviceService deviceService;
     private final DatabaseReader databaseReader;
     private final Environment env;
     private final UserLocationRepository userLocationRepository;
     private final NewTokenRepository newTokenRepository;

     @Autowired
     public UserService(BCryptPasswordEncoder passwordEncoder, UserRepository userRepository,
                        AuthenticationManager authenticationManager, JwtTokenManager jwtTokenManager,
                        ITopManager totpManager, MailService mailService,
                        RoleRepository roleRepository, DeviceService deviceService,
                        @Qualifier("GeoIPCountry") DatabaseReader databaseReader,
                        Environment env, UserLocationRepository userLocationRepository,
                        NewTokenRepository newTokenRepository) {
          this.passwordEncoder = passwordEncoder;
          this.userRepository = userRepository;
          this.authenticationManager = authenticationManager;
          this.jwtTokenManager = jwtTokenManager;
          this.totpManager = totpManager;
          this.mailService = mailService;
          this.roleRepository = roleRepository;
          this.deviceService = deviceService;
          this.databaseReader = databaseReader;
          this.env = env;
          this.userLocationRepository = userLocationRepository;
          this.newTokenRepository = newTokenRepository;
     }

     @Override
     public List<UserResponse> allUsers() {
          return FactorMapper.builListUserResponse(userRepository.findAll());
     }

     @Override
     public Optional<UserResponse> findByUserName(String username) throws Auth2factorNotFoundException {
          Optional<User> optionalUser = userRepository.findUserByUsername(username);
          if(optionalUser.isEmpty()){
               log.error("user not found");
               throw new Auth2factorNotFoundException("User not found");
          }
          return Optional.of(FactorMapper.convertToUserResponse(optionalUser.get()));
     }

     @Override
     public User findUserByUserName(String username) throws Auth2factorNotFoundException {
         return  userRepository.findUserByUsername(username)
                  .orElseThrow(()->new Auth2factorNotFoundException("User not found"));

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
                  .orElseThrow(()->new Auth2factorNotFoundException("user not found !!!"));
          Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
          //on verifie aussi la device ou ajouter un nouveau device
          if(isGeoIpLibEnabled()){
               deviceService.verifyDevice(user,request);
          }
          //TODO we need to evaluate impact to add isNew location user

          isNewLocation(user.getUsername(),getClientIP(request));

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

     //pour une nouvelle localisation en ajoutant le token selon la localisation
     @Override
     public NewLocationToken isNewLocation(String username, String ip) {
          String country;
          if(!isGeoIpLibEnabled()) {
               return null;
          }
          try {
               final InetAddress ipAddress = InetAddress.getByName(ip);
               try{
                     country = databaseReader.country(ipAddress)
                            .getCountry()
                            .getName();
               }catch (AddressNotFoundException e){
                    log.error("IP Not found on database");
                    country="UNKNOWN";
               }

               final User user = userRepository.findUserByUsername(username).orElseThrow(()->new Auth2factorNotFoundException("User by username not found!!!"));
               log.info("user "+user.getUsername());
               final UserLocation loc = userLocationRepository.findByUserAndCountry(user,country);
               if ((loc == null) || !loc.isEnabled()) {
                    return createNewLocationToken(country, user);
               }
          } catch (final Exception e) {
               log.error("Error to create user location");
               throw new BadRequestException("Unable to create user location token");
          }

          return null;
     }


     private NewLocationToken createNewLocationToken(String country, User user) {
          UserLocation loc = new UserLocation(country, user);
          loc = userLocationRepository.save(loc);

          final NewLocationToken token = new NewLocationToken(UUID.randomUUID()
                  .toString(), loc);
          return newTokenRepository.save(token);
     }

     //ajouter l'utilisateur sur une localisation donnée
     @Override
     public void addUserLocation(User user,HttpServletRequest request) {
          if(!isGeoIpLibEnabled()) {
               return;
          }
          try {
               String ip = getClientIP(request);
               final InetAddress ipAddress = InetAddress.getByName(ip);
               final String country = databaseReader.country(ipAddress)
                       .getCountry()
                       .getName();
               UserLocation loc = new UserLocation(country, user);
               loc.setEnabled(true);
               userLocationRepository.save(loc);
          } catch (final Exception e) {
               throw new RuntimeException(e);
          }

     }

     @Override
     public List<UserLocation> listLocationsUser(Long id) {
          return userLocationRepository.findByUser_Id(id);
     }


     @Override
     public String sendEmailForPassword(SendRequest request) throws IOException {
          Optional<User> optionalUser = userRepository.findUserByUsername(request.getUsername());
          if(optionalUser.isEmpty())
               throw new Auth2factorNotFoundException("User not found !!");
          User user = optionalUser.get();

          String appUrl = getUriComponentBuilder("auth/resetPassword/username?="+user.getUsername()).toString();

          String subject="Réinitialisation mot de passe";
          String message="Pour réinitialiser votre mot de passe veuillez cliquer "+
                  "\n sur le lien : \n"
                          +appUrl;
         mailService.sendEmail(subject,message,user.getUsername());
         return "Success";
     }

     @Override
     public String changeUserPassword(String username, NewPasswordRequest request) {
          User user = userRepository.findUserByUsername(username)
                  .orElseThrow(()->new Auth2factorNotFoundException("User not found !!!"));
          String passH = passwordEncoder.encode(request.getPassword());
          user.setPassword(passH);
          //update user on new password
          userRepository.save(user);
          return "Password successfully updated ";
     }

     @Override
     public List<DeviceMetadataResponse> allDevicesByUser(Long idUser) {
          return deviceService.listDevicesByUser(idUser);
     }


     private UriComponentsBuilder getUriComponentBuilder(String path){
          return UriComponentsBuilder
                  .fromHttpUrl(String.format("http://localhost:9075/%s",path));

     }

     private boolean isGeoIpLibEnabled() {
          return Boolean.parseBoolean(env.getProperty("geo.ip.lib.enabled"));
     }

     private String getClientIP(HttpServletRequest request) {
          final String xfHeader = request.getHeader("X-Forwarded-For");
          if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
               return request.getRemoteAddr();
          }
          return xfHeader.split(",")[0];
     }
}
