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

/**
 * Directory path from the session directory that should be hidden from the user.
 *
 * <p>e.g. if a session is maps to {@code /bucket/users/user} (using {@link SessionBucket} and {@link SessionHome}) and
 * users should only see the {@code /user} part of the path, then {@code SessionJail} should map the session to {@code
 * /bucket/users}.</p>
 *
 * <p>It is an error if the mapped {@code SessionJail} doesn't match the combination of {@link SessionBucket} and {@link
 * SessionHome}.</p>
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@FunctionalInterface
public interface SessionJail {

    /**
     * Returns the directory path that should be hidden from the user.
     *
     * @param session the Session.
     *
     * @return the session's jailed path
     */
    String getJail(SftpSession session);
}
