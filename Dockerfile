FROM alpine

RUN apk add --no-cache bash aws-cli

RUN mkdir mytiki
WORKDIR mytiki

COPY dist .
COPY cli/target/x86_64-unknown-linux-musl/release/lagoon /usr/local/bin/lagoon

ENV RUST_LOG=info
CMD ["lagoon", "--help"]
