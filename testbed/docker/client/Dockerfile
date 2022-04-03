FROM ubuntu:20.04
ARG UID=1000
ARG GID=1000
ARG UNAME

COPY info.bash /usr/local/bin

RUN apt-get update && apt-get upgrade -y && apt-get install -y \
    apt-utils \
    openssl \
    curl \
    jq \
    ssh \
    iputils-ping \
    && rm -Rf /var/lib/apt/lists/*

# Install kubectl (kubernetes client)
RUN mkdir -p /opt/app
RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
RUN curl -LO "https://dl.k8s.io/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl.sha256"
#RUN echo "$(<kubectl.sha256)  kubectl" | sha256sum --check
RUN install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

WORKDIR /opt/app
RUN groupadd -g $GID -o $UNAME
RUN useradd -m -u $UID -g $GID -o -s /bin/bash $UNAME

USER $UNAME
ENV HOSTALIASES=/hosts
RUN mkdir -p /home/$UNAME/.ssh
RUN chown $UNAME:$UNAME /home/$UNAME/.ssh
RUN chmod 700 /home/$UNAME/.ssh
