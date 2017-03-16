package com.hubio.s3sftp.server;

import lombok.val;
import org.apache.sshd.server.session.ServerSession;
import org.junit.Test;

import java.net.SocketAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link SftpSession}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class SftpSessionTest {

    @Test
    public void shouldGetWrapperSession() {
        //given
        val serverSession = mock(ServerSession.class);
        val subject = SftpSession.of(serverSession);
        //when
        val result = subject.getServerSession();
        //then
        assertThat(result).isSameAs(serverSession);
    }

    @Test
    public void shouldGetClientAddress() {
        //given
        val clientAddress = mock(SocketAddress.class);
        val serverSession = mock(ServerSession.class);
        given(serverSession.getClientAddress()).willReturn(clientAddress);
        val subject = SftpSession.of(serverSession);
        //when
        val result = subject.getClientAddress();
        //then
        assertThat(result).isSameAs(clientAddress);
    }

    @Test
    public void shouldGetUsername() {
        //given
        val username = "Username";
        val serverSession = mock(ServerSession.class);
        given(serverSession.getUsername()).willReturn(username);
        val subject = SftpSession.of(serverSession);
        //when
        val result = subject.getUsername();
        //then
        assertThat(result).isEqualTo(username);
    }
}
