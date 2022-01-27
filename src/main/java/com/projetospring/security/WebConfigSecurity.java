package com.projetospring.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter {

	@Autowired
	private ImplementacaoUserDetailsService implementacaoUserDetailsService;

	@Override // Configura as solicitações de acesso http
	protected void configure(HttpSecurity http) throws Exception {

		http.csrf().disable() // Desativa as configurações padrão de memória.
				.authorizeRequests() // Pertimir restringir acessos
				.antMatchers(HttpMethod.GET, "/cadastropessoa")
				.hasAnyRole("ADMIN").anyRequest().authenticated().and().formLogin().permitAll() // permite qualquer // usuário		
				.loginPage("/login").defaultSuccessUrl("/cadastropessoa").failureUrl("/login?error=true").and().logout()
				.logoutSuccessUrl("/login") // Mapeia URL de Logout e invalida usuário autenticadoS
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
	}

	@Override // Cria autenticação do usuário com banco de dados ou em memória
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

		auth.userDetailsService(implementacaoUserDetailsService).passwordEncoder(new BCryptPasswordEncoder());

	}

	@Override

	public void configure(WebSecurity web) throws Exception {

		web.ignoring().antMatchers("/materialize/**");
		

	}

}
