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

package com.hubio.s3sftp.server;

import com.hubio.s3sftp.server.filesystem.S3SftpFileSystemProviderFactory;
import com.hubio.s3sftp.server.filesystem.UserFileSystemResolver;
import com.upplication.s3fs.S3FileSystem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import me.andrz.builder.map.MapBuilder;
import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.server.session.ServerSession;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;

/**
 * Factory for creating instances of an {@link S3FileSystem} for a given user session.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Slf4j
@RequiredArgsConstructor
class S3FileSystemFactory implements FileSystemFactory {

    private final SessionBucket sessionBucket;

    private final SessionHome sessionHome;

    private final SessionJail sessionJail;

    private final URI s3Uri;

    private final S3SftpFileSystemProviderFactory fileSystemProviderFactory;

    private final UserFileSystemResolver resolver;

    @Override
    public FileSystem createFileSystem(final Session session) throws IOException {
        log.trace("createFileSystem({})", session);
        val username = session.getUsername();
        return resolver.resolve(username)
                       .orElseGet(() -> getS3FileSystem(SftpSession.of((ServerSession) session), username));
    }

    private S3FileSystem getS3FileSystem(final SftpSession session, final String username) {
        val bucket = sessionBucket.getBucket(session);
        val homedir = sessionHome.getHomePath(session);
        val jail = sessionJail.getJail(session);
        val mapBuilder = new MapBuilder<String, String>();
        val env = mapBuilder.put(S3SftpServer.USERNAME, username)
                            .put(S3SftpServer.BUCKET, bucket)
                            .put(S3SftpServer.HOMEDIR, homedir)
                            .put(S3SftpServer.JAIL, jail)
                            .build();
        val s3PathEnhancer = new FixedPrefixS3PathEnhancer(String.format("/%s/%s", bucket, jail));
        val fileSystemProvider = fileSystemProviderFactory.createWith(s3PathEnhancer, session.getServerSession());
        val fs = (S3FileSystem) fileSystemProvider.getFileSystem(s3Uri, env);
        resolver.put(username, fs);
        return fs;
    }
}
