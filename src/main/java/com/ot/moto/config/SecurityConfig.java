package com.ot.moto.config;

import com.ot.moto.filter.JwtFilter;
import com.ot.moto.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtFilter filter;

    private static final String[] AUTH_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/authenticate",
            "/admin/create",
            "/admin/getById/{id}",
            "/admin/getAll",
            "/admin/update",
            "/staff/getById/{id}",
            "/driver/getById/{id}",
            "/report/upload/jahez",
            "/report/upload/bankStatement",
            "/staff/delete",
            "/admin/delete",
            "/driver/delete",
            "/report/total-by-type",
            "/fleet/count/four-wheelers",
            "/fleet/count/two-wheelers",
            "/fleet/create",
            "/orgReports/upload/orgReports",
            "/driver/details",
            "/order/getOrderCount",
            "/order/getOrderCountByMonth",
            "/order/findAll",
            "/report/upload/jahez",
            "/report/upload/orgReports",
            "/report/getCodAmountForYesterday",
            "/report/getArrearsForToday",
            "/fleet/ownTypeCount",
            "/report/current-month",
            "/order/top-driver",
            "/driver/topDriver",
            "/driver/create",
            "/driver/findByUsernameContaining/{name}",
            "/tam/upload/tamSheet",
            "/tam/findAll",
            "/tam/getByJahezRiderId",
            "/staff/delete",
            "/staff/create",
            "/report/getAll",
            "/report/getTotalAmountByPaymentType",
            "report/getAllOrg",
            "report/getOrgReportsByDriverId"

    };

    private static final String[] ADMIN_WHITELIST = {"/staff/create", "/staff/update",
            "/user/{userId}/status"};

    private static final String[] DRIVER_WHITELIST = {};

    private static final String[] STAFF_WHITELIST = {};

    private static final String[] ADMIN_STAFF_WHITELIST = {"/driver/create", "/driver/update", "/fleet/create", "/fleet/getById/{id}", "/fleet/getAll", "/fleet/update", "/driver/details"};

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(customUserDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("Admin realm");
        return entryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint()).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .requestMatchers(ADMIN_WHITELIST).hasRole("ADMIN")
                        .requestMatchers(STAFF_WHITELIST).hasRole("STAFF")
                        .requestMatchers(DRIVER_WHITELIST).hasRole("DRIVER")
                        .requestMatchers(ADMIN_STAFF_WHITELIST).hasAnyRole("ADMIN", "STAFF")
                        .anyRequest().authenticated()
                );

        httpSecurity.authenticationProvider(authenticationProvider());

        httpSecurity.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

}