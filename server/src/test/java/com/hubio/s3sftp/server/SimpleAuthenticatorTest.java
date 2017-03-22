package com.hubio.s3sftp.server;

import lombok.val;
import org.apache.sshd.server.session.ServerSession;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link }.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class SimpleAuthenticatorTest {

    private SimpleAuthenticator subject;

    @Mock
    private ServerSession session;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new SimpleAuthenticator();
    }

    @Test
    public void shouldAuthenticateUser() throws Exception {
        //given
        val username = "username";
        val password = "password";
        subject.addUser(username, password);
        //when
        val result = subject.authenticatePassword(username, password, SftpSession.of(session));
        //then
        assertThat(result).isTrue();
    }

    @Test
    public void shouldHaveInvalidPassword() throws Exception {
        //given
        val username = "username";
        val password = "password";
        subject.addUser(username, password);
        //when
        val result = subject.authenticatePassword(username, "wrong", SftpSession.of(session));
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldHaveInvalidUser() throws Exception {
        //given
        val username = "username";
        val password = "password";
        //when
        val result = subject.authenticatePassword(username, password, SftpSession.of(session));
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void authenticateWithValidUserAndValidPasswordAndHomeExists() throws Exception {
        //given
        val username = "username";
        val password = "password";
        subject.addUser(username, password);
        val session = mock(ServerSession.class);
        val homeDirExistsChecker = mock(HomeDirExistsChecker.class);
        subject.setHomeDirExistsChecker(homeDirExistsChecker);
        given(homeDirExistsChecker.check(username, session)).willReturn(true);
        //when
        val result = subject.authenticate(username, password, session);
        //then
        assertThat(result).isTrue();
    }

    @Test
    public void authenticateWithValidUserAndInvalidPassword() throws Exception {
        //given
        val username = "username";
        val password = "password";
        subject.addUser(username, password);
        val session = mock(ServerSession.class);
        val homeDirExistsChecker = mock(HomeDirExistsChecker.class);
        subject.setHomeDirExistsChecker(homeDirExistsChecker);
        given(homeDirExistsChecker.check(username, session)).willReturn(true);
        //when
        val result = subject.authenticate(username, "garbage", session);
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void authenticateWithValidUserValidPasswordAndMissingHome() throws Exception {
        //given
        val username = "username";
        val password = "password";
        subject.addUser(username, password);
        val session = mock(ServerSession.class);
        val homeDirExistsChecker = mock(HomeDirExistsChecker.class);
        subject.setHomeDirExistsChecker(homeDirExistsChecker);
        given(homeDirExistsChecker.check(username, session)).willReturn(false);
        //when
        val result = subject.authenticate(username, password, session);
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void authenticateWithValidUserValidPasswordAndMissingHomeChecker() throws Exception {
        //given
        val username = "username";
        val password = "password";
        subject.addUser(username, password);
        val session = mock(ServerSession.class);
        exception.expect(NullPointerException.class);
        exception.expectMessage("No HomeDirExistsChecker set");
        //when
        val result = subject.authenticate(username, password, session);
        //then
        assertThat(result).isFalse();
    }
}
