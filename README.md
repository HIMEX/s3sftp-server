S3 SFTP Server
==============

SFTP Server access to an Amazon S3 Bucket.

## Usage

Include the dependency.

For Maven:

````
<dependency>
    <groupId>com.hubio</groupId>
    <artifactId>s3sftp-server</artifactId>
    <version>${s3sftp-server.version}</version>
</dependency>
````

Create a configuration:

````
final String hostkeyPrivate = "-----BEGIN RSA PRIVATE KEY-----\n"
    + "MIIEpAIBAAKCAQEA3wwVIEr6IoyQMQXnYnAEJgNRZZ00mv34c0xBQTDU6zmVbyub\n"
    + "..."
    + "S0PyRjn4njv2w/p5TJXpUvvolO+RUGI1GOgPb30cXl9V0bLcnPothQ=="
    + "-----END RSA PRIVATE KEY-----";
final Map<String, String> users = new HashMap<>();
users.put("admin", "password");
final S3SftpServerConfiguration config =
      S3SftpServerConfiguration.builder()
                               .port(2022)
                               .uri("s3://s3-us-west-1.amazonaws.com/")
                               .sessionBucket(session -> "my-s3-bucket-name")
                               .sessionHome(session -> "users/" + session.getUsername())
                               .hostKeyPrivate(hostkeyPrivate)
                               .authenticationProvider(S3SftpServer.simpleAuthenticator(users))
                               .build();
````

Create a server:

````
final S3SftpServer server = S3SftpServer.using(config);
````

Start the server:

````
server.start();
````

Stop the server:

````
server.stop();
````

## Example

The example implementation creates and runs the S3 SFTP Server.

**Do not use this example in production**

To use it you will need to create an `application.yml` file like the following,
with appropriate values for each:

````
s3:
    zone: us-west-1
    bucket-name: an-s3-bucket-name
sftp:
    port: 2222
    users:
        bob: "bob's super secret password"
    hostkey-private: |
        -----BEGIN RSA PRIVATE KEY-----
        MIIEowIBAAKCAQEA1P6KENVkG/jbIURrDFHTmKQcDElMp4AAc61i2tmQ6yb/bq0+
        ....
        +1t/66XpkpDKhXemItWLktXzGED7mhLFex0vdxF8m++VoZvcUV9T
        -----END RSA PRIVATE KEY-----

````
