package com.hubio.s3sftp.server;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AbstractAuthenticationProvider}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class AbstractAuthenticationProviderTest {

    private AbstractAuthenticationProvider subject;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new AbstractAuthenticationProvider() {
        };
    }

    @Test
    public void shouldReturnHomeDirExistsChecker() throws Exception {
        //given
        final HomeDirExistsChecker checker = (username, session) -> false;
        subject.setHomeDirExistsChecker(checker);
        //when
        final HomeDirExistsChecker result = subject.getHomeDirExistsChecker();
        //then
        assertThat(result).isSameAs(checker);
    }
}
