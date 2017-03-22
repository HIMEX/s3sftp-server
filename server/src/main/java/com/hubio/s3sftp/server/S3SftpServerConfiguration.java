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

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.File;

/**
 * Configuration for {@link S3SftpServer}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Getter
@Builder
public class S3SftpServerConfiguration {

    private final int port;

    @NonNull
    private final String hostKeyAlgorithm;

    private final String hostKeyPrivate;

    private final File hostKeyPrivateFile;

    @NonNull
    private final AuthenticationProvider authenticationProvider;

    @NonNull
    private final SessionBucket sessionBucket;

    @NonNull
    private final SessionHome sessionHome;

    private final SessionJail sessionJail;

    @NonNull
    private final String uri;

    /**
     * Builder for {@link S3SftpServerConfiguration}.
     *
     * <p>Default values:</p>
     * <ul>
     *     <li>port: 22</li>
     *     <li>hostKeyAlgorithm: RSA</li>
     *     <li>sessionHome: "" (i.e. the root of the bucket)</li>
     *     <li>sessionJail: "" (i.e. unjailed)</li>
     * </ul>
     */
    // Default configuration values
    //lombok @Builder will expand upon this class, using the default values provided
    public static class S3SftpServerConfigurationBuilder {

        private static final int DEFAULT_PORT = 22;

        private int port = DEFAULT_PORT;

        private String hostKeyAlgorithm = "RSA";

        private SessionHome sessionHome = session -> "";

        private SessionJail sessionJail = session -> "";

    }
}
