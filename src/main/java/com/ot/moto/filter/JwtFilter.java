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
            "/salary/getAllSalariesBetweenDates",
            "/salary/findSalariesOfParticularDriver",
            "/salary/findAllBetweenDates",
            "/salary/v2/settle",
            "/salary/v2/settleSalaryForDriver",
            "/report/getTotalCombinedAmountsForToday",
            "/report/totalBenefitAmountCollected",
            "/report/totalBenefitAmountCollectedByOneDriver",
            "/salary/downloadReport",
            "/order/download-csv-of-driver",
            "/order/download-csv-between-dates",
            "/report/download-OrgReport-for-driver",
            "/order/download-csv-of-driver-between-dates",
            "/order/download-OrgReport-date-between",
            "/report/download-OrgReport-date-between-for-particular-driver",
            "/report/download-payment-for-driver",
            "/report/downloadPaymentBetweenDate",
            "/report/download-payment-for-driver-date-between",
            "/metrics/findAllOrgMetricsByDateTimeBetween",
            "/metrics/findAllTamMetricsByDateTimeBetween",
            "/metrics/findAllPaymentMetricsByDateTimeBetween",



            "/order/findAllDateBetween",
            "/order/findAllDateBetweenParticularDriver",

            "/tam/downloadDriver",
            "/tam/downloadDriverBetweenDate",
            "/tam/downloadBetweenDate",

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
            "/salary/total-payable-amount",
            "/salary/total-payable-amount-driver",
            "/salary/v2/settle",
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
            "/report/download-OrgReport-date-between",
            "/report/download-OrgReport-date-between-for-particular-driver",

            "/report/getAllOrgReports-betweenDates",
            "/report/getAllOrgReports-forDriver-betweenDates",

            "/driver/getAll/rented/s-rented",

            "/tam/download-tam-date-between-for-particular-driver",
            "/tam/download-tam-date-between",
            "/tam/getAllTam-ForDriver-BetweenDates",
            "/tam/getAllTam-BetweenDates",

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
            "/driver/getAllDriverNames",
            "/report/getOrgReports-BetweenDates",
            "/report/getOrgReports-ForDriver-BetweenDates",
            "/report/download-OrgReport-for-driver",
            "/report/download-OrgReport-date-between-for-particular-driver",
            "/order/findTotalOrderOfYesterday",
            "/order/findTotalOrderOfToday",
            "/salary/findTotalPayableOfDriver",
            "/report/getTotalPaymentByDriver",
            "/tam/getTotalTamByDriver",
            "/driver/addMoney",
            "/driver/resetAmounts"
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