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

package com.hubio.s3sftp.server;

import com.hubio.s3sftp.server.filesystem.UserFileSystemResolver;
import com.upplication.s3fs.S3FileSystem;
import com.upplication.s3fs.S3Path;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.sshd.common.util.SelectorUtils;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystem;
import org.apache.sshd.server.subsystem.sftp.UnsupportedAttributePolicy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * {@link SftpSubsystem} where the user is jailed within a subdirectory specified by a {@link SessionHome}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Slf4j
class JailedSftpSubsystem extends SftpSubsystem {

    private static final String SEPARATOR = "/";

    private static final String DOT = ".";

    private final SessionBucket sessionBucket;

    private final SessionHome sessionHome;

    private final SessionJail sessionJail;

    private final UserFileSystemResolver resolver;

    /**
     * Constructor.
     *
     * @param executorService            The Executor Service
     * @param shutdownOnExit             Controls stopping the executor service when server exits
     * @param unsupportedAttributePolicy The policy for unsupported attributes
     * @param sessionBucket              The session bucket mapper
     * @param sessionHome                The session home path mapper
     * @param sessionJail                The session jail mapper
     * @param userFileSystemResolver     The session Filesystem resolver
     */
    JailedSftpSubsystem(
            final ExecutorService executorService, final boolean shutdownOnExit,
            final UnsupportedAttributePolicy unsupportedAttributePolicy, final SessionBucket sessionBucket,
            final SessionHome sessionHome, final SessionJail sessionJail,
            final UserFileSystemResolver userFileSystemResolver
                       ) {
        super(executorService, shutdownOnExit, unsupportedAttributePolicy);
        this.sessionBucket = sessionBucket;
        this.sessionHome = sessionHome;
        this.sessionJail = sessionJail;
        this.resolver = userFileSystemResolver;
    }

    // variables with 'Key' suffix are paths based in the root of the bucket

    /**
     * Resolve the {@code remotePath} for the session.
     *
     * <p>This implementation uses the {@code sessionBucket} and the {@code sessionHome} to map the {@code remotePath}
     * to the user's home directory.</p>
     *
     * <p>If {@code sessionJail} is used then the {@code sessionHome} <strong>must</strong> {@code .startsWith()} the
     * {@code sessionJail}. The {@code sessionJail} will then be removed.</p>
     *
     * @param remotePath The path of the remote file
     *
     * @return The resolved file
     */
    @Override
    protected Path resolveFile(final String remotePath) {
        log.debug("resolveFile({})", remotePath);
        val serverSession = SftpSession.of(getServerSession());
        val bucketName = sessionBucket.getBucket(serverSession);
        val homeKey = sessionHome.getHomePath(serverSession);
        val homeDir = SEPARATOR + bucketName + SEPARATOR + homeKey;
        val jailKey = sessionJail.getJail(serverSession);
        val unJailedPath = getUnJailedPath(remotePath, bucketName, homeKey, jailKey, homeDir);
        if (jailKey.isEmpty()) {
            log.trace(" <= resolved - unjailed: {}", unJailedPath);
            return unJailedPath;
        }
        val jailDir = SEPARATOR + bucketName + SEPARATOR + jailKey;
        val jailedPath = getJailedPath(unJailedPath, jailDir);
        log.trace(" <= resolved - jailed: {}", jailedPath);
        return jailedPath;
    }

    private Path getJailedPath(final S3Path unjailedPath, final String jailDir) {
        val fileSystem = unjailedPath.getFileSystem();
        val subJailKey = unjailedPath.toString()
                                     .substring(jailDir.length());
        return new S3Path(fileSystem, subJailKey);
    }

    private S3Path getUnJailedPath(
            final String remotePath, final String bucketName, final String homeKey, final String jailKey,
            final String homeDir
                                  ) {
        val jailDir = getJailDir(bucketName, homeKey, homeDir, jailKey);
        val userPath = getUserPath(remotePath, homeDir, jailDir);
        val sourcePath = SelectorUtils.translateToLocalFileSystemPath(userPath, '/', fileSystem);
        val cleanPath = cleanPath(sourcePath, bucketName);
        val resolvedPath = defaultDir.resolve(cleanPath);
        return new S3Path(getSessionFileSystem(), resolvedPath.toString());
    }

    private S3FileSystem getSessionFileSystem() {
        return resolver.resolve(getServerSession().getUsername())
                       .orElseThrow(() -> new RuntimeException("Error finding filesystem."));
    }

    private String getJailDir(final String bucket, final String home, final String homeDir, final String jail) {
        String jailDir;
        if (jail.isEmpty()) {
            jailDir = "";
        } else {
            jailDir = SEPARATOR + bucket + SEPARATOR + jail;
            if (!homeDir.startsWith(jailDir)) {
                log.trace("Session Home should be within the Session Jail");
                throw new SftpServerJailMappingException(jail, home);
            }
        }
        return jailDir;
    }

    private String getUserPath(final String remotePath, final String homeDir, final String jailDir) {
        String userPath = remotePath;
        if (!userPath.startsWith(homeDir)) {
            userPath = qualifyUserPath(homeDir, jailDir, userPath);
        }
        if (userPath.length() < homeDir.length()) {
            userPath = homeDir;
        }
        val userPathParts = userPath.split(SEPARATOR);
        return resolvePathWithinBucket(
                homeDir, Arrays.copyOfRange(userPathParts, homeDir.split(SEPARATOR).length, userPathParts.length));
    }

    private String qualifyUserPath(final String homeDir, final String jailDir, final String userPath) {
        String result;
        if (DOT.equals(userPath)) {
            result = homeDir;
        } else {
            if (jailDir.isEmpty()) {
                result = homeDir + SEPARATOR + userPath;
            } else {
                result = jailDir + userPath;
            }
        }
        return result;
    }

    private String cleanPath(final String sourcePath, final String bucket) {
        String path = sourcePath;
        if (path.endsWith(SEPARATOR + DOT)) {
            log.trace(" - removing trailing character: {}", path);
            path = path.substring(0, path.length() - 1);
        }
        path = path.replace(bucket + SEPARATOR + bucket, bucket);
        return path;
    }


    private String resolvePathWithinBucket(final String homeDir, final String[] remainder) {
        if (remainder.length > 0) {
            return homeDir + SEPARATOR + String.join(SEPARATOR, Arrays.asList(remainder));
        }
        return homeDir;
    }

    @Override
    protected void doSetAttributes(final Path file, final Map<String, ?> attributes) throws IOException {
        attributes.remove("permissions");
        super.doSetAttributes(file, attributes);
    }
}
