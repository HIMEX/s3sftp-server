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

package com.hubio.s3sftp.example;

import com.hubio.s3sftp.server.S3SftpServer;
import com.hubio.s3sftp.server.S3SftpServerConfiguration;
import com.hubio.s3sftp.server.SessionHome;
import com.hubio.s3sftp.server.SessionJail;
import com.hubio.s3sftp.server.SftpSession;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * Application Configuration.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Configuration
class AppConfiguration {

    /**
     * Create an S3 SFTP server.
     *
     * @param configuration The Server configuration
     *
     * @return The server
     */
    @Bean
    S3SftpServer s3SftpServer(final S3SftpServerConfiguration configuration) {
        return S3SftpServer.using(configuration);
    }

    /**
     * Create an S3 SFTP server configuration.
     *
     * @param sftpProperties The sftp properties
     * @param s3Properties   The s3 properties
     * @param sessionHome    The Session Home mapper
     * @param sessionJail    The Session Jail mapper
     *
     * @return The configuration
     */
    @Bean
    S3SftpServerConfiguration s3SftpServerConfiguration(
            final SftpProperties sftpProperties, final S3Properties s3Properties, final SessionHome sessionHome,
            final SessionJail sessionJail
                                                       ) {
        val bucket = s3Properties.getBucket();
        val uri = s3Properties.getUri();
        val port = sftpProperties.getPort();
        val hostkeyAlgorithm = sftpProperties.getHostkeyAlgorithm();
        val hostkeyPrivate = sftpProperties.getHostkeyPrivate();
        val users = sftpProperties.getUsers();
        return S3SftpServerConfiguration.builder()
                                        .port(port)
                                        .uri(uri)
                                        .sessionBucket(S3SftpServer.simpleSessionBucket(bucket))
                                        .sessionHome(sessionHome)
                                        .sessionJail(sessionJail)
                                        .hostKeyAlgorithm(hostkeyAlgorithm)
                                        .hostKeyPrivate(hostkeyPrivate)
                                        .authenticationProvider(S3SftpServer.simpleAuthenticator(users))
                                        .build();
    }

    /**
     * Unjailed session.
     *
     * @return unjailed path
     */
    @Bean
    @Profile({"default", "root", "unjailed"})
    SessionJail sessionJailUnjailed() {
        return session -> "";
    }

    /**
     * Jailed session for the environment.
     *
     * @param environment The Spring Environment
     *
     * @return jailed path for the user
     */
    @Bean
    @Profile("jailed")
    SessionJail sessionJailByEnvironment(final Environment environment) {
        return session -> String.format("%s", environment.getActiveProfiles()[0]);
    }


    /**
     * Maps the user home directory to /${username}.
     *
     * @return the session home mapper
     */
    @Bean
    @Profile({"default", "user"})
    SessionHome sessionHomePerUser() {
        return (SftpSession session) -> session.getServerSession()
                                               .getUsername();
    }

    /**
     * Maps the user home directory to /.
     *
     * @param environment The Spring Environment
     *
     * @return the session home mapper
     */
    @Bean
    @Profile("jailed")
    SessionHome sessionHomeJailed(final Environment environment) {
        return session -> {
            val username = session.getServerSession()
                                  .getUsername();
            val firstActiveProfile = environment.getActiveProfiles()[0];
            return String.format("%s/%s", firstActiveProfile, username);
        };
    }

    /**
     * Maps the user home directory to /.
     *
     * @return the session home mapper
     */
    @Bean
    @Profile("root")
    SessionHome sessionHomeRoot() {
        return session -> "";
    }

    /**
     * Maps the user home directory to /subdir/${username}.
     *
     * @return the session home mapper
     */
    @Bean
    @Profile("subdir-user")
    SessionHome sessionHomeSubdirUser() {
        return session -> {
            val username = session.getServerSession()
                                  .getUsername();
            return "subdir" + username;
        };
    }
}
