package com.hubio.s3sftp.server.filesystem;

import com.hubio.s3sftp.server.S3PathEnhancer;
import com.hubio.s3sftp.server.filesystem.DefaultS3SftpFileSystemProviderFactory;
import lombok.val;
import org.apache.sshd.common.session.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultS3SftpFileSystemProviderFactory}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class DefaultS3SftpFileSystemProviderFactoryTest {

    private DefaultS3SftpFileSystemProviderFactory subject;

    @Mock
    private S3PathEnhancer s3PathEnhancer;

    @Mock
    private Session session;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new DefaultS3SftpFileSystemProviderFactory();
    }

    @Test
    public void createWith() throws Exception {
        //given
        //when
        val result = subject.createWith(s3PathEnhancer, session);
        //then
        assertThat(result).isNotNull();
    }
}
