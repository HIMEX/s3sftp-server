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
import com.upplication.s3fs.S3FileSystemProvider;
import com.upplication.s3fs.S3Path;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * An {@link S3FileSystemProvider} that uses an {@link S3PathEnhancer} to modify the {@link Path} parameters.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Slf4j
@SuppressWarnings("classfanoutcomplexity")
class PathEnhancingS3SftpFileSystemProvider extends S3SftpFileSystemProviderDecorator {

    private final S3PathEnhancer s3PathEnhancer;

    /**
     * Constructor.
     *
     * @param provider       The provider to modify the paths of.
     * @param s3PathEnhancer The path enhancer to modify the paths with
     */
    PathEnhancingS3SftpFileSystemProvider(
            final S3SftpFileSystemProvider provider, final S3PathEnhancer s3PathEnhancer
                                         ) {
        super(provider);
        this.s3PathEnhancer = s3PathEnhancer;
    }

    @Override
    public FileChannel newFileChannel(
            final Path path, final Set<? extends OpenOption> options, final FileAttribute<?>[] attrs
                                     ) throws IOException {
        log.trace("newFileChannel({}, {}, {})", path, options, attrs);
        return super.newFileChannel(addBucket(path), options, attrs);
    }

    @Override
    public Map<String, Object> readAttributes(
            final Path path, final String attributes, final LinkOption... options
                                             ) throws IOException {
        log.trace("readAttributes[1]({}, {}, {})", path, attributes, options);
        return super.readAttributes(addBucket(path), attributes, options);
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(
            final Path path, final Class<A> type, final LinkOption... options
                                                           ) throws IOException {
        log.trace("readAttributes[2]({}, {}, {})", path, type, options);
        return super.readAttributes(addBucket(path), type, options);
    }

    @Override
    public void setAttribute(final Path path, final String attribute, final Object value, final LinkOption... options)
            throws IOException {
        super.setAttribute(addBucket(path), attribute, value, options);
    }

    @Override
    public FileSystem newFileSystem(final Path path, final Map<String, ?> env) throws IOException {
        log.trace("newFileSystem[2]({}, {})", path, env);
        return super.newFileSystem(addBucket(path), env);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(
            final Path dir, final DirectoryStream.Filter<? super Path> filter
                                                   ) throws IOException {
        log.trace("newDirectoryStream({}, {})", dir, filter);
        return super.newDirectoryStream(addBucket(dir), filter);
    }

    @Override
    public InputStream newInputStream(final Path path, final OpenOption... options) throws IOException {
        log.trace("newInputStream({}, {})", path, options);
        return super.newInputStream(addBucket(path), options);
    }

    @Override
    public SeekableByteChannel newByteChannel(
            final Path path, final Set<? extends OpenOption> options, final FileAttribute<?>[] attrs
                                             ) throws IOException {
        log.trace("newByteChannel({}, {}, {})", path, options, attrs);
        return super.newByteChannel(addBucket(path), options, attrs);
    }

    @Override
    public void createDirectory(final Path dir, final FileAttribute<?>[] attrs) throws IOException {
        log.trace("createDirectory({}, {})", dir, attrs);
        super.createDirectory(addBucket(dir), attrs);
    }

    @Override
    public void delete(final Path path) throws IOException {
        log.trace("delete({})", path);
        super.delete(addBucket(path));
    }

    @Override
    public void copy(final Path source, final Path target, final CopyOption... options) throws IOException {
        log.trace("copy({}, {}, {})", source, target, options);
        super.copy(addBucket(source), addBucket(target), options);
    }

    @Override
    public void move(final Path source, final Path target, final CopyOption... options) throws IOException {
        log.trace("move({}, {}, {})", source, target, options);
        super.move(addBucket(source), addBucket(target), options);
    }

    @Override
    public boolean isSameFile(final Path path1, final Path path2) throws IOException {
        log.trace("isSameFile({}, {})", path1, path2);
        return super.isSameFile(addBucket(path1), addBucket(path2));
    }

    @Override
    public boolean isHidden(final Path path) throws IOException {
        log.trace("isHidden({})", path);
        return super.isHidden(addBucket(path));
    }

    @Override
    public FileStore getFileStore(final Path path) throws IOException {
        log.trace("getFileStore({})", path);
        return super.getFileStore(addBucket(path));
    }

    @Override
    public void checkAccess(final Path path, final AccessMode... modes) throws IOException {
        log.trace("checkAccess({}, {})", path, modes);
        super.checkAccess(addBucket(path), modes);
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(
            final Path path, final Class<V> type, final LinkOption... options
                                                               ) {
        log.trace("getFileAttributeView({}, {}, {})", path, type, options);
        return super.getFileAttributeView(addBucket(path), type, options);
    }

    @Override
    public OutputStream newOutputStream(final Path path, final OpenOption... options) throws IOException {
        log.trace("newOutputStream({}, {})", path, options);
        return super.newOutputStream(addBucket(path), options);
    }

    @Override
    public AsynchronousFileChannel newAsynchronousFileChannel(
            final Path path, final Set<? extends OpenOption> options, final ExecutorService executor,
            final FileAttribute<?>[] attrs
                                                             ) throws IOException {
        log.trace("newAsynchronousFileChannel({}, {}, {}, {})", path, options, executor, attrs);
        return super.newAsynchronousFileChannel(addBucket(path), options, executor, attrs);
    }

    @Override
    public void createSymbolicLink(
            final Path link, final Path target, final FileAttribute<?>[] attrs
                                  ) throws IOException {
        log.trace("createSymbolicLink({}, {}, {})", link, target, attrs);
        super.createSymbolicLink(addBucket(link), addBucket(target), attrs);
    }

    @Override
    public void createLink(final Path link, final Path existing) throws IOException {
        log.trace("createLink({}, {})", link, existing);
        super.createLink(addBucket(link), addBucket(existing));
    }

    @Override
    public boolean deleteIfExists(final Path path) throws IOException {
        log.trace("deleteIfExists({})", path);
        return super.deleteIfExists(addBucket(path));
    }

    @Override
    public Path readSymbolicLink(final Path link) throws IOException {
        log.trace("readSymbolicLink({})", link);
        return super.readSymbolicLink(addBucket(link));
    }

    private S3Path addBucket(final Path path) {
        final S3Path result = s3PathEnhancer.apply((S3Path) path);
        log.trace("addBucket({}) => {}", path, result);
        return result;
    }
}
