package com.sid.gl.services.impl;

import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class MailService {

    @Value("${mail.api.key}")
    private String apiKey;

    private static final String MAILTO="a.seye3777@zig.univ.sn";

    public String sendEmail(final String mailDestination) throws IOException {
        // the sender email should be the same as we used to Create a Single Sender Verification
        Email from = new Email(MAILTO);
        String subject = "OTP envoyé";
        Email to = new Email(mailDestination);
        Content content = new Content("text/plain", "Le code otp est ");
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            log.info(response.getBody());
            return response.getBody();
        } catch (IOException ex) {
            throw ex;
        }
    }

}
