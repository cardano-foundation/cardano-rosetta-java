FROM postgres:14.7
COPY init.tar.gz /docker-entrypoint-initdb.d
RUN tar -xf /docker-entrypoint-initdb.d/init.tar.gz -C /docker-entrypoint-initdb.d