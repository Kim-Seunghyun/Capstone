package practice.capston.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import practice.capston.repository.ResourceRepository;
import practice.capston.security.handler.CustomAuthenticationFailureHandler;
import practice.capston.security.handler.CustomAuthenticationSuccessHandler;
import practice.capston.security.provider.CustomAuthenticationProvider;
import practice.capston.security.urlmatcher.CustomFilterInvocationSecurityMetadataSource;
import practice.capston.security.urlmatcher.CustomUrlResourcesMapFactoryBean;
import practice.capston.security.urlmatcher.SecurityResourceService;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final ResourceRepository resourceRepository;

    @Bean
    public AuthenticationProvider customAuthenticationProvider() {
        return new CustomAuthenticationProvider();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    @Bean
    public CustomAuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    @Bean
    public FilterInvocationSecurityMetadataSource urlFilterInvocationSecurityMetadataSource() throws Exception {
        return new CustomFilterInvocationSecurityMetadataSource(urlResourcesMapFactoryBean().getObject());
    }

    // DB를 통해서 인가정보를 가져오는 설
    @Bean
    public FilterSecurityInterceptor customFilterSecurityInterceptor() throws Exception {
        FilterSecurityInterceptor filterSecurityInterceptor = new FilterSecurityInterceptor();
        filterSecurityInterceptor.setSecurityMetadataSource(urlFilterInvocationSecurityMetadataSource());
        filterSecurityInterceptor.setAccessDecisionManager(accessDecisionManager());
        filterSecurityInterceptor.setAuthenticationManager(authenticationManagerBean());
        return filterSecurityInterceptor;
    }

    public SecurityResourceService securityResourceService() {
        return new SecurityResourceService(resourceRepository);
    }



    // 인증을 확인하는 메소드
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // 접근 허가 하는 방식을 정하는 메소드.
    private AccessDecisionManager accessDecisionManager() {
        AffirmativeBased affirmativeBased = new AffirmativeBased(getAccessDecisionVoters());
        return affirmativeBased;
    }

    private List<AccessDecisionVoter<?>> getAccessDecisionVoters() {
        return Arrays.asList(new RoleVoter());
    }

    // url 자원 권한을 위한 설정... DB에 접근을 한 후 권한 맵핑 정보를 가져온다.
    // 관련 메소드 : securityResourceService(), urlResourcesMapFactoryBean()


    private CustomUrlResourcesMapFactoryBean urlResourcesMapFactoryBean() {
        CustomUrlResourcesMapFactoryBean customUrlResourcesMapFactoryBean = new CustomUrlResourcesMapFactoryBean();
        customUrlResourcesMapFactoryBean.setSecurityReSourceService(securityResourceService());
        return  customUrlResourcesMapFactoryBean;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(customAuthenticationProvider());
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/", "/login" ,"/register", "/upload").permitAll()
                .antMatchers("/summaryService", "/upload**", "/myDirectory").hasRole("USER")
                .antMatchers("/adminPage", "/adminPage**" ,"/adminPage/**").hasRole("ADMIN")
                .anyRequest().permitAll()

                .and()
                .formLogin().loginPage("/login")
                .loginProcessingUrl("/login_proc")
                .successHandler(customAuthenticationSuccessHandler())
                .failureHandler(customAuthenticationFailureHandler())
                .defaultSuccessUrl("/")
                .permitAll();
    }
}
