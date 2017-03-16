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

import com.hubio.s3sftp.server.S3SftpServer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Properties;

/**
 * An {@link S3SftpFileSystemProvider} that supports the {@link S3SftpServer#JAIL} property.
 *
 * <p>While the provider does not use the property itself, it ensures that it is made available any filesystems that are
 * created.</p>
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Slf4j
class JailedS3SftpFileSystemProvider extends S3SftpFileSystemProviderDecorator {

    /**
     * Constructor.
     *
     * @param provider The provider to jail
     */
    JailedS3SftpFileSystemProvider(final S3SftpFileSystemProvider provider) {
        super(provider);
    }

    @Override
    public void overloadProperties(final Properties props, final Map<String, ?> env) {
        super.overloadProperties(props, env);
        log.trace("overloadProperties({}, {})", props, env);
        if (!env.containsKey(S3SftpServer.JAIL)) {
            throw new IllegalStateException("Jail not available");
        }
        overloadPropertiesWithEnv(props, env, S3SftpServer.JAIL);
    }
}
