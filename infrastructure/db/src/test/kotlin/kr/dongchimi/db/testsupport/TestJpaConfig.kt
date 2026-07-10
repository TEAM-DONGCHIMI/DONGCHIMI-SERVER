package kr.dongchimi.db.testsupport

import kr.dongchimi.db.common.config.JpaAuditingConfig
import kr.dongchimi.db.product.ImportJobRepositoryImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

/**
 * infrastructure:db는 실행 가능한 애플리케이션 모듈이 아니라 @SpringBootConfiguration이 없어
 * @DataJpaTest/@SpringBootTest의 오토컨피그 체인을 탈 수 없다(kotest-extensions-spring도 이 조합을
 * 공식 지원하지 않는다). 필요한 JPA 빈을 여기서 직접 조립하고 @ContextConfiguration(classes = [TestJpaConfig::class])로
 * 지정해서 쓴다. TestPostgresContainer가 기동 시점에 이미 Flyway 마이그레이션을 끝내므로 ddl-auto는 validate로 둔다.
 *
 * ImportJobRepositoryImpl을 직접 new하지 않고 빈으로 등록하는 이유: @Transactional은 Spring이 만든
 * 프록시를 통해 호출될 때만 동작한다. claimNext의 원자성(SELECT FOR UPDATE + UPDATE + SELECT를 한
 * 트랜잭션으로 묶는 것)이 이 테스트의 핵심 검증 대상이라 실제 빈으로 등록해야 한다.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = ["kr.dongchimi.db.product"])
@Import(ImportJobRepositoryImpl::class, JpaAuditingConfig::class)
class TestJpaConfig {
    @Bean
    fun dataSource(): DataSource =
        DriverManagerDataSource().apply {
            setDriverClassName("org.postgresql.Driver")
            url = TestPostgresContainer.jdbcUrl
            username = TestPostgresContainer.username
            password = TestPostgresContainer.password
        }

    @Bean
    fun entityManagerFactory(dataSource: DataSource): LocalContainerEntityManagerFactoryBean =
        LocalContainerEntityManagerFactoryBean().apply {
            this.dataSource = dataSource
            setPackagesToScan("kr.dongchimi.db.product")
            jpaVendorAdapter = HibernateJpaVendorAdapter()
            setJpaPropertyMap(
                mapOf(
                    "hibernate.hbm2ddl.auto" to "validate",
                    "hibernate.physical_naming_strategy" to "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy",
                    "hibernate.implicit_naming_strategy" to "org.springframework.boot.hibernate.SpringImplicitNamingStrategy",
                ),
            )
        }

    @Bean
    fun transactionManager(entityManagerFactory: LocalContainerEntityManagerFactoryBean): PlatformTransactionManager =
        JpaTransactionManager(entityManagerFactory.getObject()!!)
}
