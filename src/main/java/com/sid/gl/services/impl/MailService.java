package com.sid.gl.services.impl;

import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sid.gl.config.ApiKeyCredential;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final ApiKeyCredential credential;

    public String sendEmail(String subject, String message,final String mailDestination) throws IOException {
        // the sender email should be the same as we used to Create a Single Sender Verification
        log.info("credential apikey "+credential.getApikey());
        if(credential.getEmail() ==null){
            credential.setEmail("a.seye3777@zig.univ.sn");

        }
        if(credential.getApikey() ==null){
            credential.setApikey("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\n");
        }
        Email from = new Email(credential.getEmail());
        Email to = new Email(mailDestination);
        Content content = new Content("text/plain", message);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(credential.getApikey());
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            log.info(response.getBody());
            return response.getBody();
        } catch (IOException ex) {
            log.error("error sending mail with sendgrid");
           throw ex;
        }
    }

}
