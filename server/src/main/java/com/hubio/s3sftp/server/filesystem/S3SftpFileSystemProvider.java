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
import java.nio.file.Files;
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
 * Interface for S3 SFTP File System Providers.
 *
 * <p>Most methods reflect equivalent methods from the {@link java.nio.file.spi.FileSystemProvider} class.</p>
 *
 * <p>The {@link DelegatableS3FileSystemProvider} provides a base class for overlapping this interface atop the {@link
 * java.nio.file.spi.FileSystemProvider} class.</p>
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@SuppressWarnings({"classfanoutcomplexity", "methodcount"})
public interface S3SftpFileSystemProvider {

    /**
     * Get the provider as an {@link S3FileSystemProvider}.
     *
     * @return the {@link S3FileSystemProvider}
     */
    S3FileSystemProvider getS3FileSystemProvider();

    /**
     * Checks the existence, and optionally the accessibility, of a file.
     *
     * @param path  the path to the file to check
     * @param modes The access modes to check; may have zero elements
     *
     * @throws IOException if an I/O error occurs
     */
    void checkAccess(Path path, AccessMode... modes) throws IOException;

    /**
     * Copy a file to a target file.
     *
     * @param source  the path to the file to copy
     * @param target  the path to the target file
     * @param options options specifying how the copy should be done
     *
     * @throws IOException if an I/O error occurs
     */
    void copy(Path source, Path target, CopyOption... options) throws IOException;

    /**
     * Creates a new directory.
     *
     * @param dir   the directory to create
     * @param attrs an optional list of file attributes to set atomically when creating the directory
     *
     * @throws IOException if an I/O error occurs or the parent directory does not exist
     */
    void createDirectory(Path dir, FileAttribute<?>[] attrs) throws IOException;

    /**
     * Creates a new link (directory entry) for an existing file.
     *
     * @param link     the link (directory entry) to create
     * @param existing a path to an existing file
     *
     * @throws IOException if an I/O error occurs
     */
    void createLink(Path link, Path existing) throws IOException;

    /**
     * Creates a symbolic link to a target.
     *
     * @param link   the path of the symbolic link to create
     * @param target the target of the symbolic link
     * @param attrs  the array of attributes to set atomically when creating the symbolic link
     *
     * @throws IOException if an I/O error occurs
     */
    void createSymbolicLink(Path link, Path target, FileAttribute<?>[] attrs) throws IOException;

    /**
     * Deletes a file.
     *
     * @param path the path to the file to delete
     *
     * @throws IOException if an I/O error occurs
     */
    void delete(Path path) throws IOException;

    /**
     * Deletes a file if it exists.
     *
     * @param path the path to the file to delete
     *
     * @return {@code true} if the file was deleted by this method; {@code false} if the file could not be deleted
     * because it did not exist
     *
     * @throws IOException if an I/O error occurs
     */
    boolean deleteIfExists(Path path) throws IOException;

    /**
     * Check of the filesystem exists.
     *
     * @param uri URI of filesystem to check
     * @param env environment settings
     *
     * @return {@code true} if the file is found; otherwise {@code false}
     */
    boolean fileSystemExists(URI uri, Map<String, ?> env);

    /**
     * Returns an Amazon S3 client.
     *
     * @param uri   URI of the Amazon endpoint
     * @param props environment properties
     *
     * @return an Amazon S3 client
     */
    AmazonS3 getAmazonS3(URI uri, Properties props);

    /**
     * Sets the Amazon S3 client.
     *
     * <p>The default {@link #getAmazonS3(URI, Properties)} implementation uses the {@link S3FileSystemProvider}
     * implementation. Setting it here overrides that behaviour.</p>
     *
     * @param amazonS3 The AmazonS3 client
     */
    void setAmazonS3(AmazonS3 amazonS3);

    /**
     * Returns a factory for creating an Amazon S3 client.
     *
     * @param props environment properties
     *
     * @return The factory
     */
    AmazonS3Factory getAmazonS3Factory(Properties props);

