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



            "/orgReports/upload/orgReports",
            "/driver/details",
            "/order/getOrderCount",
            "/order/getOrderCountByMonth",
            "/order/findAll",
            "/report/upload/jahez",
            "/report/upload/orgReports",
            "/report/getCodAmountForYesterday",
            "/report/getArrearsForToday",

            "/report/current-month",
            "/order/top-driver",
            "/driver/topDriver",
            "/driver/create",
            "/driver/download-csv",
            "/driver/findByUsernameContaining/{name}",
            "/tam/upload/tamSheet",
            "/tam/findAll",
            "/tam/getByJahezRiderId",
            "/tam/findByDriverName",
            "/report/findByDriverName",
            "/staff/delete",
            "/staff/create",
            "/report/getAll",
            "/report/getTotalAmountByPaymentType",
            "/report/getAllOrg",
            "/report/getOrgReportsByDriverId",
            "/report/currentMonthOrgReport",
            "/report/getCodAmountForYesterdayOrgReport",
            "/staff/findByUsernameContaining/{name}",
            "/driver/findByUsernameContaining/{name}",
            "/tam/download",
            "/report/download",

            "/report/sumAmountCollectedByDriver",
            "/report/getSumForCurrentMonthForBenefit",
            "/report/getAmountForYesterdayForBenefit",
            "/report/getAllBankStatement",
            "/tam/sumPayInAmountForCurrentMonth",
            "/tam/sumPayInAmountForYesterday",
            "/master/create",
            "/master/getAll",
            "/master/delete",
            "/master/update",
            "/master/slab/{slab}",
            "/user/forgotPassword",
            "/user/updateNewPassword",
            "/user/validateOTP",
            "/staff/download-csv",
            "/driver/download-csv",
            "/report/findPaymentsByDriverName",
            "/salary/download-csv",
            "/salary/highestBonus",
            "/salary/searchByVehicleNumber",
            "/salary/getAll",
            "/salary/getById/{id}",
            "/salary/salaryPending",
            "/salary/salaryCredited",
            "/salary/settle-salaries",
            "/driver/attendance/details",
            "/summary/findAll",
            "/summary/download",
            "/summary/findBy/{id}",
            "/staff/getAll",
            "/staff/create",
            "/summary/totalPayToJahez",
            "/summary/totalProfit",
            "/summary/totalSalaryPaid",
            "/order/download-csv",
            "/order/findByNameContaining",
            "/order/totalOrders",
            "/order/highestOrders",

            "/report/payment/download",
            "/driver/summary/sum-payToJahez",
            "/driver/summary/total-profit",
            "/driver/summary/download-csv",
            "/driver/getAll",
            "/master/create",


            "/salary/search",
            "/bonus/delete/{id}",
            "/bonus/getAll",
            "/bonus/updateDateBonus",
            "/bonus/updateOrderBonus",
            "/bonus/addDateBonus",
            "/bonus/addOrderBonus",

            "/staff/getAll",
            "/visa/getByName",
            "/visa/getById/{id}",
            "/visa/getAll",
            "/visa/update",
            "/visa/create",
            "/driver/create",
            "/driver/flexi",
            "/driver/cr",
            "/driver/company",
            "/driver/other",
            "/driver/update",
            "/driver/visa/CountFlexi",
            "/driver/visa/countCr",
            "/driver/visa/countCompany",
            "/driver/visa/countOther",
            "/driver/create",
            "/bonus/delete/{id}",
            "/bonus/getAll",
            "/bonus/updateDateBonus",
            "/bonus/updateOrderBonus",
            "/bonus/addDateBonus",
            "/bonus/addOrderBonus",
            "/bonus/addDistanceTravelled",
            "/bonus/updateDistanceTravelled",
            "/user/validateOTP",
            "/user/updateNewPassword",
            "/user/forgotPassword",

            "/driver/count-owned-vehicles",
            "/driver/count-not-owned-vehicles",
            "/penalty/save",
            "/penalty/update",
            "/penalty/getAll",
            "/penalty/delete/{id}",
            "/penalty/findById/{id}",
            "/fleet/count/four-wheelers",
            "/fleet/count/two-wheelers",
            "/fleet/create",
            "/fleet/ownTypeCount",
            "/driver/update",
            "/fleet/count-assigned-two-wheeler",
            "/fleet/count-assigned-four-wheeler",
            "/fleet/search",
            "/fleet/create",
            "/fleet/getAllAssignedFleets",
            "/fleet/getById/{id}",
            "/fleet/unassignFleet",
            "/fleet/getAll",
            "/fleet/update",
            "/fleet/getAllUnAssignedFleets",
            "/fleet/assignFleet",
            "/driver/create",
            "/staff/create",
            "/user/{userId}/status",
            "/driver/details",
            "/staff/update"
    };

    private static final String[] ADMIN_WHITELIST = {};

    private static final String[] DRIVER_WHITELIST = {};

    private static final String[] STAFF_WHITELIST = {};

    private static final String[] ADMIN_STAFF_WHITELIST = {};

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