FROM registry.access.redhat.com/ubi9/openjdk-21

# Enforce runtime protection for log4j2 CVE-2021-44228 (affected version from 2.0 to 2.14.1) in the affected vesion is used.
# This applies for version from 2.10 and not below.
ENV LOG4J_FORMAT_MSG_NO_LOOKUPS=true

COPY app.jar app.jar

USER root

RUN microdnf upgrade -y && \
    microdnf clean all

RUN JAVA_HOME=${JAVA_HOME:-""} \
    && chown -c 1001:0 $JAVA_HOME/lib/security/cacerts \
    && chmod -c g+w $JAVA_HOME/lib/security/cacerts \
    && mkdir -p /usr/local/share/ca-certificates/ \
    && chown -c 1001:0 /usr/local/share/ca-certificates/ \
    && chmod -c g+w /usr/local/share/ca-certificates/ \
    && chown -c 1001:0 /etc/ssl/certs \
    && chmod -c g+w /etc/ssl/certs \
    && mkdir -p /tmp/application-certificates

ADD certs /tmp/application-certificates/

COPY files/install_certificates.sh /tmp/
RUN chmod +x /tmp/install_certificates.sh

USER 1001    

EXPOSE 8080

ENTRYPOINT ["/tmp/install_certificates.sh"]

CMD ["java","-Xmx512m", "-jar", "app.jar"]
