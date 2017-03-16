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

package com.hubio.s3sftp.server.filesystem;

import com.hubio.s3sftp.server.filechannel.FileChannelFactory;
import com.upplication.s3fs.S3Path;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;

/**
 * Restores {@link FileChannel} support.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Slf4j
class FileChannelS3SftpFileSystemProvider extends S3SftpFileSystemProviderDecorator {

    /**
     * Constructor.
     *
     * @param provider The provider to restore {@link FileChannel} support to.
     */
    FileChannelS3SftpFileSystemProvider(final S3SftpFileSystemProvider provider) {
        super(provider);
    }

    @Override
    public FileChannel newFileChannel(
            final Path path, final Set<? extends OpenOption> options, final FileAttribute<?>... attrs
                                     ) throws IOException {
        log.trace("newFileChannel({}, {}, {})", path, options, attrs);
        if (path instanceof S3Path) {
            return FileChannelFactory.of((S3Path) path, options);
        }
        throw new IllegalArgumentException("path must be an instance of S3Path");
    }
}
