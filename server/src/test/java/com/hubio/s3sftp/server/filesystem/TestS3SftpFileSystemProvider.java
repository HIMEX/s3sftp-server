package com.hubio.s3sftp.server.filesystem;

import com.amazonaws.services.s3.AmazonS3;
import com.hubio.s3sftp.server.filesystem.S3SftpFileSystemProvider;
import com.hubio.s3sftp.server.filesystem.S3SftpFileSystemProviderDecorator;

import java.net.URI;
import java.util.Properties;

/**
 * Test S3 SFTP File System Provider that uses a fake Amazon S3 client.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
class TestS3SftpFileSystemProvider extends S3SftpFileSystemProviderDecorator {

    private final AmazonS3 amazonS3;

    TestS3SftpFileSystemProvider(final S3SftpFileSystemProvider provider, final AmazonS3 amazonS3) {
        super(provider);
        this.amazonS3 = amazonS3;
    }

    @Override
    public AmazonS3 getAmazonS3(final URI uri, final Properties props) {
        return amazonS3;
    }
}
