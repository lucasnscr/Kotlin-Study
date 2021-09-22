package Config

import domain.UserRepository
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import javax.annotation.PostConstruct
import javax.sql.DataSource
import domain.User  as DomainUser

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = true)
class SecurityConfig(
    val datasource: DataSource
) : WebSecurityConfigurerAdapter() {

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()

        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        http
            .authorizeRequests()
            .antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
            .permitAll()
            .anyRequest()
            .authenticated()
            .and()
            .httpBasic()
    }

    override fun configure(auth: AuthenticationManagerBuilder) {

        val queryUsers = "select username, password, enabled from user where username=?"
        val queryRoles = "select u.username, r.roles from user_roles r, user u where r.user_id = u.id and u.username=?"

        auth.jdbcAuthentication()
            .dataSource(datasource)
            .passwordEncoder(passwordEncoder())
            .usersByUsernameQuery(queryUsers)
            .authoritiesByUsernameQuery(queryRoles)
    }
}

@Configuration
class LoadUserConfig(
    val passwordEncoder: PasswordEncoder,
    val userRepository: UserRepository
) {

    @PostConstruct
    fun init() {
        val admin = DomainUser(
            username = "admin",
            password = passwordEncoder.encode("password"),
            roles = mutableListOf("ROLE_ADMIN")
        )
        userRepository.save(admin)
    }

}


@Configuration
class AppConfig {

    @Bean
    fun messageSource() = ReloadableResourceBundleMessageSource().apply {
        setBasename("classpath:/i18n/messages")
    }
}


@Configuration
class OpenAPIConfig {

    @Bean
    fun openAPIDocumentation(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("C.A.R. API")
                    .description("API do sistema C.A.R., de facilitação de mobilidade urbana")
                    .version("v1.0")
                    .contact(
                        Contact()
                            .name("Lucas Nascimento")
                            .email("lucas.nascimento.scr@hotmail.com")
                    )
            )
    }
}