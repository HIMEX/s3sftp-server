package com.hubio.s3sftp.server;

import lombok.val;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

/**
 * Tests for {@link }.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class PasswordAuthenticationProviderTest {

    private PasswordAuthenticationProvider subject;

    private boolean authenticationResponse;

    @Mock
    private ServerSession session;

    @Mock
    private HomeDirExistsChecker homeDirExistsChecker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new PasswordAuthenticationProvider() {
            @Override
            public boolean authenticatePassword(
                    final String username, final String password, final SftpSession session
                                               ) throws PasswordChangeRequiredException {
                return authenticationResponse;
            }

            @Override
            public HomeDirExistsChecker getHomeDirExistsChecker() {
                given(homeDirExistsChecker.check(any(), any())).willReturn(authenticationResponse);
                return homeDirExistsChecker;
            }

            @Override
            public void setHomeDirExistsChecker(final HomeDirExistsChecker homeDirExistsChecker) {

            }
        };
    }

    @Test
    public void authenticatePassword() throws Exception {
        //given
        val username = "username";
        val password = "password";
        //when - false
        authenticationResponse = false;
        val resultFalse = subject.authenticate(username, password, session);
        //when - true
        authenticationResponse = true;
        val resultTrue = subject.authenticate(username, password, session);
        //then
        assertThat(resultFalse).isFalse();
        assertThat(resultTrue).isTrue();
    }
}
