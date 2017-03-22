package com.hubio.s3sftp.server.filesystem;

import com.hubio.s3sftp.server.S3SftpServer;
import lombok.val;
import org.apache.sshd.common.session.Session;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import java.net.URI;
import java.util.HashMap;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PerUserS3SftpFileSystemProvider}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class PerUserS3SftpFileSystemProviderTest {

    private PerUserS3SftpFileSystemProvider subject;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private Session session;

    @Before
    public void setUp() throws Exception {
        subject = new PerUserS3SftpFileSystemProvider(new DelegatableS3FileSystemProvider(session));
    }

    @Test
    public void overloadProperties() throws Exception {
        //given
        val properties = new Properties();
        val env = new HashMap<String, String>();
        val username = "username";
        env.put(S3SftpServer.USERNAME, username);
        //when
        subject.overloadProperties(properties, env);
        //then
        assertThat(properties).as("Username key")
                              .containsOnlyKeys(S3SftpServer.USERNAME)
                              .as("Username correct")
                              .containsValue(username);
    }

    @Test
    public void overloadPropertiesWhenUsernameIsMissing() throws Exception {
        //given
        val properties = new Properties();
        val env = new HashMap<String, String>();
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Username not available");
        //when
        subject.overloadProperties(properties, env);
    }

    @Test
    public void getFileSystemKey() throws Exception {
        //given
        val hostname = "uri";
        val uri = URI.create("s3://" + hostname);
        val props = new Properties();
        val username = "username";
        props.setProperty(S3SftpServer.USERNAME, username);
        //when
        val result = subject.getFileSystemKey(uri, props);
        //then
        assertThat(result).isEqualTo(String.format("%s@%s", username, hostname));
    }

    @Test
    public void getFileSystemKeyWhenUsernameIsMissing() throws Exception {
        //given
        val hostname = "uri";
        val uri = URI.create("s3://" + hostname);
        val props = new Properties();
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Username not specified");
        //when
        subject.getFileSystemKey(uri, props);
        //then
        assertThat(true).isTrue();
    }

    @Test
    public void getFileSystemKeyWhenUriIsInvalid() throws Exception {
        //given
        val hostname = "uri+22";
        val uri = URI.create("s3://" + hostname);
        val props = new Properties();
        val username = "username";
        props.setProperty(S3SftpServer.USERNAME, username);
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Invalid base URI: s3://uri+22");
        //when
        subject.getFileSystemKey(uri, props);
    }
}
