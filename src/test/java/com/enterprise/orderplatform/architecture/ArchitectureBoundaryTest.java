package com.enterprise.orderplatform.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureBoundaryTest {

    private static final String BASE_PACKAGE = "com.enterprise.orderplatform";

    private final JavaClasses productionClasses = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages(BASE_PACKAGE);

    @Test
    void testDomainShouldNotDependOnOuterLayers() {
        ArchRule rule = noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "..application..",
                        "..configuration..",
                        "..infrastructure..")
                .allowEmptyShould(true)
                .because("the domain must remain independent from application and infrastructure concerns");

        rule.check(productionClasses);
    }

    @Test
    void testApplicationShouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
                .that()
                .resideInAPackage("..application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..configuration..", "..infrastructure..")
                .allowEmptyShould(true)
                .because("application use cases must depend on ports rather than infrastructure adapters");

        rule.check(productionClasses);
    }
}
