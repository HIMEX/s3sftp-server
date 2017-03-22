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
import com.upplication.s3fs.S3FileSystem;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.util.Map;
import java.util.Properties;

/**
 * Restricts access to a single bucket.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Slf4j
class SingleBucketS3SftpFileSystemProvider extends S3SftpFileSystemProviderDecorator {

    /**
     * Constructor.
     *
     * @param provider The provider to be restricted.
     */
    SingleBucketS3SftpFileSystemProvider(final S3SftpFileSystemProvider provider) {
        super(provider);
    }

    @Override
    public void overloadProperties(final Properties props, final Map<String, ?> env) {
        super.overloadProperties(props, env);
        log.trace("overloadProperties({}, {})", props, env);
        // ensure that the logged in user is available within the created filesystem for access control checks
        if (!env.containsKey(S3SftpServer.BUCKET)) {
            throw new IllegalStateException("Bucket not available");
        }
        overloadPropertiesWithEnv(props, env, S3SftpServer.BUCKET);
    }

    @Override
    public S3FileSystem newFileSystem(final URI uri, final Properties props) {
        log.trace("createFileSystem({}, {})", uri, props);
        if (!props.containsKey(S3SftpServer.BUCKET)) {
            throw new IllegalArgumentException("Bucket not specified");
        }
        val bucketName = props.getProperty(S3SftpServer.BUCKET);
        log.debug("Creating filesystem mapping for bucket '{}' to {}", bucketName, uri);
        try {
            val fileSystemKey = getFileSystemKey(new URIBuilder(uri).setPath(bucketName)
                                                                    .build(), props);
            val amazonS3 = getAmazonS3(uri, props);
            val provider = new InvertedS3FileSystemProvider(this);
            val fileSystem = new FilteredS3FileSystem(provider, fileSystemKey, amazonS3, uri.getHost(), bucketName);
            log.trace(" <= fileSystem: {}", fileSystem);
            return fileSystem;
        } catch (final URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public FileSystem getFileSystem(final URI uri, final Map<String, ?> env) {
        log.trace("getFileSystem({}, {})", uri, env);
        if (fileSystemExists(uri, env)) {
            return super.getFileSystem(uri, env);
        }
        return newFileSystem(uri, mapAsProperties(env));
    }
}
