package com.ot.moto.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.ot.moto.util.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService detailsService;

    private static final List<String> AUTH_WHITELIST = Arrays.asList(
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
            "/staff/getAll"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return AUTH_WHITELIST.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        String token = null;
        String userName = null;

        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7); // Extract JWT token
            userName = jwtTokenUtil.getUsernameFromToken(token); // Extract username from token
        }

        if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails details = detailsService.loadUserByUsername(userName);

            if (jwtTokenUtil.validateToken(token, details)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userName, null, details.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}