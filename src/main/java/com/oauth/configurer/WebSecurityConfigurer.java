package com.oauth.configurer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {
	@Autowired
	DataSource dataSource;
	@Autowired
	PasswordEncoder passwordEncoder;
	
	
	@Override
    public void configure(WebSecurity webSecurity) throws Exception {
		webSecurity.ignoring().antMatchers("/resources/**");
    }
	
	@Override
	public void configure(HttpSecurity https) throws Exception {
		
		https.authorizeRequests().
		antMatchers("/2.0/**").permitAll().
		antMatchers("/2.0/check_token").permitAll().
		antMatchers("/oauth/api/client").permitAll().
		and().formLogin().loginPage("/oauth/login").permitAll().
		and().logout().permitAll();
		
		https.csrf().disable();
		
	}
	
	
	@Override
	public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
		authenticationManagerBuilder.authenticationProvider(new IntegratedLoginAuthenticationProvider());
		//authenticationManagerBuilder.jdbcAuthentication().dataSource(dataSource);
	}
	
	@Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
	
}