package com.hubio.s3sftp.server;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.services.s3.AmazonS3;
import com.upplication.s3fs.AmazonS3Factory;
import lombok.Setter;

/**
 * Test wrapper for the Amazon S3 Factory.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class TestAmazonS3Factory extends AmazonS3Factory {

    @Setter
    private AmazonS3 amazonS3;

    @Override
    protected AmazonS3 createAmazonS3(
            final AWSCredentialsProvider credentialsProvider, final ClientConfiguration clientConfiguration,
            final RequestMetricCollector requestMetricsCollector
                                     ) {
        return amazonS3;
    }
}
