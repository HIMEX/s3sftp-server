package com.hubio.s3sftp.server.filechannel;

import com.hubio.s3sftp.server.filechannel.S3FileChannel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link S3FileChannel}.
 *
 * @author Paul Campbell
 */
public class S3FileChannelTest {

    private S3FileChannel wrapper;

    @Mock
    private SeekableByteChannel content;

    @Mock
    private ByteBuffer dst;

    @Mock
    private ByteBuffer dst1;

    @Mock
    private ByteBuffer dst2;

    private ByteBuffer[] destinationArray = new ByteBuffer[3];

    private final int offset = 1;

    private final int length = 2;

    private final long position = 1024L;

    @Mock
    private ByteBuffer src;

    @Mock
    private ByteBuffer src1;

    @Mock
    private ByteBuffer src2;

    private ByteBuffer[] srcs = new ByteBuffer[3];

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        wrapper = new S3FileChannel(content);
        destinationArray[0] = dst;
        destinationArray[1] = dst1;
        destinationArray[2] = dst2;
        srcs[0] = src;
        srcs[1] = src1;
        srcs[2] = src2;
    }

    private void expectUnsupportedOperationException() {
        exception.expect(UnsupportedOperationException.class);
    }

    @Test
    public void force() throws Exception {
        expectUnsupportedOperationException();
        wrapper.force(true);
    }

    @Test
    public void lockFile() throws Exception {
        expectUnsupportedOperationException();
        wrapper.lock();
    }

    @Test
    public void lockRegion() throws Exception {
        expectUnsupportedOperationException();
        wrapper.lock(0, 0, false);
    }

    @Test
    public void map() throws Exception {
        expectUnsupportedOperationException();
        wrapper.map(null, 0, 0);
    }

    @Test
    public void position() throws Exception {
        given(content.position()).willReturn(20L);
        assertThat(wrapper.position()).isEqualTo(20L);
    }

    @Test
    public void setPosition() throws Exception {
        wrapper.position(200L);
        verify(content).position(200L);
    }

    @Test
    public void readByteBuffer() throws Exception {
        given(content.read(dst)).willReturn(23);
        assertThat(wrapper.read(dst)).isEqualTo(23);
    }

    @Test
    public void readByteBufferArray() throws Exception {
        given(content.read(dst)).willReturn(20);
        given(content.read(dst1)).willReturn(123);
        given(content.read(dst2)).willReturn(321);
        assertThat(wrapper.read(destinationArray))
                .isEqualTo(20 + 123 + 321);
    }

    @Test
    public void readByteBufferArraySegment() throws Exception {
        given(content.read(dst)).willReturn(20);
        given(content.read(dst1)).willReturn(123);
        given(content.read(dst2)).willReturn(321);
        assertThat(wrapper.read(destinationArray, 1, 2))
                .isEqualTo(123 + 321);
    }

    @Test
    public void readByBufferFromPosition() throws Exception {
        expectUnsupportedOperationException();
        wrapper.read(dst, 123L);
    }

    @Test
    public void size() throws Exception {
        given(content.size()).willReturn(123L);
        assertThat(wrapper.size()).isEqualTo(123L);
    }

    @Test
    public void transferFrom() throws Exception {
        expectUnsupportedOperationException();
        wrapper.transferFrom(null, 0, 0);
    }

    @Test
    public void transferTo() throws Exception {
        expectUnsupportedOperationException();
        wrapper.transferTo(0, 0, null);
    }

    @Test
    public void truncate() throws Exception {
        wrapper.truncate(123L);
        verify(content).truncate(123L);
    }

    @Test
    public void tryLockFile() throws Exception {
        expectUnsupportedOperationException();
        wrapper.tryLock();
    }

    @Test
    public void tryLockRegion() throws Exception {
        expectUnsupportedOperationException();
        wrapper.tryLock(0, 0, false);
    }

    @Test
    public void writeByteBuffer() throws Exception {
        given(content.write(src)).willReturn(123);
        assertThat(wrapper.write(src)).isEqualTo(123);
    }

    @Test
    public void writeByteBufferArray() throws Exception {
        given(content.write(src)).willReturn(123);
        given(content.write(src1)).willReturn(321);
        given(content.write(src2)).willReturn(456);
        assertThat(wrapper.write(srcs)).isEqualTo(123 + 321 + 456);
    }

    @Test
    public void writeByteBufferArraySegment() throws Exception {
        given(content.write(src1)).willReturn(321);
        given(content.write(src2)).willReturn(456);
        assertThat(wrapper.write(srcs, 1, 2)).isEqualTo(321 + 456);
    }

    @Test
    public void writeByteBufferToPosition() throws Exception {
        expectUnsupportedOperationException();
        wrapper.write(src, 456L);
    }
}
