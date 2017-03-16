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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.session.Session;

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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Decorator for {@link S3SftpFileSystemProvider}. The calls to the {@link S3FileSystemProvider} class are intercepted
 * and delegated to the {@link S3FileSystemProvider}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"classfanoutcomplexity", "methodcount"})
class S3SftpFileSystemProviderDecorator implements S3SftpFileSystemProvider {

    private final S3SftpFileSystemProvider provider;

    @Override
    public String getFileSystemKey(final URI uri, final Properties props) {
        return provider.getFileSystemKey(uri, props);
    }

    //    @Override
    //    public void validateUri(final URI uri) {
    //        provider.validateUri(uri);
    //    }

    @Override
    public void overloadProperties(final Properties props, final Map<String, ?> env) {
        provider.overloadProperties(props, env);
    }

    @Override
    public S3FileSystemProvider getS3FileSystemProvider() {
        return provider.getS3FileSystemProvider();
    }

    @Override
    public boolean overloadPropertiesWithEnv(final Properties props, final Map<String, ?> env, final String key) {
        return provider.overloadPropertiesWithEnv(props, env, key);
    }

    @Override
    public AmazonS3 getAmazonS3(final URI uri, final Properties props) {
        return provider.getAmazonS3(uri, props);
    }

    @Override
    public void setAmazonS3(final AmazonS3 amazonS3) {
        provider.setAmazonS3(amazonS3);
    }

    @Override
    public AmazonS3Factory getAmazonS3Factory(final Properties props) {
        return provider.getAmazonS3Factory(props);
    }

    //    @Override
    //    public AmazonS3Factory getAmazonS3Factory(final Properties props) {
    //        return provider.getAmazonS3Factory(props);
    //    }
    //
    //    @Override
    //    public String getScheme() {
    //        return provider.getScheme();
    //    }
    //
    //    @Override
    //    public FileSystem newFileSystem(final URI uri, final Map<String, ?> env) {
    //        return provider.newFileSystem(uri, env);
    //    }
    //
    //    @Override
    //    public boolean overloadPropertiesWithSystemProps(final Properties props, final String key) {
    //        return provider.overloadPropertiesWithSystemProps(props, key);
    //    }
    //
    //    @Override
    //    public boolean overloadPropertiesWithSystemEnv(final Properties props, final String key) {
    //        return provider.overloadPropertiesWithSystemEnv(props, key);
    //    }
    //
    //    @Override
    //    public String systemGetEnv(final String key) {
    //        return provider.systemGetEnv(key);
    //    }

    @Override
    public FileSystem getFileSystem(final URI uri, final Map<String, ?> env) {
        log.trace("getFileSystem({}, {})", uri, env);
        return provider.getFileSystem(uri, env);
    }

    //    @Override
    //    public S3FileSystem getFileSystem(final URI uri) {
    //        final FileSystem fileSystem = provider.getFileSystem(uri);
    //        if (fileSystem instanceof FilteredS3FileSystem) {
    //            return (S3FileSystem) fileSystem;
    //        }
    //        throw new IllegalStateException("FileSystem must be S3FileSystem");
    //    }

    @Override
    public Path getPath(final URI uri) {
        return provider.getPath(uri);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(
            final Path dir, final DirectoryStream.Filter<? super Path> filter
                                                   ) throws IOException {
        return provider.newDirectoryStream(dir, filter);
    }

    @Override
    public InputStream newInputStream(final Path path, final OpenOption... options) throws IOException {
        return provider.newInputStream(path, options);
    }

    @Override
    public SeekableByteChannel newByteChannel(
            final Path path, final Set<? extends OpenOption> options, final FileAttribute<?>[] attrs
                                             ) throws IOException {
        return provider.newByteChannel(path, options, attrs);
    }

    @Override
    public void createDirectory(final Path dir, final FileAttribute<?>[] attrs) throws IOException {
        provider.createDirectory(dir, attrs);
    }

    @Override
    public void delete(final Path path) throws IOException {
        provider.delete(path);
    }

    @Override
    public void copy(final Path source, final Path target, final CopyOption... options) throws IOException {
        provider.copy(source, target, options);
    }

    @Override
    public void move(final Path source, final Path target, final CopyOption... options) throws IOException {
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
    public Map<String, Object> readAttributes(
            final Path path, final String attributes, final LinkOption... options
                                             ) throws IOException {
        return provider.readAttributes(path, attributes, options);
    }

    @Override
    public void setAttribute(final Path path, final String attribute, final Object value, final LinkOption... options)
            throws IOException {
        provider.setAttribute(path, attribute, value, options);
    }

    @Override
    public List<S3FileSystem> getAllFileSystems() {
        return provider.getAllFileSystems();
    }

    @Override
    public Session getSession() {
        return provider.getSession();
    }

    //    @Override
    //    public Properties loadAmazonProperties() {
    //        return provider.loadAmazonProperties();
    //    }
    //
    //    @Override
    //    public void close(final S3FileSystem fileSystem) {
    //        provider.close(fileSystem);
    //    }
    //
    //    @Override
    //    public boolean isOpen(final S3FileSystem s3FileSystem) {
    //        return provider.isOpen(s3FileSystem);
    //    }
    //
    //    public static ConcurrentMap<String, S3FileSystem> getFilesystems() {
    //        return S3FileSystemProvider.getFilesystems();
    //    }
    //
    //    @Override
    //    public Cache getCache() {
    //        return provider.getCache();
    //    }
    //
    //    @Override
    //    public void setCache(final Cache cache) {
    //        provider.setCache(cache);
    //    }
    //
    //    public static List<FileSystemProvider> installedProviders() {
    //        return FileSystemProvider.installedProviders();
    //    }

    @Override
    public FileSystem newFileSystem(final Path path, final Map<String, ?> env) throws IOException {
        return provider.newFileSystem(path, env);
    }

    @Override
    public S3FileSystem newFileSystem(final URI uri, final Properties props) {
        return provider.newFileSystem(uri, props);
    }

    @Override
    public OutputStream newOutputStream(final Path path, final OpenOption... options) throws IOException {
        return provider.newOutputStream(path, options);
    }

    @Override
    public FileChannel newFileChannel(
            final Path path, final Set<? extends OpenOption> options, final FileAttribute<?>[] attrs
                                     ) throws IOException {
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
    public void createSymbolicLink(
            final Path link, final Path target, final FileAttribute<?>[] attrs
                                  ) throws IOException {
        provider.createSymbolicLink(link, target, attrs);
    }

    @Override
    public void createLink(final Path link, final Path existing) throws IOException {
        provider.createLink(link, existing);
    }

    @Override
    public boolean deleteIfExists(final Path path) throws IOException {
        return provider.deleteIfExists(path);
    }

    @Override
    public Path readSymbolicLink(final Path link) throws IOException {
        return provider.readSymbolicLink(link);
    }

    @Override
    public boolean fileSystemExists(final URI uri, final Map<String, ?> env) {
        log.trace("fileSystemExists({}, {})", uri, env);
        return provider.fileSystemExists(uri, env);
    }

    @Override
    public Properties mapAsProperties(final Map<String, ?> map) {
        return provider.mapAsProperties(map);
    }

    //    @Override
    //    public S3FileSystemProvider asS3FileSystemProvider() {
    //        return provider.asS3FileSystemProvider();
    //    }

}
