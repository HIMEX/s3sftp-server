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
import com.upplication.s3fs.util.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Mapper to allow an {@link S3SftpFileSystemProvider} as an {@link S3FileSystemProvider}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"classfanoutcomplexity", "methodcount"})
class InvertedS3FileSystemProvider extends S3FileSystemProvider {

    private final S3SftpFileSystemProvider provider;

    @Override
    public String getScheme() {
        log.error("getScheme - unsupported {}", getSessionId());
        throw new UnsupportedOperationException("Inverted - getScheme");
    }

    @Override
    public FileSystem newFileSystem(final URI uri, final Map<String, ?> env) {
        log.debug("newFileSystem {}", getSessionId());
        val props = new Properties();
        env.forEach(props::put);
        return provider.newFileSystem(uri, props);
    }

    @Override
    public FileSystem newFileSystem(final Path path, final Map<String, ?> env) throws IOException {
        return provider.newFileSystem(path, env);
    }

    @Override
    protected String getFileSystemKey(final URI uri, final Properties props) {
        return provider.getFileSystemKey(uri, props);
    }

    @Override
    protected void validateUri(final URI uri) {
        log.error("validateUri - unsupported {}", getSessionId());
        throw new UnsupportedOperationException("Inverted - validateUri");
    }

    @Override
    protected void overloadProperties(final Properties props, final Map<String, ?> env) {
        provider.overloadProperties(props, env);
    }

    @Override
    protected boolean overloadPropertiesWithEnv(final Properties props, final Map<String, ?> env, final String key) {
        return provider.overloadPropertiesWithEnv(props, env, key);
    }

    @Override
    public boolean overloadPropertiesWithSystemProps(final Properties props, final String key) {
        log.error("overloadPropertiesWithSystemProps - unsupported {}", getSessionId());
        throw new UnsupportedOperationException("Inverted - overloadPropertiesWithSystemProps");
    }

    @Override
    public boolean overloadPropertiesWithSystemEnv(final Properties props, final String key) {
        log.error("overloadPropertiesWithSystemEnv - unsupported {}", getSessionId());
        throw new UnsupportedOperationException("Inverted - overloadPropertiesWithSystemEnv");
    }

    @Override
    public String systemGetEnv(final String key) {
        log.error("systemGetEnv - unsupported {}", getSessionId());
        throw new UnsupportedOperationException("Inverted - systemGetEnv");
    }

    @Override
    public FileSystem getFileSystem(final URI uri, final Map<String, ?> env) {
        return provider.getFileSystem(uri, env);
    }

    @Override
    public S3FileSystem getFileSystem(final URI uri) {
        log.error("getFileSystem - unsupported {}", getSessionId());
        throw new UnsupportedOperationException("Inverted - getFileSystem");
    }

