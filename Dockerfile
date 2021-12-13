FROM openjdk:8

# Env variables
ENV SCALA_VERSION 2.13.2
ENV SBT_VERSION 1.5.5

# Install Scala
## Piping curl directly in tar
RUN \
  curl -fsL https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
  echo >> /root/.bashrc && \
  echo "export PATH=~/scala-$SCALA_VERSION/bin:$PATH" >> /root/.bashrc

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://scala.jfrog.io/artifactory/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion -Dsbt.rootdir=true

# Define working directory
ADD . /klm

WORKDIR /klm

EXPOSE 8089

CMD sbt compile -J-Xss2M -Dsbt.rootdir=true && sbt run -J-Xss2M -Dsbt.rootdir=true
