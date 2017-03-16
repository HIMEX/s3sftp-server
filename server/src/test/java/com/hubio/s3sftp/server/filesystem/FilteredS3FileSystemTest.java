package com.hubio.s3sftp.server.filesystem;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.hubio.s3sftp.server.filesystem.FilteredS3FileSystem;
import com.upplication.s3fs.S3FileSystemProvider;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Tests for {@link FilteredS3FileSystem}.
 *
 * @author Ross W. Drew
 * @author Paul Campbell
 */
public class FilteredS3FileSystemTest {

    private FilteredS3FileSystem filteredS3FileSystem;

    @Mock
    private S3FileSystemProvider s3FileSystemProvider;

    @Mock
    private AmazonS3 amazonS3;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        filteredS3FileSystem =
                new FilteredS3FileSystem(s3FileSystemProvider, "testKey", amazonS3, "testEndpoint", "testUserHome");
    }

    @Test
    public void testGetFileStoresWithValidHome() {
        //given
        given(amazonS3.listBuckets()).willReturn(
                Arrays.asList(new Bucket("invalidHome3"), new Bucket("invalidHome2"), new Bucket("testUserHome"),
                              // the only valid value
                              new Bucket("invalidHome1")
                             ));
        //when
        val fileStores = StreamSupport.stream(filteredS3FileSystem.getFileStores()
                                                                  .spliterator(), false)
                                      .collect(Collectors.toList());
        //then
        assertThat(fileStores).hasSize(1);
        assertThat(fileStores).extractingResultOf("name")
                              .containsOnly("testUserHome");
        assertThat(fileStores).extractingResultOf("type")
                              .containsOnly("S3Bucket");
    }

    @Test
    public void testGetFileStoresWithInvalidHome() {
        //given
        given(amazonS3.listBuckets()).willReturn(
                Arrays.asList(new Bucket("invalidHome3"), new Bucket("invalidHome2"), new Bucket("invalidHome1")));
        //when
        val fileStores = StreamSupport.stream(filteredS3FileSystem.getFileStores()
                                                                  .spliterator(), false)
                                      .collect(Collectors.toList());
        //then
        assertThat(fileStores).isEmpty();
    }
}
