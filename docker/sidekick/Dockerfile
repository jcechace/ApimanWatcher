FROM jboss/base-jdk:8
LABEL maintainter="jcechace@redhat.com>" \
      description="Sidekick Api publisher for Apiman Vert.X Gateway" \
      version="1.0"

ARG SIDEKICK_DISTRIBUTION="sidekick-shadow.zip"

ENV JBOSS_HOME="/opt/jboss"
ENV SIDEKICK_HOME="${JBOSS_HOME}/sidekick"

COPY ${SIDEKICK_DISTRIBUTION} ${JBOSS_HOME}/

RUN cd ${JBOSS_HOME} \
    && unzip ${JBOSS_HOME}/sidekick-*.zip \
    && rm ${JBOSS_HOME}/sidekick-*.zip \
    && mv ${JBOSS_HOME}/sidekick-* ${SIDEKICK_HOME} \
    && cd ${SIDEKICK_HOME} \
    && chmod +x bin/sidekick

USER root

RUN chown -R jboss:0 ${JBOSS_HOME} \
 && chmod -R g+rw    ${JBOSS_HOME}

USER jboss

VOLUME ${SIDEKICK_HOME}/configs/

CMD ${SIDEKICK_HOME}/bin/sidekick



