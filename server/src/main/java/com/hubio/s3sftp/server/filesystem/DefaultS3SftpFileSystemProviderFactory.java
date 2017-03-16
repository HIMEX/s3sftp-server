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

import com.hubio.s3sftp.server.S3PathEnhancer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.session.Session;

/**
 * Factory for creating an {@link S3SftpFileSystemProvider}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@SuppressWarnings("classdataabstractioncoupling")
@Slf4j
@RequiredArgsConstructor
class DefaultS3SftpFileSystemProviderFactory implements S3SftpFileSystemProviderFactory {

    @Override
    public S3SftpFileSystemProvider createWith(final S3PathEnhancer s3PathEnhancer, final Session session) {
        log.trace("createWith({})", s3PathEnhancer);
        S3SftpFileSystemProvider provider = new DelegatableS3FileSystemProvider(session);
        provider = new S3SftpFileSystemProviderDecorator(provider);
        provider = new FileChannelS3SftpFileSystemProvider(provider);
        provider = new PathEnhancingS3SftpFileSystemProvider(provider, s3PathEnhancer);
        provider = new PosixPermissionsS3SftpFileSystemProvider(provider);
        provider = new JailedS3SftpFileSystemProvider(provider);
        provider = new PerUserS3SftpFileSystemProvider(provider);
        provider = new SingleBucketS3SftpFileSystemProvider(provider);
        return provider;
    }
}
