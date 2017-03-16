package com.hubio.s3sftp.server.filesystem;

import com.hubio.s3sftp.server.S3SftpServer;
import com.hubio.s3sftp.server.filesystem.DelegatableS3FileSystemProvider;
import com.hubio.s3sftp.server.filesystem.JailedS3SftpFileSystemProvider;
import com.upplication.s3fs.AmazonS3Factory;
import lombok.val;
import org.apache.sshd.common.session.Session;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.HashMap;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link }.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class JailedS3SftpFileSystemProviderTest {

    private JailedS3SftpFileSystemProvider subject;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private Session session;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new JailedS3SftpFileSystemProvider(new DelegatableS3FileSystemProvider(session));
    }

    @Test
    public void overloadProperties() throws Exception {
        //given
        val props = new Properties();
        val env = new HashMap<String, String>();
        val jail = "jail";
        env.put(S3SftpServer.JAIL, jail);
        //when
        subject.overloadProperties(props, env);
        //then
        assertThat(props).as("has jail key")
                         .containsKey(S3SftpServer.JAIL);
        assertThat(props.getProperty(S3SftpServer.JAIL)).as("has jail value")
                                                        .isEqualTo(jail);
    }

    @Test
    public void overloadPropertiesWithMissingJail() throws Exception {
        //given
        val props = new Properties();
        val env = new HashMap<String, String>();
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Jail not available");
        //when
        subject.overloadProperties(props, env);
    }

    @Test
    public void getFileSystemKey() throws Exception {
        //given
        final URI uri = URI.create("s3://uri");
        final Properties props = new Properties();
        props.setProperty(S3SftpServer.JAIL, "jail");
        props.setProperty(AmazonS3Factory.ACCESS_KEY, "accessKey");
        //when
        val result = subject.getFileSystemKey(uri, props);
        //then
        /// only the host and any accesskey is used to make the key (unless an username is included)
        assertThat(result).isEqualTo("accessKey@uri");
    }
}
