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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.google.common.collect.ImmutableList;
import com.upplication.s3fs.S3FileStore;
import com.upplication.s3fs.S3FileSystem;
import com.upplication.s3fs.S3FileSystemProvider;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.FileStore;

/**
 * An {@link S3FileSystem} that limits the directories shown by user.
 *
 * @author Ross W. Drew (ross.drew@hubio.com)
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@Slf4j
class FilteredS3FileSystem extends S3FileSystem {

    private final AmazonS3 client;

    private final String bucketName;

    /**
     * Constructor.
     *
     * @param provider   The Amazon S3 Filesystem provider
     * @param key        The 'key'
     * @param client     The Amazon S3 client
     * @param endpoint   The Amazon S3 web endpoint
     * @param bucketName The name of the Bucket
     */
    FilteredS3FileSystem(
            final S3FileSystemProvider provider, final String key, final AmazonS3 client, final String endpoint,
            final String bucketName
                        ) {
        super(provider, key, client, endpoint);
        log.trace("constructor({}, {}, {}, {}, {}", provider, key, client, endpoint, bucketName);
        this.client = client;
        this.bucketName = bucketName;
    }

    /**
     * Only include an {@link S3FileStore} is the list of buckets contains the user's home directory, otherwise returns
     * an empty {@link Iterable}.
     *
     * <p>Overridden so that user can only view certain directories, this would otherwise return a full list of buckets.
     * MINA would then select the first of these, which there is likely no permission for, causing an error.</p>
     */
    @Override
    public Iterable<FileStore> getFileStores() {
        log.debug("Checking for access permissions to {}", bucketName);
        final ImmutableList.Builder<FileStore> builder = ImmutableList.builder();
        if (client.listBuckets()
                  .stream()
                  .anyMatch(this::isThisBucket)) {
            log.debug("Granting access to {}", bucketName);
            builder.add(new S3FileStore(this, bucketName));
        } else {
            log.debug("Access not permitted to {}", bucketName);
        }
        return builder.build();
    }

    //    @Override
    //    public Set<String> supportedFileAttributeViews() {
    //        log.trace("supportedFileAttributeViews()");
    //        final Set<String> views = super.supportedFileAttributeViews();
    //        log.trace(" - inherited: {}", views);
    //        val supportedViews = ImmutableSet.copyOf(Stream.concat(views.stream(), Stream.of("posix"))
    //                                                       .collect(Collectors.toSet()));
    //        log.trace(" <= supportedViews: {}", supportedViews);
    //        return supportedViews;
    //    }

    private boolean isThisBucket(final Bucket bucket) {
        return bucket.getName()
                     .equalsIgnoreCase(bucketName);
    }
}
