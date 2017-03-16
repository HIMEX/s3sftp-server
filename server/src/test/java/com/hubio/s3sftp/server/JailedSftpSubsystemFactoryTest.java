package com.hubio.s3sftp.server;

import com.hubio.s3sftp.server.filesystem.UserFileSystemResolver;
import lombok.val;
import org.apache.sshd.server.subsystem.sftp.SftpEventListener;
import org.apache.sshd.server.subsystem.sftp.SftpEventListenerManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JailedSftpSubsystemFactory}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class JailedSftpSubsystemFactoryTest {

    private JailedSftpSubsystemFactory sftpSubsystemFactory;

    @Mock
    private SftpEventListener listener;

    @Mock
    private SessionBucket sessionBucket;

    @Mock
    private SessionHome sessionHome;

    @Mock
    private SessionJail sessionJail;

    @Mock
    private UserFileSystemResolver userFileSystemResolver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        sftpSubsystemFactory =
                new JailedSftpSubsystemFactory(sessionBucket, sessionHome, sessionJail, userFileSystemResolver);
    }

    @Test
    public void create() throws Exception {
        //given
        sftpSubsystemFactory.addSftpEventListener(listener);
        //when
        val sftpSubsystem = sftpSubsystemFactory.create();
        //then
        assertThat(sftpSubsystem).isInstanceOf(JailedSftpSubsystem.class);
        // that the listener was added to the command - true if matching listener was removed
        assertThat(((SftpEventListenerManager) sftpSubsystem).removeSftpEventListener(listener)).isTrue();
    }
}