    @Override
    public Path getPath(final URI uri) {
        return provider.getPath(uri);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(
            final Path dir, final DirectoryStream.Filter<? super Path> filter
                                                   ) throws IOException {
        log.info("ls {} {}", dir, getSessionId());
        return provider.newDirectoryStream(dir, filter);
    }

    @Override
    public InputStream newInputStream(final Path path, final OpenOption... options) throws IOException {
        log.info("input stream: {} {}", path, getSessionId());
        return provider.newInputStream(path, options);
    }

    @Override
    public SeekableByteChannel newByteChannel(
            final Path path, final Set<? extends OpenOption> options, final FileAttribute<?>[] attrs
                                             ) throws IOException {
        log.info("byte channel: {} {}", path, getSessionId());
        return provider.newByteChannel(path, options, attrs);
    }

    @Override
    public void createDirectory(final Path dir, final FileAttribute<?>[] attrs) throws IOException {
        log.info("mkdir {}, {}", dir, getSessionId());
        provider.createDirectory(dir, attrs);
    }

    @Override
    public void delete(final Path path) throws IOException {
        log.info("rm {}, {}", path, getSessionId());
        provider.delete(path);
    }

    @Override
    public void copy(final Path source, final Path target, final CopyOption... options) throws IOException {
        log.info("cp {} {} {}", source, target, getSessionId());
        provider.copy(source, target, options);
    }

    @Override
    public void move(final Path source, final Path target, final CopyOption... options) throws IOException {
        log.info("mv {} {} {}", source, target, getSessionId());
        provider.move(source, target, options);
    }

    @Override
    public boolean isSameFile(final Path path1, final Path path2) throws IOException {
        return provider.isSameFile(path1, path2);
    }

    @Override
    public boolean isHidden(final Path path) throws IOException {
        return provider.isHidden(path);
    }

    @Override
    public FileStore getFileStore(final Path path) throws IOException {
        return provider.getFileStore(path);
    }

    @Override
    public void checkAccess(final Path path, final AccessMode... modes) throws IOException {
        provider.checkAccess(path, modes);
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(
            final Path path, final Class<V> type, final LinkOption... options
                                                               ) {
        return provider.getFileAttributeView(path, type, options);
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(
            final Path path, final Class<A> type, final LinkOption... options
                                                           ) throws IOException {
        return provider.readAttributes(path, type, options);
    }

    @Override
    public Map<String, Object> readAttributes(final Path path, final String attributes, final LinkOption... options)
            throws IOException {
        return provider.readAttributes(path, attributes, options);
    }

    @Override
    public void setAttribute(final Path path, final String attribute, final Object value, final LinkOption... options)
            throws IOException {
        provider.setAttribute(path, attribute, value, options);
    }

    @Override
    public S3FileSystem createFileSystem(final URI uri, final Properties props) {
        log.error("createFileSystem - unsupported {}", getSessionId());
        throw new UnsupportedOperationException("Inverted - createFileSystem");
    }

    @Override
    protected AmazonS3 getAmazonS3(final URI uri, final Properties props) {
        return provider.getAmazonS3(uri, props);
    }

    @Override
    protected AmazonS3Factory getAmazonS3Factory(final Properties props) {
        return provider.getAmazonS3Factory(props);
    }

    @Override
    public Properties loadAmazonProperties() {
        log.error("loadAmazonProperties - unsupported {}", getSessionId());
        throw new UnsupportedOperationException("Inverted - loadAmazonProperties");
    }

    @Override
    public void close(final S3FileSystem fileSystem) {
        log.info("close {}", getSessionId());
    }

    @Override
    public boolean isOpen(final S3FileSystem s3FileSystem) {
        log.error("isOpen - unsupported {}", getSessionId());
        throw new UnsupportedOperationException("Inverted - isOpen");
    }

    @Override
    public Cache getCache() {
        log.error("getCache - unsupported {}", getSessionId());
        throw new UnsupportedOperationException("Inverted - getCache");
    }

    @Override
    public void setCache(final Cache cache) {
        log.error("setCache - unsupported {}", getSessionId());
        throw new UnsupportedOperationException("Inverted - setCache");
    }

    @Override
    public OutputStream newOutputStream(final Path path, final OpenOption... options) throws IOException {
        log.info("output: {} {}", path, getSessionId());
        return provider.newOutputStream(path, options);
    }

    @Override
    public FileChannel newFileChannel(
            final Path path, final Set<? extends OpenOption> options, final FileAttribute<?>[] attrs
                                     ) throws IOException {
        log.info("channel: {} {}", path, getSessionId());
        return provider.newFileChannel(path, options, attrs);
    }

    @Override
    public AsynchronousFileChannel newAsynchronousFileChannel(
            final Path path, final Set<? extends OpenOption> options, final ExecutorService executor,
            final FileAttribute<?>[] attrs
                                                             ) throws IOException {
        return provider.newAsynchronousFileChannel(path, options, executor, attrs);
    }

    @Override
    public void createSymbolicLink(final Path link, final Path target, final FileAttribute<?>[] attrs)
            throws IOException {
        log.info("symlink: {} -> {} {}", link, target, getSessionId());
        provider.createSymbolicLink(link, target, attrs);
    }

    @Override
    public void createLink(final Path link, final Path existing) throws IOException {
        log.info("link: {} -> {} {}", link, existing, getSessionId());
        provider.createLink(link, existing);
    }

    @Override
    public boolean deleteIfExists(final Path path) throws IOException {
        log.info("rm (if exists) {} {}", path, getSessionId());
        return provider.deleteIfExists(path);
    }

    @Override
    public Path readSymbolicLink(final Path link) throws IOException {
        return provider.readSymbolicLink(link);
    }

    private String getSessionId() {
        return String.format("[%s@%s]", provider.getSession()
                                                .getUsername(), provider.getSession()
                                                                        .getIoSession()
                                                                        .getRemoteAddress());
    }
}
