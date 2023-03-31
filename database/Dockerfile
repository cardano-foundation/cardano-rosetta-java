FROM liquibase/liquibase:4.19

USER root
RUN apt-get update && apt-get upgrade -y
RUN apt-get -y install postgresql postgresql-contrib

USER liquibase
WORKDIR /bootstrap-db/
COPY . /bootstrap-db/

ENTRYPOINT ["/bin/bash", "/bootstrap-db/scripts/bootstrap_and_migrate.sh"]
