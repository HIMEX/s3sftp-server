package com.hubio.s3sftp.server.filesystem;

import com.amazonaws.services.s3.AmazonS3;
import com.hubio.s3sftp.server.S3PathEnhancer;
import com.hubio.s3sftp.server.filesystem.DelegatableS3FileSystemProvider;
import com.hubio.s3sftp.server.filesystem.PathEnhancingS3SftpFileSystemProvider;
import com.upplication.s3fs.S3FileSystem;
import com.upplication.s3fs.S3Path;
import lombok.val;
import org.apache.sshd.client.subsystem.sftp.SftpDirectoryStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link }.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class PathEnhancingS3SftpFileSystemProviderTest {

    private PathEnhancingS3SftpFileSystemProvider provider;

    @Mock
    private S3Path rawPath;

    @Mock
    private S3Path enhancedPath;

    @Mock
    private DelegatableS3FileSystemProvider delegatedProvider;

    @Mock
    private S3PathEnhancer s3PathEnhancer;

    @Mock
    private FileSystem fileSystem;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        given(s3PathEnhancer.apply(rawPath)).willReturn(enhancedPath);
        provider = new PathEnhancingS3SftpFileSystemProvider(delegatedProvider, s3PathEnhancer);
    }

    @Test
    public void newFileChannel() throws Exception {
        //given
        val options = new HashSet<OpenOption>();
        val attrs = new FileAttribute[]{};
        val expected = mock(FileChannel.class);
        given(delegatedProvider.newFileChannel(enhancedPath, options, attrs)).willReturn(expected);
        //when
        final FileChannel result = provider.newFileChannel(rawPath, options, attrs);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void readAttributesByType() throws Exception {
        //given
        val type = BasicFileAttributes.class;
        val options = LinkOption.NOFOLLOW_LINKS;
        val expected = mock(BasicFileAttributes.class);
        given(delegatedProvider.readAttributes(enhancedPath, type, options)).willReturn(expected);
        //when
        val result = provider.readAttributes(rawPath, type, options);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void readAttributesByName() throws Exception {
        //given
        val attributes = "attributes";
        val options = LinkOption.NOFOLLOW_LINKS;
        val expected = new HashMap<String, Object>();
        given(delegatedProvider.readAttributes(enhancedPath, attributes, options)).willReturn(expected);
        //when
        val result = provider.readAttributes(rawPath, attributes, options);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void newFileSystemByPath() throws Exception {
        //given
        val env = new HashMap<String, Object>();
        final FileSystem expected = mock(FileSystem.class);
        given(delegatedProvider.newFileSystem(enhancedPath, env)).willReturn(expected);
        //when
        val result = provider.newFileSystem(rawPath, env);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void newDirectoryStream() throws Exception {
        //given
        final DirectoryStream.Filter<Path> filter = entry -> false;
        val expected = mock(SftpDirectoryStream.class);
        given(delegatedProvider.newDirectoryStream(enhancedPath, filter)).willReturn(expected);
        //when
        val result = provider.newDirectoryStream(rawPath, filter);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void newInputStream() throws Exception {
        //given
        val options = mock(OpenOption.class);
        final InputStream expected = mock(InputStream.class);
        given(delegatedProvider.newInputStream(enhancedPath, options)).willReturn(expected);
        //when
        val result = provider.newInputStream(rawPath, options);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void newByteChannel() throws Exception {
        //given
        val options = new HashSet<OpenOption>();
        val attrs = new FileAttribute[]{};
        val expected = mock(SeekableByteChannel.class);
        given(delegatedProvider.newByteChannel(enhancedPath, options, attrs)).willReturn(expected);
        //when
        val result = provider.newByteChannel(rawPath, options, attrs);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void createDirectory() throws Exception {
        //given
        val attrs = new FileAttribute[]{};
        //when
        provider.createDirectory(rawPath, attrs);
        //then
        then(delegatedProvider).should()
                               .createDirectory(enhancedPath, attrs);
    }

    @Test
    public void delete() throws Exception {
        //given
        //when
        provider.delete(rawPath);
        //then
        then(delegatedProvider).should()
                               .delete(enhancedPath);
    }

    @Test
    public void copy() throws Exception {
        //given
        val options = mock(CopyOption.class);
        val target = mock(S3Path.class);
        val enhancedTarget = mock(S3Path.class);
        given(s3PathEnhancer.apply(target)).willReturn(enhancedTarget);
        //when
        provider.copy(rawPath, target, options);
        //then
        then(delegatedProvider).should()
                               .copy(enhancedPath, enhancedTarget, options);
    }

    @Test
    public void move() throws Exception {
        //given
        val target = mock(S3Path.class);
        val enhancedTarget = mock(S3Path.class);
        given(s3PathEnhancer.apply(target)).willReturn(enhancedTarget);
        //when
        provider.move(rawPath, target);
        //then
        then(delegatedProvider).should()
                               .move(enhancedPath, enhancedTarget);
    }

    @Test
    public void isSameFile() throws Exception {
        //given
        val path2 = mock(S3Path.class);
        val enhancedPath2 = mock(S3Path.class);
        given(s3PathEnhancer.apply(path2)).willReturn(enhancedPath2);
        given(delegatedProvider.isSameFile(enhancedPath, enhancedPath2)).willReturn(true)
                                                                        .willReturn(false);
        //when
        final boolean result1 = provider.isSameFile(rawPath, path2);
        final boolean result2 = provider.isSameFile(rawPath, path2);
        //then
        assertThat(result1).isTrue();
        assertThat(result2).isFalse();
    }

    @Test
    public void isHidden() throws Exception {
        //given
        given(delegatedProvider.isHidden(enhancedPath)).willReturn(true)
                                                       .willReturn(false);
        //when
        val result1 = provider.isHidden(rawPath);
        val result2 = provider.isHidden(rawPath);
        //then
        assertThat(result1).isTrue();
        assertThat(result2).isFalse();
    }

    @Test
    public void checkAccess() throws Exception {
        //given
        val modes = AccessMode.READ;
        //when
        provider.checkAccess(rawPath, modes);
        //then
        then(delegatedProvider).should()
                               .checkAccess(enhancedPath, modes);
    }

    @Test
    public void newOutputStream() throws Exception {
        //given
        val options = mock(OpenOption.class);
        val expected = mock(OutputStream.class);
        given(delegatedProvider.newOutputStream(enhancedPath, options)).willReturn(expected);
        //when
        val result = provider.newOutputStream(rawPath, options);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void newAsynchronousFileChannel() throws Exception {
        //given
        val options = new HashSet<OpenOption>();
        val executor = mock(ExecutorService.class);
        val attrs = new FileAttribute[]{};
        val expected = mock(AsynchronousFileChannel.class);
        given(delegatedProvider.newAsynchronousFileChannel(enhancedPath, options, executor, attrs)).willReturn(
                expected);
        //when
        val result = provider.newAsynchronousFileChannel(rawPath, options, executor, attrs);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void createSymbolicLink() throws Exception {
        //given
        val target = mock(S3Path.class);
        val enhancedTarget = mock(S3Path.class);
        given(s3PathEnhancer.apply(target)).willReturn(enhancedTarget);
        val attrs = new FileAttribute[]{};
        //when
        provider.createSymbolicLink(rawPath, target, attrs);
        //then
        then(delegatedProvider).should()
                               .createSymbolicLink(enhancedPath, enhancedTarget, attrs);
    }

    @Test
    public void createLink() throws Exception {
        //given
        val existing = mock(S3Path.class);
        val enhancedExisting = mock(S3Path.class);
        given(s3PathEnhancer.apply(existing)).willReturn(enhancedExisting);
        //when
        provider.createLink(rawPath, existing);
        //then
        then(delegatedProvider).should()
                               .createLink(enhancedPath, enhancedExisting);
    }

    @Test
    public void deleteIfExists() throws Exception {
        //given
        given(delegatedProvider.deleteIfExists(enhancedPath)).willReturn(true)
                                                             .willReturn(false);
        //when
        val result1 = provider.deleteIfExists(rawPath);
        val result2 = provider.deleteIfExists(rawPath);
        //then
        assertThat(result1).isTrue();
        assertThat(result2).isFalse();
    }

    @Test
    public void readSymbolicLink() throws Exception {
        //given
        val expected = mock(Path.class);
        given(delegatedProvider.readSymbolicLink(enhancedPath)).willReturn(expected);
        //when
        val result = provider.readSymbolicLink(rawPath);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void setAttribute() throws Exception {
        //given
        val attribute = "attribute";
        val value = new Object();
        val options = LinkOption.NOFOLLOW_LINKS;
        //when
        provider.setAttribute(rawPath, attribute, value, options);
        //then
        then(delegatedProvider).should()
                               .setAttribute(enhancedPath, attribute, value, options);
    }

    //    @Test
    //    public void getSchema() throws Exception {
    //        //given
    //        val expected = "schema";
    //        given(delegatedProvider.getScheme()).willReturn(expected);
    //        //when
    //        val result = provider.getScheme();
    //        //then
    //        assertThat(result).isSameAs(expected);
    //    }

    @Test
    public void newFileSystem() throws Exception {
        //given
        val path = mock(S3Path.class);
        val env = new HashMap<String, String>();
        val expected = mock(FileSystem.class);
        given(delegatedProvider.newFileSystem(path, env)).willReturn(expected);
        given(s3PathEnhancer.apply(path)).willReturn(path);
        //when
        val result = provider.newFileSystem(path, env);
        //then
        assertThat(result).isSameAs(expected);
    }

    //    @Test
    //    public void getFileSystemWithURIOnly() throws Exception {
    //        //given
    //        val uri = URI.create("http://localhost");
    //        val expected = mock(S3FileSystem.class);
    //        given(delegatedProvider.getFileSystem(uri)).willReturn(expected);
    //        //when
    //        val result = provider.getFileSystem(uri);
    //        //then
    //        assertThat(result).isSameAs(expected);
    //    }

    @Test
    public void getFileSystemWithURIAndMap() throws Exception {
        //given
        val uri = URI.create("http://localhost");
        val env = new HashMap<String, String>();
        val expected = mock(S3FileSystem.class);
        given(delegatedProvider.getFileSystem(uri, env)).willReturn(expected);
        //when
        val result = provider.getFileSystem(uri, env);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void getPath() throws Exception {
        //given
        val uri = URI.create("http://localhost");
        val expected = mock(Path.class);
        given(delegatedProvider.getPath(uri)).willReturn(expected);
        //when
        val result = provider.getPath(uri);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void getFileStore() throws Exception {
        //given
        val expected = mock(FileStore.class);
        given(delegatedProvider.getFileStore(enhancedPath)).willReturn(expected);
        //when
        val result = provider.getFileStore(rawPath);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void getFileAttributeView() throws Exception {
        //given
        val type = FileAttributeView.class;
        val options = LinkOption.NOFOLLOW_LINKS;
        val expected = mock(FileAttributeView.class);
        given(delegatedProvider.getFileAttributeView(enhancedPath, type, options)).willReturn(expected);
        //when
        val result = provider.getFileAttributeView(rawPath, type, options);
        //then
        assertThat(result).isSameAs(expected);
    }

    @Test
    public void getFileSystemMap() throws Exception {
        //given
        val uri = URI.create("http://localhost");
        val props = new HashMap<String, String>();
        given(delegatedProvider.getFileSystem(uri, props)).willReturn(fileSystem);
        //when
        val result = provider.getFileSystem(uri, props);
        //then
        assertThat(result).isSameAs(fileSystem);
    }

    @Test
    public void getFileSystemKeyProps() throws Exception {
        //given
        val uri = URI.create("http://localhost");
        val props = new Properties();
        val key = "key";
        given(delegatedProvider.getFileSystemKey(uri, props)).willReturn(key);
        //when
        val result = provider.getFileSystemKey(uri, props);
        //then
        assertThat(result).isSameAs(key);
    }

    //    @Test
    //    public void validateUri() throws Exception {
    //        //given
    //        val uri = URI.create("http://localhost");
    //        //when
    //        provider.validateUri(uri);
    //        //then
    //        then(delegatedProvider).should()
    //                               .validateUri(uri);
    //    }

    @Test
    public void overloadProperties() throws Exception {
        //given
        val props = new Properties();
        val env = new HashMap<String, String>();
        //when
        provider.overloadProperties(props, env);
        //then
        then(delegatedProvider).should()
                               .overloadProperties(props, env);
    }

    @Test
    public void overloadPropertiesWithEnv() throws Exception {
        val props = new Properties();
        val env = new HashMap<String, String>();
        val key = "key";
        //given - false
        given(delegatedProvider.overloadPropertiesWithEnv(props, env, key)).willReturn(false);
        //when
        val resultFalse = provider.overloadPropertiesWithEnv(props, env, key);
        //then
        assertThat(resultFalse).isFalse();
        //given - true
        given(delegatedProvider.overloadPropertiesWithEnv(props, env, key)).willReturn(true);
        //when
        val resultTrue = provider.overloadPropertiesWithEnv(props, env, key);
        //then
        assertThat(resultTrue).isTrue();
    }

    //    @Test
    //    public void overloadPropertiesWithSystemProps() throws Exception {
    //        val props = new Properties();
    //        val key = "key";
    //        //given - false
    //        given(delegatedProvider.overloadPropertiesWithSystemProps(props, key)).willReturn(false);
    //        //when
    //        val resultFalse = provider.overloadPropertiesWithSystemProps(props, key);
    //        //then
    //        assertThat(resultFalse).isFalse();
    //        //given - true
    //        given(delegatedProvider.overloadPropertiesWithSystemProps(props, key)).willReturn(true);
    //        //when
    //        val resultTrue = provider.overloadPropertiesWithSystemProps(props, key);
    //        //then
    //        assertThat(resultTrue).isTrue();
    //    }

    //    @Test
    //    public void overloadPropertiesWithSystemEnv() throws Exception {
    //        val props = new Properties();
    //        val key = "key";
    //        //given - false
    //        given(delegatedProvider.overloadPropertiesWithSystemEnv(props, key)).willReturn(false);
    //        //when
    //        val resultFalse = provider.overloadPropertiesWithSystemEnv(props, key);
    //        //then
    //        assertThat(resultFalse).isFalse();
    //        //given - true
    //        given(delegatedProvider.overloadPropertiesWithSystemEnv(props, key)).willReturn(true);
    //        //when
    //        val resultTrue = provider.overloadPropertiesWithSystemEnv(props, key);
    //        //then
    //        assertThat(resultTrue).isTrue();
    //    }

    //    @Test
    //    public void systemGetEnd() throws Exception {
    //        //given
    //        val key = "key";
    //        val value = "value";
    //        given(delegatedProvider.systemGetEnv(key)).willReturn(value);
    //        //when
    //        val result = provider.systemGetEnv(key);
    //        //then
    //        assertThat(result).isSameAs(value);
    //    }

    @Test
    public void getAmazonS3() throws Exception {
        //given
        val uri = URI.create("http://localhost");
        val props = new Properties();
        val amazonS3 = mock(AmazonS3.class);
        given(delegatedProvider.getAmazonS3(uri, props)).willReturn(amazonS3);
        //when
        val result = provider.getAmazonS3(uri, props);
        //then
        assertThat(result).isSameAs(amazonS3);
    }

    //    @Test
    //    public void getAmazonS3Factory() throws Exception {
    //        //given
    //        val props = new Properties();
    //        val amazonS3Factory = mock(AmazonS3Factory.class);
    //        given(delegatedProvider.getAmazonS3Factory(props)).willReturn(amazonS3Factory);
    //        //when
    //        val result = provider.getAmazonS3Factory(props);
    //        //then
    //        assertThat(result).isSameAs(amazonS3Factory);
    //    }

    //    @Test
    //    public void loadAmazonProperties() throws Exception {
    //        //given
    //        val properties = new Properties();
    //        given(delegatedProvider.loadAmazonProperties()).willReturn(properties);
    //        //when
    //        val result = provider.loadAmazonProperties();
    //        //then
    //        assertThat(result).isSameAs(properties);
    //    }

    //    @Test
    //    public void close() throws Exception {
    //        //given
    //        val s3FileSystem = mock(S3FileSystem.class);
    //        //when
    //        provider.close(s3FileSystem);
    //        //then
    //        then(delegatedProvider).should()
    //                               .close(s3FileSystem);
    //    }

    //    @Test
    //    public void isOpen() throws Exception {
    //        val s3FileSystem = mock(S3FileSystem.class);
    //        //given - false
    //        given(delegatedProvider.isOpen(s3FileSystem)).willReturn(false);
    //        //when
    //        val resultFalse = provider.isOpen(s3FileSystem);
    //        //then
    //        assertThat(resultFalse).isFalse();
    //        //given - true
    //        given(delegatedProvider.isOpen(s3FileSystem)).willReturn(true);
    //        //when
    //        val resultTrue = provider.isOpen(s3FileSystem);
    //        //then
    //        assertThat(resultTrue).isTrue();
    //    }

    //    @Test
    //    public void getCache() throws Exception {
    //        //given
    //        val cache = mock(Cache.class);
    //        given(delegatedProvider.getCache()).willReturn(cache);
    //        //when
    //        val result = provider.getCache();
    //        //then
    //        assertThat(result).isSameAs(cache);
    //    }

    //    @Test
    //    public void setCache() throws Exception {
    //        //given
    //        val cache = mock(Cache.class);
    //        //when
    //        provider.setCache(cache);
    //        //then
    //        then(delegatedProvider).should()
    //                               .setCache(cache);
    //    }
}
