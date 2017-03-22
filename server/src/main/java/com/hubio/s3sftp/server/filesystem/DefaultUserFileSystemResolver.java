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

package com.hubio.s3sftp.server.filesystem;

import com.upplication.s3fs.S3FileSystem;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Default implementation of {@link UserFileSystemResolver}.
 *
 * <p>Stores a {@link WeakReference} to the filesystem.</p>
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Slf4j
class DefaultUserFileSystemResolver implements UserFileSystemResolver {

    private Map<String, WeakReference<S3FileSystem>> map = new HashMap<>();

    @Override
    public Optional<S3FileSystem> resolve(final String username) {
        log.debug("resolve({})", username);
        val fileSystem = Optional.ofNullable(map.get(username))
                                 .map(WeakReference::get);
        log.trace(" <= filesystem: {}", fileSystem);
        return fileSystem;
    }

    @Override
    public void put(final String username, final S3FileSystem fileSystem) {
        log.trace("put({}, {})", username, fileSystem);
        map.put(username, new WeakReference<>(fileSystem));
    }
}
