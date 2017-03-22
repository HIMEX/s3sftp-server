package com.hubio.s3sftp.server;

import com.upplication.s3fs.S3FileSystem;
import com.upplication.s3fs.S3Path;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link }.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class FixedPrefixS3PathEnhancerTest {

    private FixedPrefixS3PathEnhancer subject;

    private String prefix;

    @Mock
    private S3FileSystem fileSystem;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        prefix = "/home";
        subject = new FixedPrefixS3PathEnhancer(prefix);
    }

    @Test
    public void shouldAlreadyStartWithPrefix() throws Exception {
        //given
        val path = new S3Path(fileSystem, prefix + "/user");
        //when
        val result = subject.apply(path);
        //then
        assertThat(result.toString()).isEqualTo("/home/user");
    }

    @Test
    public void shouldNeedPrefixAdded() throws Exception {
        //given
        val path = new S3Path(fileSystem, "/user");
        //when
        val result = subject.apply(path);
        //then
        assertThat(result.toString()).isEqualTo("/home/user");
    }
}
