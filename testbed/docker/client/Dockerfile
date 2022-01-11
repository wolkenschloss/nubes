FROM ubuntu:20.04
ARG UID=1000
ARG GID=1000
ARG UNAME

RUN apt-get update && apt-get upgrade -y && apt-get install -y \
    apt-utils \
    openssl \
    curl \
    jq \
    ssh \
    && rm -Rf /var/lib/apt/lists/*

# Install kubectl (kubernetes client)
RUN mkdir -p /opt/app
RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
RUN curl -LO "https://dl.k8s.io/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl.sha256"
#RUN echo "$(<kubectl.sha256)  kubectl" | sha256sum --check
RUN install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Install cmctl (cert-manager client)
RUN curl -L -o cmctl.tar.gz https://github.com/jetstack/cert-manager/releases/latest/download/cmctl-linux-amd64.tar.gz
RUN tar xzf cmctl.tar.gz
RUN mv cmctl /usr/local/bin

WORKDIR /opt/app
RUN groupadd -g $GID -o $UNAME
RUN useradd -m -u $UID -g $GID -o -s /bin/bash $UNAME

USER $UNAME
ENV HOSTALIASES=/hosts
RUN mkdir -p /home/$UNAME/.ssh
RUN chown $UNAME:$UNAME /home/$UNAME/.ssh
RUN chmod 700 /home/$UNAME/.ssh
