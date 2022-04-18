FROM alpine
COPY ./generate-secret.sh /generate-secret.sh
RUN apk add yq bash
RUN chmod +x /generate-secret.sh
ENTRYPOINT ["/generate-secret.sh"]