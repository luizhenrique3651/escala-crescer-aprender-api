package com.crescer_aprender.escala.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AuthMetrics {

    private final Counter loginSucesso;
    private final Counter loginFalha;
    private final Counter acessoNegado;

    public AuthMetrics(MeterRegistry registry) {
        loginSucesso = Counter.builder("auth.login.sucesso.total")
                .description("Logins bem-sucedidos")
                .tag("dominio", "seguranca").register(registry);

        loginFalha = Counter.builder("auth.login.falha.total")
                .description("Tentativas de login falhas")
                .tag("dominio", "seguranca").register(registry);

        acessoNegado = Counter.builder("auth.acesso.negado.total")
                .description("Requisições com 403 Forbidden")
                .tag("dominio", "seguranca").register(registry);
    }

    public void incrementLoginSucesso() { loginSucesso.increment(); }
    public void incrementLoginFalha()   { loginFalha.increment(); }
    public void incrementAcessoNegado() { acessoNegado.increment(); }
}
