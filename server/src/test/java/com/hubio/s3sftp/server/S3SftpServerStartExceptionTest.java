package com.hubio.s3sftp.server;

import lombok.val;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link S3SftpServerStartException}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class S3SftpServerStartExceptionTest {

    @Test
    public void shouldCreateException() throws Exception{
        //given
        final Throwable cause = mock(Throwable.class);
        final String message = "message";
        //when
        val exception = new S3SftpServerStartException(message, cause);
        //then
        assertThat(exception.getMessage()).isSameAs(message);
        assertThat(exception.getCause()).isSameAs(cause);
    }
}
