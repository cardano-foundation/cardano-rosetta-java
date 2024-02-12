#!/bin/bash
exec java -jar -DdbUrl="${DB_URL}" -DdbUser="${DB_USER}" -DdbSecret="${DB_SECRET}" -DdbDriverName="${DB_DRIVER_CLASS_NAME}" ./app.jar
