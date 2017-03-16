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

import com.amazonaws.services.s3.AmazonS3;
import com.upplication.s3fs.AmazonS3Factory;
import com.upplication.s3fs.S3FileSystem;
import com.upplication.s3fs.S3FileSystemProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.sshd.common.session.Session;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Wrapper for {@link S3FileSystemProvider} to allow use as a delegated object by making {@code protected} methods
 * {@code public}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Getter
@Slf4j
@RequiredArgsConstructor
class DelegatableS3FileSystemProvider extends S3FileSystemProvider implements S3SftpFileSystemProvider {

    private final Session session;

    @Setter
    private AmazonS3 amazonS3;

    @Override
    public String getFileSystemKey(final URI uri, final Properties props) {
        return super.getFileSystemKey(uri, props);
    }

    @Override
    public void validateUri(final URI uri) {
        super.validateUri(uri);
    }

    @Override
    public void overloadProperties(final Properties props, final Map<String, ?> env) {
        super.overloadProperties(props, env);
    }

    @Override
    public boolean fileSystemExists(final URI uri, final Map<String, ?> env) {
        log.trace("fileSystemExists({}, {})", uri, env);
        val fileSystemKey = getFileSystemKey(uri, mapAsProperties(env));
        log.trace(" - fileSystemKey: {}", fileSystemKey);
        val exists = getFilesystems().containsKey(fileSystemKey);
        log.trace(" <= exists: {}", exists);
        return exists;
    }

    @Override
    public S3FileSystemProvider getS3FileSystemProvider() {
        return this;
    }

    @Override
    public boolean overloadPropertiesWithEnv(final Properties props, final Map<String, ?> env, final String key) {
        return super.overloadPropertiesWithEnv(props, env, key);
    }

    @Override
    public List<S3FileSystem> getAllFileSystems() {
        return Collections.unmodifiableList(new ArrayList<>(getFilesystems().values()));
    }

    @Override
    public AmazonS3 getAmazonS3(final URI uri, final Properties props) {
        if (amazonS3 != null) {
            return amazonS3;
        }
        return super.getAmazonS3(uri, props);
    }

    @Override
    public AmazonS3Factory getAmazonS3Factory(final Properties props) {
        return super.getAmazonS3Factory(props);
    }

    @Override
    public S3FileSystem newFileSystem(final URI uri, final Properties props) {
        val env = new HashMap<String, String>();
        props.stringPropertyNames()
             .forEach(prop -> env.put(prop, props.getProperty(prop)));
        return (S3FileSystem) super.newFileSystem(uri, env);
    }

    @Override
    public Properties mapAsProperties(final Map<String, ?> map) {
        final Properties properties = new Properties();
        map.forEach(properties::put);
        return properties;
    }
}
