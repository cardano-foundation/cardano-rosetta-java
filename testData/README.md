# H2 Database Connection

## Connecting to Local H2 Database

To connect to the local H2 database using the H2 Shell:

```bash
java -cp h2-2.2.224.jar org.h2.tools.Shell
```

When prompted, enter the following connection details:

```
URL:      jdbc:h2:./devkit.db
Driver:   [Enter] (org.h2.Driver - default)
User:     rosetta_db_admin
Password: weakpwd#123
```

Once connected, you can run SQL commands such as:

```sql
show tables;
```
