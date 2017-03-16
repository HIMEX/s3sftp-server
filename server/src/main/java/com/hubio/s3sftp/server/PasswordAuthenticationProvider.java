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

import lombok.val;
import org.apache.sshd.server.session.ServerSession;

import java.util.Objects;

/**
 * Authentication Provider for username/passwords.
 *
 * <p>Includes a check that users with valid passwords, that they also have a home directory. If they do not, then they
 * will not be authenticated.</p>
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public interface PasswordAuthenticationProvider extends AuthenticationProvider {

    /**
     * Authenticate the username and password for the session.
     *
     * @param username The username
     * @param password The password
     * @param session  The session
     *
     * @return true if the username and password are valid
     */
    boolean authenticatePassword(String username, String password, SftpSession session);

    /**
     * Perform authentication for the user.
     *
     * <p>This default implementation also requires that the user's hoe directory exists.</p>
     *
     * <p>To implement the authentication of the username, password and session override the
     * {@link #authenticatePassword(String, String, SftpSession)} method.</p>
     *
     * @param username The username
     * @param password The password
     * @param session  The session
     *
     * @return true if the username and password are valid and the user's home directory exists
     */
    default boolean authenticate(final String username, final String password, final ServerSession session) {
        val homeDirExistsChecker = getHomeDirExistsChecker();
        Objects.requireNonNull(homeDirExistsChecker, "No HomeDirExistsChecker set");
        val isUserAuthenticated = authenticatePassword(username, password, SftpSession.of(session));
        val isUserDirectoryFound = homeDirExistsChecker.check(username, session);
        return isUserAuthenticated && isUserDirectoryFound;
    }
}
