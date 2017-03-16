package com.hubio.s3sftp.server.filesystem;

import com.amazonaws.services.s3.AmazonS3;
import com.upplication.s3fs.AmazonS3Factory;
import com.upplication.s3fs.S3FileSystem;
import com.upplication.s3fs.util.Cache;
import lombok.val;
import me.andrz.builder.map.MapBuilder;
import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.common.session.Session;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link InvertedS3FileSystemProvider}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class InvertedS3FileSystemProviderTest {

    private InvertedS3FileSystemProvider subject;

    @Mock
    private S3SftpFileSystemProvider delegate;

    @Mock
    private Session session;

    @Mock
    private IoSession ioSession;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private SocketAddress remoteAddress;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new InvertedS3FileSystemProvider(delegate);
        given(delegate.getSession()).willReturn(session);
        given(session.getUsername()).willReturn("username");
        given(session.getIoSession()).willReturn(ioSession);
        remoteAddress = InetSocketAddress.createUnresolved("localhost", 22);
        given(ioSession.getRemoteAddress()).willReturn(remoteAddress);
    }

    @Test
    public void getScheme() throws Exception {
        //given
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage("Inverted - getScheme");
        //when
        subject.getScheme();
    }

    @Test
    public void newFileSystem() throws Exception {
        //given
        final URI uri = URI.create("s3://uri");
        final Map<String, String> env = new HashMap<>();
        final S3FileSystem expected = mock(S3FileSystem.class);
        given(delegate.newFileSystem(eq(uri), any())).willReturn(expected);
        //when
        val result = subject.newFileSystem(uri, env);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void newFileSystem1() throws Exception {
        //given
        final Path path = mock(Path.class);
        final Map<String, String> env = new HashMap<>();
        final S3FileSystem expected = mock(S3FileSystem.class);
        given(delegate.newFileSystem(eq(path), any())).willReturn(expected);
        //when
        val result = subject.newFileSystem(path, env);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void getFileSystemKey() throws Exception {
        //given
        final URI uri = URI.create("s3://uri");
        final Properties props = new Properties();
        final String expected = "expected";
        given(delegate.getFileSystemKey(uri, props)).willReturn(expected);
        //when
        val result = subject.getFileSystemKey(uri, props);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void validateUri() throws Exception {
        //given
        final URI uri = URI.create("s3://uri");
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage("Inverted - validateUri");
        //when
        subject.validateUri(uri);
    }

    @Test
    public void overloadProperties() throws Exception {
        //given
        final Properties props = new Properties();
        final Map<String, ?> env = new HashMap<>();
        //when
        subject.overloadProperties(props, env);
        //then
        then(delegate).should()
                      .overloadProperties(props, env);
    }

    @Test
    public void overloadPropertiesWithEnv() throws Exception {
        //given
        final Properties props = new Properties();
        final Map<String, ?> env = new HashMap<>();
        final String key = "key";
        given(delegate.overloadPropertiesWithEnv(props, env, key)).willReturn(true);
        //when
        val result = subject.overloadPropertiesWithEnv(props, env, key);
        //then
        then(delegate).should()
                      .overloadPropertiesWithEnv(props, env, key);
        assertThat(result).isTrue();
    }

    @Test
    public void overloadPropertiesWithSystemProps() throws Exception {
        //given
        final Properties props = new Properties();
        final String key = "key";
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage("Inverted - overloadPropertiesWithSystemProps");
        //when
        subject.overloadPropertiesWithSystemProps(props, key);
    }

    @Test
    public void overloadPropertiesWithSystemEnv() throws Exception {
        //given
        final Properties props = new Properties();
        final String key = "key";
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage("Inverted - overloadPropertiesWithSystemEnv");
        //when
        subject.overloadPropertiesWithSystemEnv(props, key);
    }

    @Test
    public void systemGetEnv() throws Exception {
        //given
        final String key = "key";
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage("Inverted - systemGetEnv");
        //when
        subject.systemGetEnv(key);
    }

    @Test
    public void getFileSystem() throws Exception {
        //given
        final URI uri = URI.create("s3://uri");
        final Map<String, ?> env = new HashMap<>();
        final FileSystem expected = mock(FileSystem.class);
        given(delegate.getFileSystem(uri, env)).willReturn(expected);
        //when
        val result = subject.getFileSystem(uri, env);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void getFileSystem1() throws Exception {
        //given
        final URI uri = URI.create("s3://uri");
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage("Inverted - getFileSystem");
        //when
        subject.getFileSystem(uri);
    }

    @Test
    public void getPath() throws Exception {
        //given
        final URI uri = URI.create("s3://uri");
        final Path expected = mock(Path.class);
        given(delegate.getPath(uri)).willReturn(expected);
        //when
        val result = subject.getPath(uri);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void newDirectoryStream() throws Exception {
        //given
        final Path dir = mock(Path.class);
        final DirectoryStream.Filter<? super Path> filter = (DirectoryStream.Filter<Path>) entry -> false;
        final DirectoryStream<Path> expected = mock(DirectoryStream.class);
        given(delegate.newDirectoryStream(dir, filter)).willReturn(expected);
        //when
        val result = subject.newDirectoryStream(dir, filter);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void newInputStream() throws Exception {
        //given
        final Path path = mock(Path.class);
        final OpenOption options = StandardOpenOption.APPEND;
        final InputStream expected = mock(InputStream.class);
        given(delegate.newInputStream(path, options)).willReturn(expected);
        //when
        val result = subject.newInputStream(path, options);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void newByteChannel() throws Exception {
        //given
        final Path path = mock(Path.class);
        final Set<? extends OpenOption> options = EnumSet.noneOf(StandardOpenOption.class);
        final FileAttribute<?>[] attrs = new FileAttribute[0];
        final SeekableByteChannel expected = mock(SeekableByteChannel.class);
        given(delegate.newByteChannel(path, options, attrs)).willReturn(expected);
        //when
        val result = subject.newByteChannel(path, options, attrs);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void createDirectory() throws Exception {
        //given
        final Path path = mock(Path.class);
        final FileAttribute<?>[] attrs = new FileAttribute[0];
        //when
        subject.createDirectory(path, attrs);
        //then
        then(delegate).should()
                      .createDirectory(path, attrs);
    }

    @Test
    public void delete() throws Exception {
        //given
        final Path path = mock(Path.class);
        //when
        subject.delete(path);
        //then
        then(delegate).should()
                      .delete(path);
    }

    @Test
    public void copy() throws Exception {
        //given
        final Path source = mock(Path.class);
        final Path target = mock(Path.class);
        final CopyOption options = StandardCopyOption.ATOMIC_MOVE;
        //when
        subject.copy(source, target, options);
        //then
        then(delegate).should()
                      .copy(source, target, options);
    }

    @Test
    public void move() throws Exception {
        //given
        final Path source = mock(Path.class);
        final Path target = mock(Path.class);
        final CopyOption options = StandardCopyOption.ATOMIC_MOVE;
        //when
        subject.move(source, target, options);
        //then
        then(delegate).should()
                      .move(source, target, options);
    }

    @Test
    public void isSameFile() throws Exception {
        //given
        final Path path1 = mock(Path.class);
        final Path path2 = mock(Path.class);
        given(delegate.isSameFile(path1, path2)).willReturn(true)
                                                .willReturn(false);
        //when
        val resultTrue = subject.isSameFile(path1, path2);
        val resultFalse = subject.isSameFile(path1, path2);
        //then
        assertThat(resultTrue).isTrue();
        assertThat(resultFalse).isFalse();
    }

    @Test
    public void isHidden() throws Exception {
        //given
        final Path path = mock(Path.class);
        given(delegate.isHidden(path)).willReturn(true)
                                      .willReturn(false);
        //when
        val resultTrue = subject.isHidden(path);
        val resultFalse = subject.isHidden(path);
        //then
        assertThat(resultTrue).isTrue();
        assertThat(resultFalse).isFalse();
    }

    @Test
    public void getFileStore() throws Exception {
        //given
        final Path path = mock(Path.class);
        final FileStore expected = mock(FileStore.class);
        given(delegate.getFileStore(path)).willReturn(expected);
        //when
        val result = subject.getFileStore(path);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void checkAccess() throws Exception {
        //given
        final Path path = mock(Path.class);
        final AccessMode modes = AccessMode.READ;
        //when
        subject.checkAccess(path, modes);
        //then
        then(delegate).should()
                      .checkAccess(path, modes);
    }

    @Test
    public void getFileAttributeView() throws Exception {
        //given
        final Path path = mock(Path.class);
        final Class<FileAttributeView> type = FileAttributeView.class;
        final LinkOption options = LinkOption.NOFOLLOW_LINKS;
        final FileAttributeView expected = mock(FileAttributeView.class);
        given(delegate.getFileAttributeView(path, type, options)).willReturn(expected);
        //when
        val result = subject.getFileAttributeView(path, type, options);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void readAttributes() throws Exception {
        //given
        final Path path = mock(Path.class);
        final Class<BasicFileAttributes> type = BasicFileAttributes.class;
        final LinkOption options = LinkOption.NOFOLLOW_LINKS;
        final BasicFileAttributes expected = mock(BasicFileAttributes.class);
        given(delegate.readAttributes(path, type, options)).willReturn(expected);
        //when
        val result = subject.readAttributes(path, type, options);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void readAttributes1() throws Exception {
        //given
        final Path path = mock(Path.class);
        final String attributes = "attributes";
        final LinkOption options = LinkOption.NOFOLLOW_LINKS;
        final Map<String, Object> expected = new MapBuilder<String, Object>().build();
        given(delegate.readAttributes(path, attributes, options)).willReturn(expected);
        //when
        val result = subject.readAttributes(path, attributes, options);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void setAttribute() throws Exception {
        //given
        final Path path = mock(Path.class);
        final String attribute = "attribute";
        final Object value = new Object();
        final LinkOption options = LinkOption.NOFOLLOW_LINKS;
        //when
        subject.setAttribute(path, attribute, value, options);
        //then
        then(delegate).should()
                      .setAttribute(path, attribute, value, options);
    }

    @Test
    public void createFileSystem() throws Exception {
        //given
        final URI uri = URI.create("s3://uri");
        final Properties props = new Properties();
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage("Inverted - createFileSystem");
        //when
        subject.createFileSystem(uri, props);
    }

    @Test
    public void getAmazonS3() throws Exception {
        //given
        final URI uri = URI.create("s3://uri");
        final Properties props = new Properties();
        final AmazonS3 expected = mock(AmazonS3.class);
        given(delegate.getAmazonS3(uri, props)).willReturn(expected);
        //when
        val result = subject.getAmazonS3(uri, props);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void getAmazonS3Factory() throws Exception {
        //given
        final Properties props = new Properties();
        final AmazonS3Factory expected = mock(AmazonS3Factory.class);
        given(delegate.getAmazonS3Factory(props)).willReturn(expected);
        //when
        val result = subject.getAmazonS3Factory(props);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void loadAmazonProperties() throws Exception {
        //given
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage("Inverted - loadAmazonProperties");
        //when
        subject.loadAmazonProperties();
    }

    @Test
    public void close() throws Exception {
        //given
        final S3FileSystem s3FileSystem = mock(S3FileSystem.class);
        //when
        subject.close(s3FileSystem);
        //then
        /// no exception thrown
    }

    @Test
    public void isOpen() throws Exception {
        //given
        final S3FileSystem s3FileSystem = mock(S3FileSystem.class);
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage("Inverted - isOpen");
        //when
        subject.isOpen(s3FileSystem);
    }

    @Test
    public void getCache() throws Exception {
        //given
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage("Inverted - getCache");
        //when
        subject.getCache();
    }

    @Test
    public void setCache() throws Exception {
        //given
        final Cache cache = mock(Cache.class);
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage("Inverted - setCache");
        //when
        subject.setCache(cache);
    }

    @Test
    public void newOutputStream() throws Exception {
        //given
        final Path path = mock(Path.class);
        final OpenOption options = StandardOpenOption.WRITE;
        final OutputStream expected = mock(OutputStream.class);
        given(delegate.newOutputStream(path, options)).willReturn(expected);
        //when
        val result = subject.newOutputStream(path, options);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void newFileChannel() throws Exception {
        //given
        final Path path = mock(Path.class);
        final Set<? extends OpenOption> options = EnumSet.of(StandardOpenOption.READ);
        final FileAttribute<?>[] attrs = new FileAttribute[0];
        final FileChannel expected = mock(FileChannel.class);
        given(delegate.newFileChannel(path, options, attrs)).willReturn(expected);
        //when
        val result = subject.newFileChannel(path, options, attrs);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void newAsynchronousFileChannel() throws Exception {
        //given
        final Path path = mock(Path.class);
        final Set<? extends OpenOption> options = EnumSet.of(StandardOpenOption.READ);
        final FileAttribute<?>[] attrs = new FileAttribute[0];
        final ExecutorService executor = mock(ExecutorService.class);
        final AsynchronousFileChannel expected = mock(AsynchronousFileChannel.class);
        given(delegate.newAsynchronousFileChannel(path, options, executor, attrs)).willReturn(expected);
        //when
        val result = subject.newAsynchronousFileChannel(path, options, executor, attrs);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void createSymbolicLink() throws Exception {
        //given
        final Path link = mock(Path.class);
        final Path target = mock(Path.class);
        final FileAttribute<?>[] attrs = new FileAttribute[0];
        //when
        subject.createSymbolicLink(link, target, attrs);
        //then
        then(delegate).should()
                      .createSymbolicLink(link, target, attrs);
    }

    @Test
    public void createLink() throws Exception {
        //given
        final Path link = mock(Path.class);
        final Path existing = mock(Path.class);
        //when
        subject.createLink(link, existing);
        //then
        then(delegate).should()
                      .createLink(link, existing);
    }

    @Test
    public void deleteIfExists() throws Exception {
        //given
        final Path path = mock(Path.class);
        given(delegate.deleteIfExists(path)).willReturn(true)
                                            .willReturn(false);
        //when
        val resultTrue = subject.deleteIfExists(path);
        val resultFalse = subject.deleteIfExists(path);
        //then
        assertThat(resultTrue).isTrue();
        assertThat(resultFalse).isFalse();
    }

    @Test
    public void readSymbolicLink() throws Exception {
        //given
        final Path link = mock(Path.class);
        final Path expected = mock(Path.class);
        given(delegate.readSymbolicLink(link)).willReturn(expected);
        //when
        val result = subject.readSymbolicLink(link);
        //then
        assertThat(result).isSameAs(expected);
    }
}
