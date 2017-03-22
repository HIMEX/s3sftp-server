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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.sshd.server.session.ServerSession;

import java.net.SocketAddress;

/**
 * Wrapper for the {@link ServerSession}.
 *
 * <p>This wrapper allows client code to provide an implementation of {@link PasswordAuthenticationProvider} without
 * needing to include the apache sshd dependencies required to access the {@link ServerSession} if they do no need to
 * use it in their implementation.</p>
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SftpSession {

    @Getter
    private final ServerSession serverSession;

    /**
     * Create a new wrapper for the ServerSession.
     *
     * @param serverSession The Apache SSHD server session
     *
     * @return the wrapped server session
     */
    public static SftpSession of(final ServerSession serverSession) {
        return new SftpSession(serverSession);
    }

    /**
     * Get the client's SocketAddress.
     *
     * @return the socket address of the client
     */
    public SocketAddress getClientAddress() {
        return serverSession.getClientAddress();
    }

    /**
     * Get the session's username.
     *
     * @return the username for the session
     */
    public String getUsername() {
        return serverSession.getUsername();
    }
}
