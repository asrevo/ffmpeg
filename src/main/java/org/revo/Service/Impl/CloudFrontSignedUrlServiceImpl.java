package org.revo.Service.Impl;

import com.amazonaws.auth.PEM;
import org.revo.Config.Env;
import org.revo.Domain.Bucket;
import org.revo.Service.SignedUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;

import static com.amazonaws.services.cloudfront.CloudFrontUrlSigner.getSignedURLWithCannedPolicy;
import static com.amazonaws.services.cloudfront.util.SignerUtils.Protocol.https;
import static com.amazonaws.services.cloudfront.util.SignerUtils.generateResourcePath;

@Service
@Profile("cloudFront")
public class CloudFrontSignedUrlServiceImpl implements SignedUrlService {

    @Value("${cloudfront.keyId}")
    private String cloudfront_keyId;
    @Value("${spring.cloud.config.uri}")
    private String configServer;
    private String privateKey = "";
    @Autowired
    private Env env;

    @PostConstruct
    public void someInit() throws IOException {
        URL url = new URL(configServer + "/revo-pk.pem");
        URLConnection uc = url.openConnection();
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode(configServer.split("@")[0].split("//")[1].getBytes()));
        uc.setRequestProperty("Authorization", basicAuth);
        Scanner s = new Scanner(uc.getInputStream());
        while (s.hasNext()) {
            privateKey += s.nextLine() + "\n";
        }
    }


    @Override
    public String generate(Bucket bucket, String key) {
        Date expirationDate = new Date(System.currentTimeMillis() + 60 * 60 * 1000);
        try {
            return getSignedURLWithCannedPolicy(generateResourcePath(https, bucket.getDomainName(), key), cloudfront_keyId, PEM.readPrivateKey(new ByteArrayInputStream(privateKey.getBytes())), expirationDate);
        } catch (InvalidKeySpecException | IOException e) {
            e.printStackTrace();
        }
        return key;
    }

    @Override
    public String getUrl(String key, String bucket) {
        return generateResourcePath(https, env.getBuckets().get(bucket).getDomainName(), key);
    }
}
