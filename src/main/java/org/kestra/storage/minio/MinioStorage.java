package org.kestra.storage.minio;

import io.micronaut.core.annotation.Introspected;
import io.minio.MinioClient;
import org.kestra.core.storages.StorageInterface;
import org.kestra.core.storages.StorageObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;

@Singleton
@MinioStorageEnabled
@Introspected
public class MinioStorage implements StorageInterface {
    @Inject
    MinioClientFactory factory;

    @Inject
    MinioConfig config;

    private MinioClient client() {
        return factory.of(config);
    }

    @Override
    public InputStream get(URI uri) throws FileNotFoundException  {
        try {
            return client().getObject(this.config.getBucket(), uri.getPath().substring(1));
        } catch (Throwable e) {
            throw new FileNotFoundException(uri.toString() + " (" + e.getMessage() + ")");
        }
    }

    @Override
    public StorageObject put(URI uri, InputStream data) throws IOException {
        try {
            client().putObject(
                this.config.getBucket(),
                uri.toString().substring(1),
                data,
                null,
                new HashMap<>(),
                null,
                null
            );

            data.close();
        } catch (Throwable e) {
            throw new IOException(e);
        }

        URI result = URI.create("kestra://" + uri.getPath());

        return new StorageObject(this, result);
    }
}
