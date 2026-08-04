package io.astraforge.supplyplatform.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class OrderWebAdapterArchitectureTest {

    private static final String BASE_PACKAGE = "io.astraforge.supplyplatform";
    private static final String ORDER_WEB_PACKAGE =
            "..infrastructure.order.web..";

    private final JavaClasses productionClasses = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages(BASE_PACKAGE);

    @Test
    void testControllersShouldBeFinalAndAnnotatedAsRestControllers() {
        ArchRule rule = classes()
                .that()
                .resideInAPackage(ORDER_WEB_PACKAGE)
                .and()
                .haveSimpleNameEndingWith("Controller")
                .should()
                .haveModifier(JavaModifier.FINAL)
                .andShould()
                .beAnnotatedWith(RestController.class)
                .allowEmptyShould(false)
                .because(
                        "HTTP controllers are immutable adapters and must "
                                + "be discovered explicitly by Spring");

        rule.check(productionClasses);
    }

    @Test
    void testControllersShouldNotDependOnApplicationServices() {
        ArchRule rule = noClasses()
                .that()
                .resideInAPackage(ORDER_WEB_PACKAGE)
                .and()
                .haveSimpleNameEndingWith("Controller")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..application.order.service..")
                .allowEmptyShould(false)
                .because(
                        "controllers must invoke input ports instead of "
                                + "concrete application services");

        rule.check(productionClasses);
    }

    @Test
    void testWebAdaptersShouldNotAccessPersistenceAdapters() {
        ArchRule rule = noClasses()
                .that()
                .resideInAPackage(ORDER_WEB_PACKAGE)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(
                        "..infrastructure.order.persistence..")
                .allowEmptyShould(false)
                .because(
                        "HTTP adapters must not bypass application use cases "
                                + "to access persistence directly");

        rule.check(productionClasses);
    }

    @Test
    void testWebMappersShouldRemainInternalImplementationDetails() {
        ArchRule rule = classes()
                .that()
                .resideInAPackage(ORDER_WEB_PACKAGE)
                .and()
                .haveSimpleNameEndingWith("WebMapper")
                .should()
                .bePackagePrivate()
                .andShould()
                .haveModifier(JavaModifier.FINAL)
                .allowEmptyShould(false)
                .because(
                        "web mappers belong to the adapter implementation "
                                + "and must not become public API");

        rule.check(productionClasses);
    }

    @Test
    void testRequestAndResponseModelsShouldBeRecords() {
        ArchRule requestRule = classes()
                .that()
                .resideInAPackage(ORDER_WEB_PACKAGE)
                .and()
                .haveSimpleNameEndingWith("Request")
                .should()
                .beRecords()
                .allowEmptyShould(false)
                .because(
                        "HTTP request contracts must be immutable value "
                                + "carriers");

        ArchRule responseRule = classes()
                .that()
                .resideInAPackage(ORDER_WEB_PACKAGE)
                .and()
                .haveSimpleNameEndingWith("Response")
                .should()
                .beRecords()
                .allowEmptyShould(false)
                .because(
                        "HTTP response contracts must be immutable value "
                                + "carriers");

        requestRule.check(productionClasses);
        responseRule.check(productionClasses);
    }
}
