package com.hubio.s3sftp.server;

import com.hubio.s3sftp.server.filesystem.S3SftpFileSystemProvider;
import com.hubio.s3sftp.server.filesystem.S3SftpFileSystemProviderFactory;
import com.hubio.s3sftp.server.filesystem.UserFileSystemResolver;
import com.upplication.s3fs.S3FileSystem;
import lombok.val;
import org.apache.sshd.server.session.ServerSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

/**
 * Tests for {@link }.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class S3FileSystemFactoryTest {

    private S3FileSystemFactory subject;

    private SftpSession sftpSession;

    @Mock
    private ServerSession serverSession;

    @Mock
    private S3FileSystem s3FileSystem;

    @Mock
    private S3SftpFileSystemProviderFactory fileSystemProviderFactory;

    @Mock
    private S3SftpFileSystemProvider s3SftpFileSystemProvider;

    @Mock
    private UserFileSystemResolver userFileSystemResolver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        sftpSession = SftpSession.of(serverSession);
        subject = new S3FileSystemFactory(session -> "bucket", session -> "home", session -> "", URI.create("uri"),
                                          fileSystemProviderFactory, userFileSystemResolver
        );
    }

    @Test
    public void shouldCreateFileSystem() throws Exception {
        //given
        val username = "newUser";
        given(serverSession.getUsername()).willReturn(username);
        given(userFileSystemResolver.resolve(username)).willReturn(Optional.empty());
        given(fileSystemProviderFactory.createWith(any(), eq(serverSession))).willReturn(s3SftpFileSystemProvider);
        given(s3SftpFileSystemProvider.getFileSystem(any(), any())).willReturn(s3FileSystem);
        given(s3SftpFileSystemProvider.getSession()).willReturn(serverSession);
        //when
        val result = subject.createFileSystem(serverSession);
        //then
        assertThat(result).isSameAs(s3FileSystem);
    }
}