    /**
     * Returns a file attribute view of a given type.
     *
     * @param <V>     The {@code FileAttributeView} type
     * @param path    the path to the file
     * @param type    the {@code Class} object corresponding to the file attribute view
     * @param options options indicating how symbolic links are handled
     *
     * @return a file attribute view of the specified type, or {@code null} if the attribute view type is not available
     */
    <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options);

    /**
     * Returns the {@link FileStore} representing the file store where a file is located.
     *
     * @param path the path to the file
     *
     * @return the file store where the file is stored
     *
     * @throws IOException if an I/O error occurs
     */
    FileStore getFileStore(Path path) throws IOException;

    /**
     * Get existing filesystem based on a combination of URI and env settings; create new filesystem otherwise.
     *
     * @param uri URI of existing, or to be created filesystem.
     * @param env environment settings.
     *
     * @return new or existing filesystem.
     */
    FileSystem getFileSystem(URI uri, Map<String, ?> env);

    /**
     * Get the file system key represented by: the access key @ endpoint.
     *
     * <p>Example: access-key@s3.amazonaws.com</p>
     *
     * <p>If uri host is empty then s3.amazonaws.com is used as host</p>
     *
     * @param uri   URI with the endpoint
     * @param props with the access key property
     *
     * @return String
     */
    String getFileSystemKey(URI uri, Properties props);

    /**
     * Return a {@code Path} object by converting the given {@link URI}.
     *
     * <p>The resulting {@code Path} is associated with a {@link FileSystem} that already exists or is constructed
     * automatically.</p>
     *
     * @param uri The URI to convert
     *
     * @return The resulting {@code Path}
     */

    Path getPath(URI uri);

    /**
     * Tells whether or not a file is considered <em>hidden</em>.
     *
     * <p>This method is invoked by the {@link Files#isHidden isHidden} method.</p>
     *
     * @param path the path to the file to test
     *
     * @return {@code true} if the file is considered hidden
     *
     * @throws IOException if an I/O error occurs
     */
    boolean isHidden(Path path) throws IOException;

    /**
     * Tests if two paths locate the same file.
     *
     * @param path1 one path to the file
     * @param path2 the other path
     *
     * @return {@code true} if, and only if, the two paths locate the same file
     *
     * @throws IOException if an I/O error occurs
     */
    boolean isSameFile(Path path1, Path path2) throws IOException;

    /**
     * Converts a map into a Properties object.
     *
     * @param map The map to convert
     *
     * @return The Properites
     */
    Properties mapAsProperties(Map<String, ?> map);

    /**
     * Move or rename a file to a target file.
     *
     * @param source  the path to the file to move
     * @param target  the path to the target file
     * @param options options specifying how the move should be done
     *
     * @throws IOException if an I/O error occurs
     */
    void move(Path source, Path target, CopyOption... options) throws IOException;

    /**
     * Opens or creates a file for reading and/or writing, returning an asynchronous file channel to access the file.
     *
     * @param path     the path of the file to open or create
     * @param options  options specifying how the file is opened
     * @param executor the thread pool or {@code null} to associate the channel with the default thread pool
     * @param attrs    an optional list of file attributes to set atomically when creating the file
     *
     * @return a new asynchronous file channel
     *
     * @throws IOException If an I/O error occurs
     */
    AsynchronousFileChannel newAsynchronousFileChannel(
            Path path, Set<? extends OpenOption> options, ExecutorService executor, FileAttribute<?>[] attrs
                                                      ) throws IOException;

    /**
     * Opens or creates a file, returning a seekable byte channel to access the file.
     *
     * @param path    the path to the file to open or create
     * @param options options specifying how the file is opened
     * @param attrs   an optional list of file attributes to set atomically when creating the file
     *
     * @return a new seekable byte channel
     *
     * @throws IOException if an I/O error occurs
     */
    SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>[] attrs)
            throws IOException;

    /**
     * Opens a directory, returning a {@code DirectoryStream} to iterate over the entries in the directory.
     *
     * @param dir    the path to the directory
     * @param filter the directory stream filter
     *
     * @return a new and open {@code DirectoryStream} object
     *
     * @throws IOException if an I/O error occurs
     */
    DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException;

    /**
     * Opens or creates a file for reading and/or writing, returning a file channel to access the file.
     *
     * @param path    the path of the file to open or create
     * @param options options specifying how the file is opened
     * @param attrs   an optional list of file attributes to set atomically when creating the file
     *
     * @return a new file channel
     *
     * @throws IOException If an I/O error occurs
     */
    FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException;


    /**
     * Constructs a new {@code FileSystem} object identified by a URI.
     *
     * @param uri   URI reference
     * @param props Provider specific properties to configure the file system
     *
     * @return A new file system
     */
    S3FileSystem newFileSystem(URI uri, Properties props);

    /**
     * Constructs a new {@code FileSystem} to access the contents of a file as a file system.
     *
     * @param path The path to the file
     * @param env  A map of provider specific properties to configure the file system; may be empty
     *
     * @return A new file system
     *
     * @throws IOException If an I/O error occurs
     */
    FileSystem newFileSystem(Path path, Map<String, ?> env) throws IOException;

    /**
     * Opens a file, returning an input stream to read from the file.
     *
     * @param path    the path to the file to open
     * @param options options specifying how the file is opened
     *
     * @return a new input stream
     *
     * @throws IOException if an I/O error occurs
     */
    InputStream newInputStream(Path path, OpenOption... options) throws IOException;

    /**
     * Opens or creates a file, returning an output stream that may be used to write bytes to the file.
     *
     * @param path    the path to the file to open or create
     * @param options options specifying how the file is opened
     *
     * @return a new output stream
     *
     * @throws IOException if an I/O error occurs
     */
    OutputStream newOutputStream(Path path, OpenOption... options) throws IOException;

    /**
     * Hook to allow copy select key/value pairs from {@code env} into {@code props}.
     *
     * <p>Implementations should use {@link #overloadPropertiesWithEnv(Properties, Map, String)} to perform the actual
     * copying, supplying only the required {@code key}s.</p>
     *
     * @param props The properties to copy to
     * @param env   The environment settings to copy from
     */
    void overloadProperties(Properties props, Map<String, ?> env);

    /**
     * Copy the selected key/value from the environment settings into the properties.
     *
     * @param props The properties to copy to
     * @param env   The environment settings to copy from
     * @param key   The key to be copied
     *
     * @return {@code true} if the key was found; otherwise {@code false}
     */
    boolean overloadPropertiesWithEnv(Properties props, Map<String, ?> env, String key);

    /**
     * Reads a set of file attributes as a bulk operation.
     *
     * @param path       the path to the file
     * @param attributes the attributes to read
     * @param options    options indicating how symbolic links are handled
     *
     * @return a map of the attributes returned; may be empty. The map's keys are the attribute names, its values are
     * the attribute values
     *
     * @throws IOException If an I/O error occurs
     */
    Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException;

    /**
     * Reads a file's attributes as a bulk operation.
     *
     * @param <A>     The {@code BasicFileAttributes} type
     * @param path    the path to the file
     * @param type    the {@code Class} of the file attributes required to read
     * @param options options indicating how symbolic links are handled
     *
     * @return the file attributes
     *
     * @throws IOException if an I/O error occurs
     */
    <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
            throws IOException;

    /**
     * Reads the target of a symbolic link.
     *
     * @param link the path to the symbolic link
     *
     * @return The target of the symbolic link
     *
     * @throws IOException if an I/O error occurs
     */
    Path readSymbolicLink(Path link) throws IOException;

    /**
     * Sets the value of a file attribute.
     *
     * <p>This implementation ignores the {@code permissions} attributes as they are not supported by Amazon S3.</p>
     *
     * @param path      the path to the file
     * @param attribute the attribute to set
     * @param value     the attribute value
     * @param options   options indicating how symbolic links are handled
     *
     * @throws IOException If an I/O error occurs
     */
    void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException;

    /**
     * Returns a list of all the open filesystems.
     *
     * @return a list of open filesystems
     */
    List<S3FileSystem> getAllFileSystems();

    /**
     * Returns the session the filesystem belongs to.
     *
     * @return the session
     */
    Session getSession();
}
