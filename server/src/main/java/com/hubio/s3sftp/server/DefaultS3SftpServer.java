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

import com.hubio.s3sftp.server.filesystem.FileSystemProviderFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.UserAuth;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.auth.pubkey.UserAuthPublicKeyFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Default implementation of the {@link S3SftpServer}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DefaultS3SftpServer implements S3SftpServer {

    private final SshServer sshServer = SshServer.setUpDefaultServer();

    private final S3SftpServerConfiguration configuration;

    private static KeyPairProvider keyPairProviderFromString(final String algorithm, final String privateHostKey)
            throws Exception {
        // write private key to temp file
        val privateKey = Files.createTempFile("sftp-private-host", ".key");
        log.info("Writing private host key: {}", privateKey);
        Files.write(privateKey, privateHostKey.getBytes(StandardCharsets.UTF_8));
        return keyPairProviderFromFile(algorithm, privateKey.toFile());
    }

    private static KeyPairProvider keyPairProviderFromFile(final String algorithm, final File privateHostKeyFile)
            throws Exception {
        // load private key into a key pair provider
        return SshServer.setupServerKeys(null, algorithm, 0, Collections.singletonList(privateHostKeyFile.toString()));
    }

    @Override
    public void start() {
        validateSessionMapping();
        val port = configuration.getPort();
        sshServer.setPort(port);
        loadHostKey();
        // sftp subsystem
        val sessionFileSystemResolver = FileSystemProviderFactory.userResolver();
        val sessionBucket = configuration.getSessionBucket();
        val sessionHome = configuration.getSessionHome();
        val sessionJail = configuration.getSessionJail();
        val sftpSubsystemFactory =
                new JailedSftpSubsystemFactory(sessionBucket, sessionHome, sessionJail, sessionFileSystemResolver);
        sshServer.setSubsystemFactories(Collections.singletonList(sftpSubsystemFactory));
        // file system
        val fileSystemFactory =
                new S3FileSystemFactory(sessionBucket, sessionHome, sessionJail, URI.create(configuration.getUri()),
                                        FileSystemProviderFactory.s3SftpProviderFactory(),
                                        sessionFileSystemResolver
                );
        sshServer.setFileSystemFactory(fileSystemFactory);
        configureAuthentication(sftpSubsystemFactory, fileSystemFactory);
        try {
            sshServer.start();
        } catch (IOException e) {
            throw new S3SftpServerStartException("Could not start server", e);
        }
        log.info("S3 SFTP Server started on port {}", port);
    }

    /**
     * Checks the sessionBucket + sessionHome are compatible with sessionJail.
     *
     * <p>i.e. is sessionJail is specified, then it must be fully matched for it's own length by sessionBucket and
     * sessionHome.</p>
     */
    private void validateSessionMapping() {

    }

    private void configureAuthentication(
            final JailedSftpSubsystemFactory sftpSubsystemFactory, final S3FileSystemFactory fileSystemFactory
                                        ) {
        val userAuthFactories = new ArrayList<NamedFactory<UserAuth>>();
        val authenticationProvider = configuration.getAuthenticationProvider();
        authenticationProvider.setHomeDirExistsChecker(
                new DefaultHomeDirExistsChecker(sftpSubsystemFactory, fileSystemFactory));
        if (authenticationProvider instanceof PasswordAuthenticator) {
            sshServer.setPasswordAuthenticator((PasswordAuthenticator) authenticationProvider);
            userAuthFactories.add(UserAuthPasswordFactory.INSTANCE);
        }
        if (authenticationProvider instanceof PublickeyAuthenticator) {
            sshServer.setPublickeyAuthenticator((PublickeyAuthenticator) authenticationProvider);
            userAuthFactories.add(UserAuthPublicKeyFactory.INSTANCE);
        }
        sshServer.setUserAuthFactories(userAuthFactories);
    }

    @SuppressWarnings("illegalcatch")
    private void loadHostKey() {
        try {
            val hostKeyPrivateFile = configuration.getHostKeyPrivateFile();
            val hostKeyAlgorithm = configuration.getHostKeyAlgorithm();
            val hostKeyPrivate = configuration.getHostKeyPrivate();
            if (hostKeyPrivateFile != null) {
                sshServer.setKeyPairProvider(keyPairProviderFromFile(hostKeyAlgorithm, hostKeyPrivateFile));
            } else if (hostKeyPrivate != null && !hostKeyPrivate.isEmpty()) {
                sshServer.setKeyPairProvider(keyPairProviderFromString(hostKeyAlgorithm, hostKeyPrivate));
            } else {
                throw new IllegalStateException("Missing hostKey. Specify either hostKeyPrivateFile or hostKeyPrivate");
            }
        } catch (Exception e) {
            throw new S3SftpServerStartException("Could not load host key", e);
        }
    }

    @Override
    public void stop() {
        log.info("Stopping S3 SFTP Server");
        try {
            sshServer.stop();
        } catch (IOException e) {
            throw new S3SftpServerStopException("Could not stop server", e);
        }
        log.info("S3 SFTP Server stopped");
    }
}
