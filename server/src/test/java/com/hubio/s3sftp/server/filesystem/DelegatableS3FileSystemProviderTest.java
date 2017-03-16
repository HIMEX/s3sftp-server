package com.hubio.s3sftp.server.filesystem;

import com.amazonaws.services.s3.AmazonS3;
import com.hubio.s3sftp.server.filesystem.DelegatableS3FileSystemProvider;
import com.upplication.s3fs.AmazonS3Factory;
import lombok.val;
import org.apache.sshd.common.session.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DelegatableS3FileSystemProvider}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class DelegatableS3FileSystemProviderTest {

    private DelegatableS3FileSystemProvider subject;

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private Session session;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new DelegatableS3FileSystemProvider(session);
    }

    @Test
    public void getFileSystemKey() throws Exception {
        //given
        final URI uri = URI.create("s3://uri");
        final Properties props = new Properties();
        //when
        val result = subject.getFileSystemKey(uri, props);
        //then
        assertThat(result).isEqualTo("uri");
    }

    @Test
    public void validateUri() throws Exception {
        //given
        final URI uri = URI.create("s3://uri");
        //when
        subject.validateUri(uri);
        //then
        assertThat(true).as("No exception is thrown")
                        .isTrue();
    }

    @Test
    public void overloadProperties() throws Exception {
        //given
        final Properties props = new Properties();
        final Map<String, String> env = new HashMap<>();
        final String accessKey = "access key";
        env.put(AmazonS3Factory.ACCESS_KEY, accessKey);
        final String secretKey = "secret key";
        env.put(AmazonS3Factory.SECRET_KEY, secretKey);
        //when
        subject.overloadProperties(props, env);
        //then
        assertThat(props).contains(new AbstractMap.SimpleEntry<>(AmazonS3Factory.ACCESS_KEY, accessKey))
                         .contains(new AbstractMap.SimpleEntry<>(AmazonS3Factory.SECRET_KEY, secretKey));
    }

    @Test
    public void fileSystemExists() throws Exception {
        //given
        final URI uri = URI.create("s3://uri");
        final Map<String, String> env = new HashMap<>();

        /// force closure of any file systems held in static map in core FileSystemProvider
        subject.getAllFileSystems()
               .forEach(subject::close);
        //when
        val resultFalse = subject.fileSystemExists(uri, env);
        //then
        assertThat(resultFalse).as("filesystem does not exist")
                               .isFalse();

        //given
        subject.newFileSystem(uri, subject.mapAsProperties(env));
        //when
        val resultTrue = subject.fileSystemExists(uri, env);
        //then
        assertThat(resultTrue).as("filesystem does exist")
                              .isTrue();
    }

    @Test
    public void getS3FileSystemProvider() throws Exception {
        //given
        //when
        val result = subject.getS3FileSystemProvider();
        //then
        assertThat(result).isSameAs(subject);
    }

    @Test
    public void overloadPropertiesWithEnv() throws Exception {
        //given
        final Properties props = new Properties();
        final Map<String, String> env = new HashMap<>();
        final String key = "key";
        final String value = "value";
        env.put(key, value);
        env.put("other key", "other value");
        //when
        val result = subject.overloadPropertiesWithEnv(props, env, key);
        //then
        assertThat(result).isTrue();
        assertThat(props).containsExactly(new AbstractMap.SimpleEntry<>(key, value));
    }

    @Test
    public void getAmazonS3WhenSet() throws Exception {
        //given
        final URI uri = URI.create("S3://uri");
        final Properties props = new Properties();
        subject.setAmazonS3(amazonS3);
        //when
        val result = subject.getAmazonS3(uri, props);
        //then
        assertThat(result).isSameAs(amazonS3);
    }

    @Test
    public void getAmazonS3WhenNotSet() throws Exception {
        //given
        final URI uri = URI.create("S3://uri");
        final Properties props = new Properties();
        //when
        val result = subject.getAmazonS3(uri, props);
        //then
        assertThat(result).isNotNull();
    }

    @Test
    public void getAmazonS3Factory() throws Exception {
        //given
        final Properties props = new Properties();
        //when
        val result = subject.getAmazonS3Factory(props);
        //then
        assertThat(result).isNotNull();
    }

    @Test
    public void newFileSystem() throws Exception {
        //given
        final URI uri = URI.create("s3://uri");
        final Properties props = new Properties();
        //when
        val result = subject.newFileSystem(uri, props);
        //then
        assertThat(result).isNotNull();
    }

    @Test
    public void mapAsProperties() throws Exception {
        //given
        final Map<String, String> map = new HashMap<>();
        final String key = "key";
        final String value = "value";
        map.put(key, value);
        //when
        val result = subject.mapAsProperties(map);
        //then
        assertThat(result).containsExactly(new AbstractMap.SimpleEntry<>(key, value));
    }
}
