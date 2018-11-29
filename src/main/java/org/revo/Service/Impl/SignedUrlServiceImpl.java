package org.revo.Service.Impl;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.revo.Config.Env;
import org.revo.Domain.Bucket;
import org.revo.Service.SignedUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SignedUrlServiceImpl implements SignedUrlService {
    @Autowired
    private AmazonS3Client amazonS3Client;
    @Autowired
    private Env env;

    @Override
    public String generate(Bucket bucket, String key) {
        java.util.Date expiration = new java.util.Date();
        long msec = expiration.getTime();
        msec += 1000 * 60 * 60;
        expiration.setTime(msec);
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket.toString(), key);
        generatePresignedUrlRequest.setMethod(HttpMethod.GET);
        generatePresignedUrlRequest.setExpiration(expiration);
        return amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest).toString();

    }

    @Override
    public String getUrl(String key, String bucket) {
        return amazonS3Client.getResourceUrl(env.getBuckets().get(bucket).getName(), key);
    }
}
