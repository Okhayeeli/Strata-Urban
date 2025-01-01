package com.strataurban.strata.Security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomAuthenticationSuccessHandlers {
//
//    implements
//} AuthenticationSuccessHandler {
//    private final ObjectMapper objectMapper;
//    private final JwtService jwtService;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//                                        Authentication authentication) throws IOException, java.io.IOException {
//        User user = (User) authentication.getPrincipal();
//        String accessToken = jwtService.generateAccessToken(user);
//        String refreshToken = jwtService.generateRefreshToken(user);
//
//        Map<String, Object> responseBody = new HashMap<>();
//        responseBody.put("accessToken", accessToken);
//        responseBody.put("refreshToken", refreshToken);
//        responseBody.put("username", user.getUsername());
//        responseBody.put("roles", user.getAuthorities());
//
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
//
//        log.info("User {} successfully authenticated", user.getUsername());
//    }
}