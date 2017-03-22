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

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

/**
 * Authenticate the username and password against a list of known users and passwords.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Slf4j
public class SimpleAuthenticator extends AbstractAuthenticationProvider implements PasswordAuthenticationProvider {

    private final Map<String, String> permitted = new HashMap<>();

    /**
     * Authenticate the username and password for the session.
     *
     * <p>This implementation uses a map of usernames and unencrypted passwords to authenticate the user. As such this
     * implementation should not be directly used in production.</p>
     *
     * @param username The username
     * @param password The password
     * @param session  The session (ignored in this implementation)
     *
     * @return true if the username and password are valid
     */
    @Override
    public boolean authenticatePassword(final String username, final String password, final SftpSession session) {
        val userExists = permitted.containsKey(username);
        val isValidPassword = userExists && permitted.get(username)
                                                     .equals(password);
        if (userExists) {
            if (isValidPassword) {
                log.info("user: '{}', password: okay", username);
            } else {
                log.warn("user: '{}', password: invalid", username);
            }
        } else {
            log.warn("user: '{}': unknown", username);
        }
        return isValidPassword;
    }

    /**
     * Add a username and password as a valid account.
     *
     * @param username The username
     * @param password The password
     */
    final void addUser(final String username, final String password) {
        permitted.put(username, password);
    }
}
