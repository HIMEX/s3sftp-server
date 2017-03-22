package com.hubio.s3sftp.server;

import lombok.val;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Tests for {@link DefaultS3SftpServer}.
 *
 * @author Paul Campbell (paul.campbell@hubio.com)
 */
public class DefaultS3SftpServerTest {

    private static final String HOSTKEY =
            "-----BEGIN RSA PRIVATE KEY-----\n" + "MIIEpAIBAAKCAQEAp0oxAoPJXBnkg4S0i0TvrFrnyK4MHP7JrFFk5t0cCixq43wQ\n"
            + "R09VvLh0dZhdUe+uIyLEBYzGdZMqmgFiPRG0oFoQJ/YoRkJ7Y00ajKoCgiVrkPV2\n"
            + "Q7aYacfkayF2jnugwq5bAAspw4jQqVitRimT8yQcahPltgqFlubgWdc1ehOE9eCL\n"
            + "QaHihL/qCteN/Keaj3aFfx9QqbA9RjARFDRhiWYQ+QF8UTI2KJ8ZoMJEEGEQfMlV\n"
            + "HCACCZ2YZpFRm50/3O8iSM2XzhZQOBfQit7HY4lGd5kdDRPdeVKcDkSpgJSp4DuA\n"
            + "hlnVgy9KHELzgcfcKEkt2vuWOkUT0SSFk6IJvQIDAQABAoIBAQCeXF4uqkB4Pk6S\n"
            + "rZIXcGeN+fQGhbQT0qFozRg+bzs26js5I11pk9FuuBIOq/BTOxfWTPfQ5SWNcYXX\n"
            + "ic3MT7F0Ri2bFqujbxXMt9WVKO788p1z+Nk+WmDHaiFxxJitYpyZDmI4lViwsBgO\n"
            + "51IH5B4ZAasgQ7ulaypw4hepFE+cQcuJfZTe/EQgK3raP7T4jYXdAJpsnRkeWdkN\n"
            + "iFqbVnf+8YDPcj6+9OY+Xc2VM5NEfWhKvBTnFVDbBWKYyhSzUO51EFS4s39TPRig\n"
            + "uwta4SfUDYVyXySJrd5cIknUQBJHDvL4ZKlmfJ7dnpjE51+lkZt41T/vAxHDnMVU\n"
            + "IIPp0DvhAoGBANvp6Rqud6cqs75frdJuH/DLOJVUGCfBcPadSvgPHKQUdvfaqjyq\n"
            + "rU/8BWp9GuokQb0qDFtTE7vpD6Wbkns1eFI8uAC/128o9089pqGx4DH6o/t+RoAB\n"
            + "Zy6syU56kIuCeLbTMsrd61/8QlPsI6gTFXVbkscPVxLxzLd9iSXFldnpAoGBAMK9\n"
            + "qrxtxlau95Req0CYm9kT//pa+IuJEz7J/+gYENbbf6XxDpw0opRqgUObwuGDPfgk\n"
            + "B43nMd2zwYBmLw6YZcOD/L4obDQhRX7q+CZvJBkoYrnp4h5xcXOnnUu3xOBmoHL9\n"
            + "Wuab9yZktRJVE/k76WpAbVxGr0ijtSSvpuDqZDi1AoGBAMEcBSr922o60D2y7QNk\n"
            + "2r1q5tQSVWfLsPOOKf/r3T2kDtgU9vpw8eHTr7nUA+dpUSTYIKOtLx4KSUgmdZml\n"
            + "2XN1iCp4S6h8M7csrv88IGAi9Q5p02SiVsYgymEUtYscVf5NNUP5XbAa5u+k46a6\n"
            + "o1Q7xobwTIkBNcBHB0DY4X7JAoGAR7jlDfr8JmbQZkOrnOHX3E5iY4lnqrR0cxag\n"
            + "epGKeidjTvGGKP+1tSW4r/bJApd8lkxmv9ubYQTYSnrX7+8u46BT0JFAsL5kQwc1\n"
            + "F6qtR9q46bH7Bq1PVIIyC3YGO4NwqoknFnHwx6IlkjflYFCxeeF6pZae7gjlKTrM\n"
            + "ImARQ1UCgYBcRfZci4aDS4wuZF8euLfmc6k1ZW6tSk6RN0U/fIH8tOmr3N/yISLx\n"
            + "6y8FbjkT++WtLRDEAJ+/uSTW0gnJNr5xvvXFSkI9AwOv6jA5Oufo4ZNDuUZ92f/8\n"
            + "jfuqbPp3XxkGc0K2KWb8YhL+qD3CpId39FeIM2b8CSqB7p4R7YukrA==\n" + "-----END RSA PRIVATE KEY-----\n";

    private S3SftpServer subject;

    private int port;

    private String hostKeyAlgorithm;

    private String hostKeyPrivate;

    private File hostKeyPrivateFile;

    private AuthenticationProvider authenticationProvider;

    private SessionBucket sessionBucket;

    private SessionHome sessionHome;

    private String uri;

    private Map<String, String> users;

    private String bucket;

    private String home;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private SftpSession sftpSession;

