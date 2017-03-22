package com.hubio.s3sftp.server.filesystem;

import com.hubio.s3sftp.server.filesystem.DefaultUserFileSystemResolver;
import com.upplication.s3fs.S3FileSystem;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DefaultUserFileSystemResolver}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class DefaultUserFileSystemResolverTest {

    private DefaultUserFileSystemResolver subject;

    @Mock
    private S3FileSystem fileSystem;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        subject = new DefaultUserFileSystemResolver();
    }

    @Test
    public void resolveKnownUser() throws Exception {
        //given
        val username = "username";
        subject.put(username, fileSystem);
        //when
        val result = subject.resolve(username);
        //then
        assertThat(result).contains(fileSystem);
    }

    @Test
    public void resolveUnknownUser() throws Exception {
        //given
        val username = "username";
        //when
        val result = subject.resolve(username);
        //then
        assertThat(result).isEmpty();
    }
}
