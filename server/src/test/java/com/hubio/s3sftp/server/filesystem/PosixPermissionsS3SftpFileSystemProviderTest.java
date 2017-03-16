package com.hubio.s3sftp.server.filesystem;

import com.hubio.s3sftp.server.filesystem.PosixPermissionsS3SftpFileSystemProvider;
import com.hubio.s3sftp.server.filesystem.S3SftpFileSystemProvider;
import lombok.val;
import me.andrz.builder.map.MapBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link PosixPermissionsS3SftpFileSystemProvider}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class PosixPermissionsS3SftpFileSystemProviderTest {

    private PosixPermissionsS3SftpFileSystemProvider subject;

    @Mock
    private S3SftpFileSystemProvider delegate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new PosixPermissionsS3SftpFileSystemProvider(delegate);
    }

    @Test
    public void readAttributes() throws Exception {
        //given
        final Path path = mock(Path.class);
        final String attributes = "attributes";
        final LinkOption options = LinkOption.NOFOLLOW_LINKS;
        final Map<String, Object> expected = new MapBuilder<String, Object>().build();
        given(delegate.readAttributes(path, attributes, options)).willReturn(expected);
        //when
        val result = subject.readAttributes(path, attributes, options);
        //then
        assertThat(result).as("delegated result")
                          .isSameAs(expected)
                          .as("permissions added")
                          .containsKey("permissions");
    }
}
