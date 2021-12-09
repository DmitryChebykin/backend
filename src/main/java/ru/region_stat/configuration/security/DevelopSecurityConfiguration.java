package ru.region_stat.configuration.security;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Profile("dev")
@Configuration
//@EnableAdminServer
public class DevelopSecurityConfiguration
        extends WebSecurityConfigurerAdapter {
    public DevelopSecurityConfiguration() {
        super(true); // Disable defaults
    }

    @Override
    protected void configure(HttpSecurity http) {
        // Do nothing, this is just overriding the default behavior in WebSecurityConfigurerAdapter

    }
}