# Adding the Maven Wrapper

The README uses `./mvnw`. Generate the wrapper once (requires Maven installed):

```bash
mvn -N wrapper:wrapper -Dmaven=3.9.9
```

This adds `mvnw`, `mvnw.cmd`, and `.mvn/wrapper/` so contributors don't need a local
Maven install. Alternatively, replace `./mvnw` with `mvn` everywhere.
