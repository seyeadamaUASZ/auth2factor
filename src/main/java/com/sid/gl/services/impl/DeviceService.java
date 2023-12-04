package com.sid.gl.services.impl;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.sid.gl.dto.DeviceMetadataResponse;
import com.sid.gl.mappers.FactorMapper;
import com.sid.gl.model.DeviceMetadata;
import com.sid.gl.model.User;
import com.sid.gl.repositories.DeviceRepository;
import com.sid.gl.services.interfaces.IDevice;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import ua_parser.Client;
import ua_parser.Parser;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;


@Component
@Slf4j
public class DeviceService implements IDevice {
  private static final String UNKNOWN = "UNKNOWN";
  private final DeviceRepository deviceRepository;
  private final DatabaseReader databaseReader;
  private final Parser parser;
  private final MailService  mailService;

  private final MessageSource messages;

  public DeviceService(DeviceRepository deviceRepository, @Qualifier("GeoIPCity") DatabaseReader databaseReader, Parser parser, MailService mailService, MessageSource messages) {
    this.deviceRepository = deviceRepository;
    this.databaseReader = databaseReader;
    this.parser = parser;
    this.mailService = mailService;
      this.messages = messages;
  }

  @Override
    public List<DeviceMetadataResponse> listDevicesByUser(Long idUser) {
        return FactorMapper.buildListDeviceResponse(deviceRepository.findByUserId(idUser)) ;
    }

    @Override
    public void verifyDevice(User user, HttpServletRequest request) throws IOException, GeoIp2Exception {
        String ip = extractIp(request);
        String location = getLocation(ip);
        String deviceDetails = getDeviceDetails(request.getHeader("user-agent"));
        log.info("deviceDetails "+deviceDetails);

        DeviceMetadata existingDevice = findExistDevice(user.getId(),deviceDetails,location);

        if(Objects.isNull(existingDevice)){
              unknownDeviceNotification(deviceDetails,location,ip, user.getUsername(),request.getLocale());

              DeviceMetadata deviceMetadata = new DeviceMetadata();
              deviceMetadata.setUserId(user.getId());
              deviceMetadata.setLocation(location);
              deviceMetadata.setDeviceDetails(deviceDetails);
              deviceMetadata.setLastLoggedIn(new Date());
              deviceRepository.save(deviceMetadata);
        }else{
          existingDevice.setLastLoggedIn(new Date());
          deviceRepository.save(existingDevice);
        }
    }

  private void unknownDeviceNotification(String deviceDetails, String location, String ip, String email, Locale locale) throws IOException {
    final String subject = "New Login Notification";

    String text = getMessage("message.login.notification.deviceDetails", locale) +
            " " + deviceDetails +
            "\n" +
            getMessage("message.login.notification.location", locale) +
            " " + location +
            "\n" +
            getMessage("message.login.notification.ip", locale) +
            " " + ip;

       mailService.sendEmail(subject,text,email);
  }


  private String getMessage(String code, Locale locale) {
    try {
      return messages.getMessage(code, null, locale);

    } catch (NoSuchMessageException ex) {
      return messages.getMessage(code, null, Locale.ENGLISH);
    }
  }

  private DeviceMetadata findExistDevice(Long id, String deviceDetails, String location) {
    List<DeviceMetadata> knowDevices = deviceRepository.findByUserId(id);
      Predicate<DeviceMetadata> deviceMetadataPredicate = deviceMetadata -> deviceMetadata.getDeviceDetails().equals(deviceDetails) &&
              deviceMetadata.getLocation().equals(location);

     return  CollectionUtils.emptyIfNull(knowDevices).stream()
              .filter(deviceMetadataPredicate)
              .findFirst()
              .orElse(null);
  }

  private String extractIp(HttpServletRequest request){
       String clientXForwardedForIp = request.getHeader("x-forwarded-for");
       return nonNull(clientXForwardedForIp) ? parseXForwardedHeader(clientXForwardedForIp) : request.getRemoteAddr();

    }

  private String parseXForwardedHeader(String header) {
      Pattern p = Pattern.compile(" *, *");
      String[] result = p.split(header);
    return result[0];
  }

  private String getLocation(String ip) throws IOException,GeoIp2Exception{
      String location = "";
      InetAddress ipAddress= InetAddress.getByName(ip);
      try{
          CityResponse cityResponse = databaseReader.city(ipAddress);
          if(nonNull(cityResponse) && nonNull(cityResponse.getCity()) && StringUtils.isNotEmpty(cityResponse.getCity().getName())
          ){
              location = cityResponse.getCity().getName();
              log.info("location : {} ",location);
          }
      }catch (AddressNotFoundException e){
          log.error("Ip Address not found ");
          location = "UNKNOWN";
      }
      return location;
  }

  //les details du device
  private String getDeviceDetails(String userAgent){
    String deviceDetails=UNKNOWN;
    Client client = parser.parse(userAgent);
    if(nonNull(client)){
      deviceDetails = client.userAgent.family + " " + client.userAgent.major + "." + client.userAgent.minor +
              " - " + client.os.family + " " + client.os.major + "." + client.os.minor;
    }
    return deviceDetails;
  }
}
