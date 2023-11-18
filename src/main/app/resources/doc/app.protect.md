# Protect SSL Keystore Password(s)

##### Generate Environment Secret
- `java -jar irby.jar secret <secret-path>`

##### Protect Keystore Password
- PKCS12 only (one of the following)
- `java -jar irby.jar https-keystore`
- `java -jar irby.jar https-keystore https-default <keystore-path>`

##### Protect Truststore Password
- JKS only (one of the following)
- `java -jar irby.jar https-truststore`
- `java -jar irby.jar https-truststore https-default <truststore-path>`
