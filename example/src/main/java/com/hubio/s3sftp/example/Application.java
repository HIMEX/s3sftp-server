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

import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Example usage of the s3sftp-server.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
@SpringBootApplication
@SuppressWarnings("hideutilityclassconstructor")
public class Application {

    private static final long ONE_SECOND_IN_MILLIS = 1_000L;

    /**
     * Start the application.
     *
     * @param args Command line arguments.
     *
     * @throws InterruptedException if the sleeping thread is interrupted
     */
    public static void main(final String[] args) throws InterruptedException {
        val context = SpringApplication.run(Application.class, args);
        // don't allow context to go out-of-scope to keep application running
        while (context.isRunning()) {
            // check context once a second
            Thread.sleep(ONE_SECOND_IN_MILLIS);
        }
    }
}
