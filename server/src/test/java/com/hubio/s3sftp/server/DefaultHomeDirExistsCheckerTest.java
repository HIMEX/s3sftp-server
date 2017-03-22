package com.hubio.s3sftp.server;

import lombok.val;
import org.apache.sshd.server.session.ServerSession;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * Tests for {@link DefaultHomeDirExistsChecker}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class DefaultHomeDirExistsCheckerTest {

    private DefaultHomeDirExistsChecker subject;

    @Mock
    private JailedSftpSubsystemFactory sftpSubsystemFactory;

    @Mock
    private S3FileSystemFactory s3FilesystemFactory;

    private String username;

    @Mock
    private ServerSession session;

    @Mock
    private JailedSftpSubsystem sftpSubsystem;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new DefaultHomeDirExistsChecker(sftpSubsystemFactory, s3FilesystemFactory);
        username = "username";
        given(sftpSubsystemFactory.create()).willReturn(sftpSubsystem);
    }

    @Test
    public void shouldFindHomeDir() throws Exception {
        //given
        val mockHome = folder.newFolder()
                             .toPath();
        given(sftpSubsystem.resolveFile(any())).willReturn(mockHome);
        //when
        val result = subject.check(username, session);
        //then
        assertThat(result).isTrue();
    }

    @Test
    public void shouldNotFindHomeDir() throws Exception {
        //given
        given(sftpSubsystem.resolveFile(any())).willReturn(Paths.get("/garbage"));
        //when
        val result = subject.check(username, session);
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldHandleIOException() throws Exception {
        //given
        doThrow(IOException.class).when(sftpSubsystem)
                                  .resolveFile(any());
        //when
        val result = subject.check(username, session);
        //then
        assertThat(result).isFalse();
    }
}
