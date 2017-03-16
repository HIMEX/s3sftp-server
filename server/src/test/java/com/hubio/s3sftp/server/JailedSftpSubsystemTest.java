package com.hubio.s3sftp.server;

import com.hubio.s3sftp.server.filesystem.UserFileSystemResolver;
import com.upplication.s3fs.S3FileSystem;
import de.bechte.junit.runners.context.HierarchicalContextRunner;
import lombok.val;
import org.apache.sshd.common.Factory;
import org.apache.sshd.common.random.Random;
import org.apache.sshd.server.ServerFactoryManager;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.AbstractSftpEventListenerAdapter;
import org.apache.sshd.server.subsystem.sftp.UnsupportedAttributePolicy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;

/**
 * Tests for {@link JailedSftpSubsystem}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@RunWith(HierarchicalContextRunner.class)
public class JailedSftpSubsystemTest {

    private JailedSftpSubsystem sftpSubsystem;

    @Mock
    private ExecutorService executorService;

    @Mock
    private SessionBucket sessionBucket;

    @Mock
    private SessionHome sessionHome;

    @Mock
    private SessionJail sessionJail;

    @Mock
    private UserFileSystemResolver userFileSystemResolver;

    @Mock
    private ServerSession serverSession;

    private SftpSession sftpSession;

    @Mock
    private ServerFactoryManager serverFactoryManager;

    @Mock
    private Factory<Random> randomFactory;

    @Mock
    private Random randomizer;

    @Mock
    private S3FileSystem s3FileSystem;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        val unsupportedAttributePolicy = UnsupportedAttributePolicy.Warn;
        val shutdownOnExit = false;
        sftpSubsystem =
                new JailedSftpSubsystem(executorService, shutdownOnExit, unsupportedAttributePolicy, sessionBucket,
                                        sessionHome, sessionJail, userFileSystemResolver
                );
    }

    @Test
    public void shouldRemoveOnlyPermissionsFromAttributes() throws Exception {
        //given
        val path = Paths.get(".");
        val attributes = new HashMap<String, String>();
        attributes.put("basic", "keep me");
        attributes.put("permissions", "drop me");
        val modifiedAttributes = new HashMap<String, Object>();
        sftpSubsystem.addSftpEventListener(new AbstractSftpEventListenerAdapter() {
            @Override
            public void modifyingAttributes(final ServerSession session, final Path path, final Map<String, ?> attrs)
                    throws IOException {
                modifiedAttributes.putAll(attrs);
            }
        });
        //when
        sftpSubsystem.doSetAttributes(path, attributes);
        //then
        assertThat(modifiedAttributes).containsOnlyKeys("basic")
                                      .containsValues("keep me")
                                      .doesNotContainValue("drop me");
    }

    public class ResolveFile {

        private String username;

        private String bucket;

        @Before
        public void setUp() throws Exception {
            username = "bob";
            bucket = "bucket";
            given(serverSession.getFactoryManager()).willReturn(serverFactoryManager);
            given(serverFactoryManager.getRandomFactory()).willReturn(randomFactory);
            given(randomFactory.create()).willReturn(randomizer);
            given(serverSession.getUsername()).willReturn(username);
            sftpSubsystem.setSession(serverSession);
            sftpSession = SftpSession.of(serverSession);
            given(sessionBucket.getBucket(anyObject())).willReturn(bucket);
            given(sessionJail.getJail(anyObject())).willReturn("");
            given(sessionHome.getHomePath(anyObject())).willReturn("");
            given(userFileSystemResolver.resolve(username)).willReturn(Optional.of(s3FileSystem));
            given(s3FileSystem.getSeparator()).willReturn("/");
        }

        public class Root {

            @Before
            public void setUp() throws Exception {
                given(sessionHome.getHomePath(sftpSession)).willReturn("");
            }

            @Test
            public void resolveFileRoot() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile(".");
                //then
                assertThat(path.toString()).isEqualTo("/bucket/");
            }

            @Test
            public void resolveFileDirectory() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile("/subdir");
                //then
                assertThat(path.toString()).isEqualTo("/bucket/subdir");
            }

            @Test
            public void resolveFileFile() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile("/file.txt");
                //then
                assertThat(path.toString()).isEqualTo("/bucket/file.txt");
            }

            @Test
            public void resolverFilePreresolved() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile("/bucket/file.txt");
                //then
                assertThat(path.toString()).isEqualTo("/bucket/file.txt");
            }

            @Test
            public void resolveFileShouldRemoveTrailingPeriod() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile("dir/.");
                //then
                assertThat(path.toString()).isEqualTo("/bucket/dir");
            }

            @Test
            public void resolveFileShouldResolveParentDir() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile("dir/subdir/..");
                //then
                assertThat(path.toString()).isEqualTo("/bucket/dir/subdir/..");
            }
        }

        public class Home {

            @Before
            public void setUp() throws Exception {
                given(sessionHome.getHomePath(anyObject())).willReturn(username);
            }

            @Test
            public void resolveUserDir() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile("");
                //then
                assertThat(path.toString()).isEqualTo("/bucket/bob");
            }

            @Test
            public void resolveWithinUserDir() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile("file.txt");
                //then
                assertThat(path.toString()).isEqualTo("/bucket/bob/file.txt");
            }

            @Test
            public void resolveFileRoot() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile(".");
                //then
                assertThat(path.toString()).isEqualTo("/bucket/bob");
            }

            @Test
            public void resolveFileDirectory() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile("/subdir");
                //then
                assertThat(path.toString()).isEqualTo("/bucket/bob/subdir");
            }

            @Test
            public void resolveFileFile() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile("/file.txt");
                //then
                assertThat(path.toString()).isEqualTo("/bucket/bob/file.txt");
            }

            @Test
            public void resolverFilePreresolved() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile("/bucket/bob/file.txt");
                //then
                assertThat(path.toString()).isEqualTo("/bucket/bob/file.txt");
            }

            @Test
            public void resolveFileShouldRemoveTrailingPeriod() throws Exception {
                //given
                val path = "dir/.";
                //when
                val result = sftpSubsystem.resolveFile(path);
                //then
                assertThat(result.toString()).isEqualTo("/bucket/bob/dir");
            }

            @Test
            public void resolveFileShouldResolveParentDir() throws Exception {
                //given
                val path = "dir/subdir/..";
                //when
                val result = sftpSubsystem.resolveFile(path);
                //then
                assertThat(result.toString()).isEqualTo("/bucket/bob/dir/subdir/..");
            }
        }

        public class Jailed {

            @Before
            public void setUp() throws Exception {
                given(sessionHome.getHomePath(anyObject())).willReturn(String.format("users/%s", username));
                given(sessionJail.getJail(anyObject())).willReturn("users");
                // i.e. user should only see their own username as the visible path
            }

            @Test
            public void dot() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile(".");
                //then
                assertThat(path.toString()).isEqualTo("/bob/");
            }

            @Test
            public void home() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile("/bob");
                //then
                assertThat(path.toString()).isEqualTo("/bob/");
            }

            @Test
            public void file() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile("/bob/file.txt");
                //then
                assertThat(path.toString()).isEqualTo("/bob/file.txt");
            }

            @Test
            public void fullJailedPath() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile("/bucket/users/bob/file.txt");
                //then
                assertThat(path.toString()).isEqualTo("/bob/file.txt");
            }

            @Test
            public void removeTrailingPeriod() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile("/bob/dir/.");
                //then
                assertThat(path.toString()).isEqualTo("/bob/dir");
            }

            @Test
            public void acceptNavToParentDir() throws Exception {
                //when
                val path = sftpSubsystem.resolveFile("/bob/dir/subdir/..");
                //then
                assertThat(path.toString()).isEqualTo("/bob/dir/subdir/..");
            }

            @Test
            public void homeIsOutsideJail() throws Exception {
                //given
                given(sessionJail.getJail(anyObject())).willReturn("jail");
                given(sessionHome.getHomePath(anyObject())).willReturn("home");
                exception.expect(SftpServerJailMappingException.class);
                exception.expectMessage("User directory is outside jailed path: jail: home");
                //when
                sftpSubsystem.resolveFile(".");
            }

            @Test
            public void tryToExitJail() throws Exception {
                //given
                val path = sftpSubsystem.resolveFile("/");
                //then
                assertThat(path.toString()).isEqualTo("/bob/");
            }
        }

        @Test
        public void filesystemForUserIsMissing() throws Exception {
            //given
            given(userFileSystemResolver.resolve(username)).willReturn(Optional.empty());
            exception.expect(RuntimeException.class);
            exception.expectMessage("Error finding filesystem.");
            //when
            sftpSubsystem.resolveFile("path");
        }
    }
}
