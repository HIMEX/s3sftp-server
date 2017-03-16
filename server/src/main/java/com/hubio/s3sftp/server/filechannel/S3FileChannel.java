/**
 * The MIT License (MIT)
 * Copyright (c) 2017 Hubio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.hubio.s3sftp.server.filechannel;

import com.upplication.s3fs.S3Path;
import com.upplication.s3fs.S3SeekableByteChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.OpenOption;
import java.util.Set;

/**
 * Presents an {@link S3SeekableByteChannel} as a {@link FileChannel}.
 *
 * <p>A {@link FileChannel} that simply directs calls to
 * {@link S3SeekableByteChannel} so that Apache MINA can work with s3fs.</p>
 *
 * @author Ross W. Drew (ross.drew@hubio.com)
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Slf4j
class S3FileChannel extends FileChannel {

    private final SeekableByteChannel byteChannel;

    /**
     * Constructor.
     *
     * @param byteChannel The channel for reading and writing
     */
    S3FileChannel(final SeekableByteChannel byteChannel) {
        log.trace("new({})", byteChannel);
        this.byteChannel = byteChannel;
    }

    /**
     * Constructor.
     *
     * @param path    The path of the local file
     * @param options The options to use when opening the file
     *
     * @throws IOException if an I/O error occurs
     */
    S3FileChannel(final S3Path path, final Set<? extends OpenOption> options) throws IOException {
        log.trace("new({}, {})", path, options);
        this.byteChannel = new S3SeekableByteChannel(path, options);
    }

    @Override
    public int read(final ByteBuffer dst) throws IOException {
        log.trace("read({})", dst);
        return byteChannel.read(dst);
    }

    @Override
    public long read(final ByteBuffer[] dsts, final int offset, final int length) throws IOException {
        log.trace("read({}, {}, {})", dsts, offset, length);
        long total = 0L;
        final int end = offset + length;
        for (int i = offset; i < end; i++) {
            total += byteChannel.read(dsts[i]);
        }
        return total;
    }

    @Override
    public int read(final ByteBuffer dst, final long position) throws IOException {
        log.error("read({}, {})", dst, position);
        throw new UnsupportedOperationException();
    }

    @Override
    public int write(final ByteBuffer src, final long position) throws IOException {
        log.error("write({}, {})", src, position);
        throw new UnsupportedOperationException();
    }

    @Override
    public int write(final ByteBuffer src) throws IOException {
        log.trace("write({})", src);
        return byteChannel.write(src);
    }

    @Override
    public long write(final ByteBuffer[] srcs, final int offset, final int length) throws IOException {
        log.trace("write({}, {}, {})", srcs, offset, length);
        long total = 0L;
        final int end = offset + length;
        for (int i = offset; i < end; i++) {
            total += byteChannel.write(srcs[i]);
        }
        return total;
    }

    @Override
    public long position() throws IOException {
        log.trace("position()");
        return byteChannel.position();
    }

    @Override
    public FileChannel position(final long newPosition) throws IOException {
        log.trace("position({})", newPosition);
        return new S3FileChannel(byteChannel.position(newPosition));
    }

    @Override
    public long size() throws IOException {
        log.trace("size()");
        return byteChannel.size();
    }

    @Override
    public FileChannel truncate(final long size) throws IOException {
        log.trace("truncate({})", size);
        return new S3FileChannel(byteChannel.truncate(size));
    }

    @Override
    public void force(final boolean metaData) throws IOException {
        log.error("force({})", metaData);
        throw new UnsupportedOperationException();
    }

    @Override
    public long transferTo(final long position, final long count, final WritableByteChannel target) throws IOException {
        log.error("transferTo({}, {}, {})", position, count, target);
        throw new UnsupportedOperationException();
    }

    @Override
    public long transferFrom(final ReadableByteChannel src, final long position, final long count) throws IOException {
        log.error("transferFrom({}, {}, {})", src, position, count);
        throw new UnsupportedOperationException();
    }

    @Override
    public MappedByteBuffer map(final MapMode mode, final long position, final long size) throws IOException {
        log.error("map({}, {}, {})", mode, position, size);
        throw new UnsupportedOperationException();
    }

    @Override
    public FileLock lock(final long position, final long size, final boolean shared) throws IOException {
        log.error("lock({}, {}, {})", position, size, shared);
        throw new UnsupportedOperationException();
    }

    @Override
    public FileLock tryLock(final long position, final long size, final boolean shared) throws IOException {
        log.error("tryLock({}, {}, {})", position, size, shared);
        throw new UnsupportedOperationException();
    }

    @Override
    protected void implCloseChannel() throws IOException {
        log.trace("implCloseChannel()");
        byteChannel.close();
    }
}
