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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.sshd.common.util.io.IoUtils;
import org.apache.sshd.server.session.ServerSession;

import java.io.IOException;

/**
 * Base Authentication Provider, includes check for existence of home directory.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Slf4j
@RequiredArgsConstructor
class DefaultHomeDirExistsChecker implements HomeDirExistsChecker {

    private final JailedSftpSubsystemFactory jailedSftpSubsystemFactory;

    private final S3FileSystemFactory s3FileSystemFactory;

    @Override
    @SuppressWarnings("illegalcatch")
    public boolean check(final String username, final ServerSession session) {
        log.trace("check({}, {})", username, session);
        val sftpSubsystem = (JailedSftpSubsystem) jailedSftpSubsystemFactory.create();
        session.setUsername(username);
        sftpSubsystem.setSession(session);
        String message = "Creating S3 file system";
        try {
            val fileSystem = s3FileSystemFactory.createFileSystem(session);
            sftpSubsystem.setFileSystem(fileSystem);
            message = "Checking home directory exists";
            val path = sftpSubsystem.resolveFile(".");
            val exists = IoUtils.checkFileExists(path);
            if (exists) {
                log.info("user: '{}', homeDir:'{}': found", username, path);
            } else {
                log.warn("user: '{}', homeDir:'{}': does not exist", username, path);
            }
            return exists;
        } catch (IOException e) {
            log.warn(message, e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
