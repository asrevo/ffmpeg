package org.revo.Service;

import org.revo.Domain.Bucket;

public interface SignedUrlService {
    String generate(Bucket bucket, String key);

    String getUrl(String key, String bucket);
}
