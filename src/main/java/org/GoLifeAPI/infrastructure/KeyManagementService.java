package org.GoLifeAPI.infrastructure;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.kms.v1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class KeyManagementService {
    private static final Logger log = LoggerFactory.getLogger(KeyManagementService.class);
    private static final String CRYPTO_KEY = System.getenv("KMS_CRYPTO_KEY");

    private volatile String primaryVersionName;
    private KeyManagementServiceClient kms;

    @PostConstruct
    void initializeKMS() {
        final long t0 = System.currentTimeMillis();
        boolean ok = false;

        try {
            if (CRYPTO_KEY == null || CRYPTO_KEY.isBlank()) {
                throw new IllegalStateException("KMS_CRYPTO_KEY no está definido");
            }
            GoogleCredentials cred = GoogleCredentials.getApplicationDefault();
            log.info("[KMS] init starting | name={}, credsClass={}", CRYPTO_KEY, cred.getClass().getSimpleName());

            kms = KeyManagementServiceClient.create();

            resolveVersionFromEnv();
            ok = true;
            log.info("[KMS] initialized OK | versionName={}", primaryVersionName);

        } catch (Exception e) {
            log.error("[KMS] init FAILED | name={}", CRYPTO_KEY, e);
            throw new IllegalStateException("No se pudo inicializar KMS", e);
        } finally {
            log.info("[KMS] init finished | status={}, elapsedMs={}", ok ? "OK" : "FAILED", System.currentTimeMillis() - t0);
        }
    }

    private void resolveVersionFromEnv() {
        if (CRYPTO_KEY.matches(".*/cryptoKeyVersions/\\d+$")) {
            primaryVersionName = CRYPTO_KEY;
            log.info("[KMS] using explicit version | versionName={}", primaryVersionName);
        } else {
            CryptoKey key = kms.getCryptoKey(CRYPTO_KEY);
            if (!key.hasPrimary()) {
                throw new IllegalStateException("La clave KMS no tiene versión primaria asignada");
            }
            primaryVersionName = key.getPrimary().getName();
            log.info("[KMS] primary resolved | versionName={}", primaryVersionName);
        }
    }

    public boolean ping() {
        try {
            String mac = sign("kms-ping");
            return mac != null && !mac.isBlank();
        } catch (Exception ignored) {
            return false;
        }
    }


    public String sign(String data) {
        ByteString payload = ByteString.copyFrom(data.getBytes(StandardCharsets.UTF_8));
        try {
            MacSignResponse resp = kms.macSign(MacSignRequest.newBuilder()
                    .setName(primaryVersionName)
                    .setData(payload)
                    .build());
            return Base64.getEncoder().encodeToString(resp.getMac().toByteArray());
        } catch (Exception e) {
            resolveVersionFromEnv();
            MacSignResponse resp = kms.macSign(MacSignRequest.newBuilder()
                    .setName(primaryVersionName)
                    .setData(payload)
                    .build());
            return Base64.getEncoder().encodeToString(resp.getMac().toByteArray());
        }
    }

    public boolean verify(String data, String base64Mac) {
        ByteString payload = ByteString.copyFrom(data.getBytes(StandardCharsets.UTF_8));
        ByteString mac = ByteString.copyFrom(Base64.getDecoder().decode(base64Mac));
        try {
            MacVerifyResponse resp = kms.macVerify(MacVerifyRequest.newBuilder()
                    .setName(primaryVersionName)
                    .setData(payload)
                    .setMac(mac)
                    .build());
            return resp.getSuccess();
        } catch (Exception e) {
            resolveVersionFromEnv();
            MacVerifyResponse resp = kms.macVerify(MacVerifyRequest.newBuilder()
                    .setName(primaryVersionName)
                    .setData(payload)
                    .setMac(mac)
                    .build());
            return resp.getSuccess();
        }
    }
}