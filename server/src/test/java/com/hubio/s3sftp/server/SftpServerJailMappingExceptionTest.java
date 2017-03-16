package com.hubio.s3sftp.server;

import lombok.val;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SftpServerJailMappingException}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class SftpServerJailMappingExceptionTest {

    @Test
    public void shouldCreateException() {
        //given
        val jail = "jail";
        val home = "home";
        //when
        val result = new SftpServerJailMappingException(jail, home);
        //then
        assertThat(result.getMessage()).isEqualTo("User directory is outside jailed path: jail: home");
        assertThat(result.getInput()).isEqualTo(home);
    }
}
