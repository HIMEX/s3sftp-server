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

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.util.Set;

/**
 * Factory interface for creating {@link FileChannel} instances.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public interface FileChannelFactory {

    /**
     * Create a {@link FileChannel} for the path with the specific open options.
     *
     * @param path        The path of the file to open an channel to
     * @param openOptions The options for opening the file channel
     *
     * @return The file channel
     *
     * @throws IOException if an I/O error occurs
     */
    static FileChannel of(final S3Path path, final Set<? extends OpenOption> openOptions) throws IOException {
        return new S3FileChannel(path, openOptions);
    }
}
