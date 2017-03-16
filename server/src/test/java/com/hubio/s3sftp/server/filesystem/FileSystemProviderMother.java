package com.hubio.s3sftp.server.filesystem;

import org.apache.sshd.common.session.Session;

/**
 * Test Object Mother for {@link java.nio.file.spi.FileSystemProvider}s.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class FileSystemProviderMother {

    /**
     * Create a filesystem provider with {@link java.nio.channels.FileChannel} support for the server session.
     *
     * @param session The server session
     *
     * @return The filesystem provider
     */
    public static S3SftpFileSystemProvider fileChannelProvider(final Session session) {
        return new FileChannelS3SftpFileSystemProvider(FileSystemProviderFactory.delegatableProvider(session));
    }
}
