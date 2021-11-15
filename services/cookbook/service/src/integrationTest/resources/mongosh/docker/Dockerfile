FROM ubuntu:20.04
RUN apt update -y
RUN apt install -y gnupg wget
RUN wget -qO - https://www.mongodb.org/static/pgp/server-5.0.asc | apt-key add -
RUN echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/5.0 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-5.0.list
RUN apt-get update
RUN apt-get install -y mongodb-mongosh
RUN mkdir -p /opt/mongosh
COPY *.js /opt/mongosh/
CMD ["/bin/bash", "-c", "while :; do echo 'Hit CTRL+C'; sleep 1; done"]