package com.hubio.s3sftp.server;

import lombok.val;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link com.hubio.s3sftp.server.S3SftpServerConfiguration.S3SftpServerConfigurationBuilder}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class S3SftpServerConfigurationBuilderTest {

    @Test
    public void builderToString() {
        //given
        val builder = S3SftpServerConfiguration.builder()
                                               .port(200);
        //when
        val result = builder.toString();
        //then
        assertThat(result).contains("(port=200,");
    }
}
