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
            "/fleet/assignFleet",
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
            "/fleet/search",
            "/report/payment/download",
            "/driver/summary/sum-payToJahez",
            "/driver/summary/total-profit",
            "/driver/summary/download-csv",
            "/driver/getAll",
            "/master/create",
            "/fleet/count-assigned-two-wheeler",
            "/fleet/count-assigned-four-wheeler",
            "/salary/search",
            "/bonus/delete/{id}",
            "/bonus/getAll",
            "/bonus/updateDateBonus",
            "/bonus/updateOrderBonus",
            "/bonus/addDateBonus",
            "/bonus/addOrderBonus",
            "/fleet/getById/{id}",
            "/fleet/getAll",
            "/fleet/assignFleet",
            "/staff/getAll",
            "/visa/getByName",
            "/visa/getById/{id}",
            "/visa/getAll",
            "/visa/update",
            "/visa/create",
            "/driver/flexi",
            "/driver/cr",
            "/driver/company",
            "/driver/other",
            "/driver/visa/CountFlexi",
            "/driver/visa/countCr",
            "/driver/visa/countCompany",
            "/driver/visa/countOther",
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
            "/fleet/unassignFleet",
            "/driver/count-owned-vehicles",
            "/driver/count-not-owned-vehicles",
            "/fleet/getAllAssignedFleets",
            "/fleet/getAllUnAssignedFleets",
            "/fleet/update",
            "/penalty/save",
            "/penalty/update",
            "/penalty/getAll",
            "/penalty/delete/{id}",
            "/penalty/findById/{id}",
            "/fleet/getById/{id}",
            "/fleet/update",
            "/fleet/getAll",
            "/fleet/getAll",
            "/driver/create",
            "/driver/update",
            "/visa/delete",
            "/staff/create",
            "/user/{userId}/status",
            "/fleet/create",
            "/fleet/getById/{id}",
            "/fleet/getAll",
            "/fleet/update",
            "/driver/details",
            "/staff/update",
            "/fleet/count-assigned-four-wheeler",
            "/fleet/count-assigned-two-wheeler",

            "/fleetHistory/getByFleetIdAndDriverId",
            "/fleetHistory/getByDriverId/{driverId}",
            "/fleetHistory/getByFleetId/{fleetId}",
            "/fleetHistory/getById/{id}",
            "/fleetHistory/getAll",
            "/fleetHistory/downloadReport",

            "/driver/getAll/rented/s-rented",

            "/penalty/getPenaltiesByDriverId",
            "/penalty/getPenaltiesByFleetId",
            "/penalty/save",
            "/penalty/update",
            "/penalty/deletePenaltiesByFleetIdAndDriverId",
            "/penalty/getAll",
            "/penalty/settlePenaltyByDriver",
            "/penalty/deletePenaltiesByFleetId",
            "/penalty/downloadPenaltyReport",
            "/bonus/getAllDateBonus",
            "/bonus/getAllOrderBonus",
            "/driver/delete",
            "/report/getAllAnalysis",
            "/report/getAnalysisSum",
            "/report/getDriverAnalysis",
            "/report/getDriverAnalysisSum",
            "/driver/getAllDriverNames"
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