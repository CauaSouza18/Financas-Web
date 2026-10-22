package com.app.financas.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AutenticacaoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,

                             Object handler) {
        HttpSession sessao = request.getSession(false);

        if (sessao == null || sessao.getAttribute("usuarioId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);   // 401
            return false;   // barra: o controller nem é chamado
        }
        return true;        // deixa passar
    }
}