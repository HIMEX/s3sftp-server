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

import org.apache.sshd.common.session.Session;

/**
 * Factory interface for creating File System objects.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public interface FileSystemProviderFactory {

    /**
     * Create a default user filesystem resolver.
     *
     * @return a filesystem resolver for users
     */
    static UserFileSystemResolver userResolver() {
        return new DefaultUserFileSystemResolver();
    }

    /**
     * Create a default factory for creating S3Sftp Filesystems.
     *
     * @return a factory for creating an S3Sftp Filesystem
     */
    static S3SftpFileSystemProviderFactory s3SftpProviderFactory() {
        return new DefaultS3SftpFileSystemProviderFactory();
    }

    /**
     * Create a provider of delegatable filesystems for the session.
     *
     * @param session The server session
     *
     * @return the filesystem provider
     */
    static S3SftpFileSystemProvider delegatableProvider(final Session session) {
        return new DelegatableS3FileSystemProvider(session);
    }

}
