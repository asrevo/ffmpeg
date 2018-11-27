package org.revo.Service.Impl;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import org.revo.Config.Env;
import org.revo.Service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by ashraf on 15/04/17.
 */
@Service
public class S3ServiceImpl implements S3Service {
    @Autowired
    private AmazonS3Client amazonS3Client;
    @Autowired
    private Env env;

    @Override
    public Path pull(String key) throws IOException {
        S3Object object = this.amazonS3Client.getObject(env.getBuckets().get("video").toString(), key);
        Path temp = Files.createTempDirectory("temp");
        Path f = Paths.get(temp.toString(), key);
        Files.copy(object.getObjectContent(), f);
        return f;
    }

    @Override
    public void pushMedia(String key, File file) {
        this.amazonS3Client.putObject(env.getBuckets().get("video").toString(), key, file);
    }

    @Override
    public void pushImage(String key, File file) {
        this.amazonS3Client.putObject(env.getBuckets().get("thumb").toString(), key, file);
    }

    @Override
    public void deleteMedia(String key) {
        this.amazonS3Client.deleteObject(env.getBuckets().get("video").toString(), key);
    }
}