    @Mock
    private ServerSession serverSession;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        hostKeyAlgorithm = "RSA";
        users = new HashMap<>();
        bucket = "bucket";
        sessionBucket = S3SftpServer.simpleSessionBucket(bucket);
        home = "home";
        sessionHome = S3SftpServer.perUserHome(home);
        uri = "uri";
        sftpSession = SftpSession.of(serverSession);
    }

    private S3SftpServer createServer() {
        return S3SftpServer.using(S3SftpServerConfiguration.builder()
                                                           .port(port)
                                                           .hostKeyAlgorithm(hostKeyAlgorithm)
                                                           .hostKeyPrivate(hostKeyPrivate)
                                                           .hostKeyPrivateFile(hostKeyPrivateFile)
                                                           .authenticationProvider(authenticationProvider)
                                                           .sessionBucket(sessionBucket)
                                                           .sessionHome(sessionHome)
                                                           .uri(uri)
                                                           .build());
    }

    private void useFileHostKey() throws IOException {
        hostKeyPrivateFile = folder.newFile();
        Files.write(hostKeyPrivateFile.toPath(), HOSTKEY.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void startAndStopWithHostKeyStringAndPasswordAuth() {
        //given
        hostKeyPrivate = HOSTKEY;
        authenticationProvider = S3SftpServer.simpleAuthenticator(users);
        subject = createServer();
        //when
        subject.start();
        subject.stop();
    }

    @Test
    public void startAndStopWithHostKeyFileAndPasswordAuth() throws IOException {
        //given
        useFileHostKey();
        authenticationProvider = S3SftpServer.simpleAuthenticator(users);
        subject = createServer();
        //when
        subject.start();
        subject.stop();
    }

    @Test
    public void startAndStopWithHostKeyStringAndPublicKeyAuth() {
        //given
        hostKeyPrivate = HOSTKEY;
        authenticationProvider = S3SftpServer.publicKeyAuthenticator(AcceptAllPublickeyAuthenticator.INSTANCE);
        subject = createServer();
        //when
        subject.start();
        subject.stop();
    }

    @Test
    public void shouldErrorWhenNoHostKey() {
        //given
        authenticationProvider = S3SftpServer.simpleAuthenticator(users);
        subject = createServer();
        exception.expect(S3SftpServerStartException.class);
        exception.expectMessage("Could not load host key");
        //when
        subject.start();
    }

    @Test
    public void shouldErrorWhenHostKeyIsEMpty() {
        //given
        authenticationProvider = S3SftpServer.simpleAuthenticator(users);
        hostKeyPrivate = "";
        subject = createServer();
        exception.expect(S3SftpServerStartException.class);
        exception.expectMessage("Could not load host key");
        //when
        subject.start();
    }

    @Test
    public void shouldErrorWhenNoAuthenticationProvider() {
        //given
        authenticationProvider = null;
        exception.expect(NullPointerException.class);
        exception.expectMessage("authenticationProvider");
        //when
        createServer();
    }

    @Test
    public void shouldErrorWhenPrivateHostKeyIsNull() {
        //given
        authenticationProvider = S3SftpServer.simpleAuthenticator(users);
        hostKeyPrivate = null;
        subject = createServer();
        exception.expect(S3SftpServerStartException.class);
        exception.expectMessage("Could not load host key");
        exception.expectCause(IsInstanceOf.instanceOf(IllegalStateException.class));
        //when
        subject.start();
    }

    @Test
    public void shouldErrorWhenPortIsInvalid() {
        //given
        authenticationProvider = S3SftpServer.simpleAuthenticator(users);
        hostKeyPrivate = HOSTKEY;
        // not running as root so permission should be denied
        port = 22;
        subject = createServer();
        exception.expect(S3SftpServerStartException.class);
        exception.expectMessage("Could not start server");
        exception.expectCause(IsInstanceOf.instanceOf(IOException.class));
        //when
        subject.start();
    }

    @Test
    public void simpleAuthenticatorShouldErrorWhenUsersIsNull() {
        //given
        exception.expect(NullPointerException.class);
        exception.expectMessage("users");
        //when
        S3SftpServer.simpleAuthenticator(null);
    }

    @Test
    public void simpleSessionBucketShouldReturnBucket() {
        //given
        val sessionBucket = S3SftpServer.simpleSessionBucket(bucket);
        //when
        val result = sessionBucket.getBucket(sftpSession);
        //then
        assertThat(result).isSameAs(bucket);
    }

    @Test
    public void simpleSessionBucketShouldErrorWhenBucketIsNull() {
        //given
        exception.expect(NullPointerException.class);
        exception.expectMessage("bucket");
        //when
        S3SftpServer.simpleSessionBucket(null);
    }

    @Test
    public void simpleSessionHomeShouldReturnHome() {
        //given
        val sessionHome = S3SftpServer.perUserHome(home);
        given(sftpSession.getServerSession().getUsername()).willReturn("username");
        //when
        val result = sessionHome.getHomePath(sftpSession);
        //then
        assertThat(result).isEqualTo("home/username");
    }

    @Test
    public void simpleSessionHomeShouldErrorWhenSubdirIsNull() {
        //given
        exception.expect(NullPointerException.class);
        exception.expectMessage("subdir");
        //when
        S3SftpServer.perUserHome(null);
    }

    //    @Test
    //    public void builderToString() {
    //        //given
    //        val builder = S3SftpServer.builder()
    //                .port(200);
    //        //when
    //        val result = builder.toString();
    //        //then
    //        assertThat(result).contains("(port=200,");
    //    }
}
