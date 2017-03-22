package com.hubio.s3sftp.server;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.hubio.s3sftp.server.filesystem.FileSystemProviderFactory;
import com.hubio.s3sftp.server.filesystem.S3SftpFileSystemProvider;
import com.upplication.s3fs.S3FileSystemProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import me.andrz.builder.map.MapBuilder;
import org.apache.sshd.common.session.Session;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Creates a test configuration for the supplied arguments.
 *
 * <p>The 'configuration' consists of:</p>
 *
 * <dl>
 *
 * <dt>homeDir</dt>
 * <dd>The full path to the user's home directory within the {@code bucket} and {@code user} subdirectory.</dd>
 *
 * <dt>fileSystemProvider</dt>
 * <dd>A configured filesystem provider.</dd>
 *
 * <dt>env</dt>
 * <dd>A populated environment map, containing USERNAME, BUCKET, HOMEDIR and JAIL values.</dd>
 *
 * </dl>
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Getter
@RequiredArgsConstructor
public class TestAmazonS3Configuration {

    private final String homeDir;

    private final S3SftpFileSystemProvider fileSystemProvider;

    private final Map<String, String> env;

    public static TestAmazonS3Configuration of(
            final AmazonS3 amazonS3, final String bucket, final String users, final String username
                                              ) {
        val key = String.format("%s/%s", users, username);
        val path = String.format("/%s/%s", bucket, key);
        val factory = FileSystemProviderFactory.s3SftpProviderFactory();
        val pathEnhancer = new FixedPrefixS3PathEnhancer(String.format("/%s/%s", bucket, key));
        val session = mock(Session.class);
        val fileSystemProvider = factory.createWith(pathEnhancer, session);
        fileSystemProvider.setAmazonS3(amazonS3);
        given(amazonS3.getObjectMetadata(bucket, key)).willReturn(new ObjectMetadata());
        given(amazonS3.getObjectAcl(bucket, key)).willReturn(new AccessControlList());
        val env = new MapBuilder<String, String>().put(S3SftpServer.USERNAME, username)
                                                  .put(S3SftpServer.BUCKET, bucket)
                                                  .put(S3SftpServer.HOMEDIR, users)
                                                  .put(S3SftpServer.JAIL, key)
                                                  .put(
                                                          S3FileSystemProvider.AMAZON_S3_FACTORY_CLASS,
                                                          TestAmazonS3Factory.class.getCanonicalName()
                                                      )
                                                  .build();
        return new TestAmazonS3Configuration(path, fileSystemProvider, env);
    }
}
