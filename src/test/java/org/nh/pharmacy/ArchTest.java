package org.nh.pharmacy;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchTest {

    @Test
    void servicesAndRepositoriesShouldNotDependOnWebLayer() {

        JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("org.nh.pharmacy");

        noClasses()
            .that()
                .resideInAnyPackage("org.nh.pharmacy.service..")
            .or()
                .resideInAnyPackage("org.nh.pharmacy.repository..")
            .should().dependOnClassesThat()
                .resideInAnyPackage("..org.nh.pharmacy.web..")
        .because("Services and repositories should not depend on web layer")
        .check(importedClasses);
    }
}
