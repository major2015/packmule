package com.borderx.packamule;

import com.borderx.packamule.auth.FifthAveAuth;
import com.borderx.packamule.auth.IAuth;
import com.borderx.packamule.resources.AboutResource;
import com.codahale.metrics.MetricRegistry;
import io.dropwizard.Application;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.jdbi.v3.core.Jdbi;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

public class PackmuleApplication extends Application<PackmuleConfiguration> {
    private MetricRegistry metrics;

    @Override
    public String getName() {
        return "packmule";
    }

    @Override
    public void initialize(Bootstrap<PackmuleConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)));
        bootstrap.addBundle(new MigrationsBundle<PackmuleConfiguration>() {
            @Override
            public PooledDataSourceFactory getDataSourceFactory(PackmuleConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        this.metrics = bootstrap.getMetricRegistry();
    }

    public static void main(final String[] args) throws Exception {
        new PackmuleApplication().run(args);
    }

    @Override
    public void run(final PackmuleConfiguration configuration, final Environment env) {
        allowCORS(env);
        final Jdbi jdbi = createJDBI(configuration.getDataSourceFactory(), metrics);
        final HttpClient httpClient = new HttpClientBuilder(env)
                .using(configuration.getHttpClientConfiguration())
                .build("httpClient");
        final IAuth auth = new FifthAveAuth(httpClient, configuration.getFifthAve());

        env.jersey().register(new AboutResource());
    }

    private Jdbi createJDBI(DataSourceFactory config, MetricRegistry metrics) {
        DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setDriverClass("org.postgresql.Driver");
        dataSourceFactory.setUrl(config.getUrl());
        dataSourceFactory.setUser(config.getUser());
        dataSourceFactory.setPassword(config.getPassword());
        dataSourceFactory.setMinSize(config.getMinSize());
        dataSourceFactory.setMinSize(config.getMinSize());
        dataSourceFactory.setMaxSize(config.getMaxSize());
        dataSourceFactory.setInitialSize(5);

        ManagedDataSource dataSource = dataSourceFactory.build(metrics, "postgres");
        Jdbi dbi = Jdbi.create(dataSource);
        dbi.installPlugins();
        return dbi;
    }

    private void allowCORS(Environment environment) {
        FilterRegistration.Dynamic filter = environment
                .servlets()
                .addFilter("CORS", CrossOriginFilter.class);

        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        String[] allowMethods = {
                "GET", "PUT", "POST", "PATCH", "DELETE", "OPTIONS"
        };
        filter.setInitParameter(
                CrossOriginFilter.ALLOWED_METHODS_PARAM,
                StringUtils.join(allowMethods, ","));
        filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        filter.setInitParameter("Accept-Language", "en-US,en");
        String[] allowedHeaders = {
                "Content-Type",
                "Authorization",
                "X-Requested-With",
                "Content-Length",
                "Accept",
                "Origin",
                "X-Session-User",
                "X-Session-Key",
                "User-Agent",
        };
        filter.setInitParameter("allowedHeaders", StringUtils.join(allowedHeaders, ","));
        filter.setInitParameter("allowCredentials", "true");
    }
}
