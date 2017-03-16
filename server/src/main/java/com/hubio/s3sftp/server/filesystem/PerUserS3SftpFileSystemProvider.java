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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

/**
 * Create a separate filesystem for each user.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Slf4j
class PerUserS3SftpFileSystemProvider extends S3SftpFileSystemProviderDecorator {

    /**
     * Constructor.
     *
     * @param provider The provider that should create different file systems for each user.
     */
    PerUserS3SftpFileSystemProvider(final S3SftpFileSystemProvider provider) {
        super(provider);
    }

    @Override
    public void overloadProperties(final Properties props, final Map<String, ?> env) {
        super.overloadProperties(props, env);
        log.trace("overloadProperties({}, {})", props, env);
        // ensure that the logged in user is available within the created filesystem for access control checks
        if (!env.containsKey(S3SftpServer.USERNAME)) {
            throw new IllegalStateException("Username not available");
        }
        overloadPropertiesWithEnv(props, env, S3SftpServer.USERNAME);
    }

    @Override
    public String getFileSystemKey(final URI uri, final Properties props) {
        log.trace("getFileSystemKey({}, {})", uri, props);
        if (!props.containsKey(S3SftpServer.USERNAME)) {
            throw new IllegalArgumentException("Username not specified");
        }
        URI uriForUser;
        try {
            uriForUser =
                    new URI(uri.getScheme(), props.getProperty(S3SftpServer.USERNAME), uri.getHost(), uri.getPort(),
                            uri.getPath(), uri.getQuery(), uri.getFragment()
                    );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid base URI: " + uri.toString(), e);
        }
        log.trace(" <= {}", uriForUser);
        return super.getFileSystemKey(uriForUser, props);
    }
}
