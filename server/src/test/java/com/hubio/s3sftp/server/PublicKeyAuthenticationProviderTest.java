package com.hubio.s3sftp.server;

import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.PublicKey;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PublicKeyAuthenticationProvider}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class PublicKeyAuthenticationProviderTest {

    private PublicKeyAuthenticationProvider subject;

    private boolean expectedResponse;

    @Mock
    private PublicKey key;

    @Mock
    private ServerSession session;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new PublicKeyAuthenticationProvider(new PublickeyAuthenticator() {
            @Override
            public boolean authenticate(
                    final String username, final PublicKey key, final ServerSession session
                                       ) {
                return expectedResponse;
            }
        });
    }

    @Test
    public void shouldDelegateToAuthenticator() {
        //given
        expectedResponse = false;
        //then
        assertThat(subject.authenticate("username", key, session)).isFalse();
        //given
        expectedResponse = true;
        //then
        assertThat(subject.authenticate("username", key, session)).isTrue();
    }
}
