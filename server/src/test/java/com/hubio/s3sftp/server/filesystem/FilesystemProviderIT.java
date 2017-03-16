package com.hubio.s3sftp.server.filesystem;

import com.amazonaws.services.s3.AmazonS3;
import com.hubio.s3sftp.server.S3PathEnhancer;
import com.hubio.s3sftp.server.S3SftpServer;
import com.hubio.s3sftp.server.TestAmazonS3Configuration;
import com.upplication.s3fs.S3FileSystem;
import com.upplication.s3fs.S3Path;
import lombok.val;
import me.andrz.builder.map.MapBuilder;
import org.apache.sshd.common.session.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link S3SftpFileSystemProvider} collection of classes.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class FilesystemProviderIT {

    private final static String S3_URI = "s3://uri";

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private Session session;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        // ensure all cached filesystems are closed
        new DelegatableS3FileSystemProvider(session).getAllFileSystems()
                                                    .forEach(s3FileSystem -> s3FileSystem.provider()
                                                                                         .close(s3FileSystem));
    }

    @Test
    public void shouldCreateFilteredS3FileSystem() {
        //given
        val factory = new DefaultS3SftpFileSystemProviderFactory();
        val pathEnhancer = S3PathEnhancer.fixedPrefix("prefix");
        val env = new MapBuilder<String, String>().put(S3SftpServer.BUCKET, "bucket")
                                                  .put(S3SftpServer.USERNAME, "bob")
                                                  .put(S3SftpServer.JAIL, "/users/bob")
                                                  .build();
        val fileSystemProvider = factory.createWith(pathEnhancer, session);
        //when
        val fileSystem = fileSystemProvider.getFileSystem(URI.create(S3_URI), env);
        //then
        assertThat(fileSystem).isInstanceOf(FilteredS3FileSystem.class);
    }

    @Test
    public void shouldCallPosixPermissionsReadAttributesFromS3FileProviderInterface() throws IOException {
        //given
        val configuration = TestAmazonS3Configuration.of(amazonS3, "bucket", "users", "username");
        val fileSystemProvider = configuration.getFileSystemProvider();
        val fileSystem = fileSystemProvider.getFileSystem(URI.create(S3_URI), configuration.getEnv());
        assertThat(fileSystem).isInstanceOf(S3FileSystem.class);
        val s3path = new S3Path((S3FileSystem) fileSystem, configuration.getHomeDir());
        val attr = "";
        //when
        val attributes = fileSystemProvider.readAttributes(s3path, attr);
        //then
        assertThat(attributes).containsKey("permissions");
    }
}
