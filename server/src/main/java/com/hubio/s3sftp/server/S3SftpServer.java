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

import lombok.NonNull;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;

import java.util.Map;

/**
 * S3 SFTP Server.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public interface S3SftpServer {

    /**
     * The name of the property in the {@link S3FileSystemFactory} containing the username.
     */
    String USERNAME = "com.hubio.s3sftp.username";

    /**
     * The name of the property in the {@link S3FileSystemFactory} containing the bucket name.
     */
    String BUCKET = "com.hubio.s3sftp.bucket";

    /**
     * The name of the property in the {@link S3FileSystemFactory} containing the user's home directory within the
     * bucket.
     */
    String HOMEDIR = "com.hubio.s3sftp.homedir";

    /**
     * The name of the property in the {@link S3FileSystemFactory} containing the subdirectory to jail the file system
     * in.
     */
    String JAIL = "com.hubio.s3sftp.jail";

    /**
     * Creates a new {@link S3SftpServer} with the supplied configuration.
     *
     * @param configuration The configuration
     *
     * @return The new Server
     */
    static S3SftpServer using(final S3SftpServerConfiguration configuration) {
        return new DefaultS3SftpServer(configuration);
    }

    /**
     * Start the server.
     */
    void start();

    /**
     * Stop the server.
     */
    void stop();

    /**
     * Creates a simple password authenticator using a list of usernames and password.
     *
     * @param users the usernames and password to authenticate against
     *
     * @return the authentication provider
     */
    static AuthenticationProvider simpleAuthenticator(@NonNull final Map<String, String> users) {
        final SimpleAuthenticator authenticator = new SimpleAuthenticator();
        users.forEach(authenticator::addUser);
        return authenticator;
    }

    /**
     * Creates a public key authenticator.
     *
     * @param authenticator The Publickey Authenticator to use.
     *
     * @return the authenticator provider
     */
    static AuthenticationProvider publicKeyAuthenticator(final PublickeyAuthenticator authenticator) {
        return new PublicKeyAuthenticationProvider(authenticator);
    }

    /**
     * Creates a simple session bucket mapper that only maps to a single bucket.
     *
     * @param bucket The bucket to map to
     *
     * @return The session bucket mapper
     */
    static SessionBucket simpleSessionBucket(@NonNull final String bucket) {
        return session -> bucket;
    }

    /**
     * Creates a simgple session home mapper that maps each user into a directory within the subdir.
     *
     * @param subdir The directory to contain the user home directories
     *
     * @return The session home mapper
     */
    static SessionHome perUserHome(@NonNull final String subdir) {
        return session -> subdir + "/" + session.getServerSession().getUsername();
    }
}
