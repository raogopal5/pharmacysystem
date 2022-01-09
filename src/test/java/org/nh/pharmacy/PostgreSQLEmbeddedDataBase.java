package org.nh.pharmacy;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;
import ru.yandex.qatools.embed.postgresql.distribution.Version;

import java.io.IOException;
import java.util.Arrays;

public class PostgreSQLEmbeddedDataBase extends RunListener {

    PostgresProcess process;

    @Override
    public void testRunStarted(Description description) throws Exception {
        super.testRunStarted(description);
        process = embeddedPostgreSQL();
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        super.testRunFinished(result);
        process.stop();
    }

    public PostgresProcess embeddedPostgreSQL() throws IOException {
        final String name = "pharmacy_db_test";
        final String username = "postgres";
        final String password = "Password@123";

        // starting Postgres
        final PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getDefaultInstance();

        final PostgresConfig config = new PostgresConfig(
            Version.V9_5_0,
            new AbstractPostgresConfig.Net("127.0.0.1", 37700),
            new AbstractPostgresConfig.Storage(name),
            new AbstractPostgresConfig.Timeout(),
            new AbstractPostgresConfig.Credentials(username, password)
        );
        //config = PostgresConfig.defaultWithDbName(name, username, password);
        // pass info regarding encoding, locale, collate, ctype, instead of setting global environment settings
        config.getAdditionalInitDbParams().addAll(Arrays.asList(
            "-E", "UTF-8",
            "--locale=en_US.UTF-8",
            "--lc-collate=en_US.UTF-8",
            "--lc-ctype=en_US.UTF-8"
        ));

        PostgresExecutable exec = runtime.prepare(config);
        PostgresProcess process= exec.start();
        return process;
    }

    public static void main(String[] args) {
        try {
            PostgreSQLEmbeddedDataBase p = new PostgreSQLEmbeddedDataBase();
            PostgresProcess process = p.embeddedPostgreSQL();
            process.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
