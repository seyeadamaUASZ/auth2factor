package com.sid.gl.services.impl;

import com.sid.gl.services.interfaces.ITopManager;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
@Slf4j
public class TopManagerService implements ITopManager {


    @Override
    public String generateSecret() {
        SecretGenerator secretGenerator =
                new DefaultSecretGenerator();
        return secretGenerator.generate();
    }

    @Override
    public String getUriForImage(String secret) {
        QrData qrData = new QrData.Builder()
                .label("two-factor-auth")
                .secret(secret)
                .issuer("twofactor")
                .algorithm(HashingAlgorithm.SHA256)
                .digits(6)
                .period(30)
                .build();
        QrGenerator qrGenerator =
        new ZxingPngQrGenerator();
        byte[] imageData=new byte[0];

        try {
            imageData = qrGenerator.generate(qrData);
        } catch (QrGenerationException e) {
            log.error("unable to generate QrCode");
        }

        String mimeType = qrGenerator.getImageMimeType();

        return getDataUriForImage(imageData, mimeType);
    }

    @Override
    public boolean verifyCode(String code, String secret) {
        TimeProvider timeProvider =
                new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator,timeProvider);
        return codeVerifier.isValidCode(secret,code);
    }
}
