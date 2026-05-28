package sk.eventfindr.fsa.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HexagonalArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("sk.eventfindr.fsa");

    @Test
    void domain_must_not_depend_on_frameworks_or_adapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "sk.eventfindr.fsa.controller..",
                        "sk.eventfindr.fsa.mapper..",
                        "sk.eventfindr.fsa.security..",
                        "sk.eventfindr.fsa.jpa..",
                        "sk.eventfindr.fsa.rest..",
                        "sk.eventfindr.fsa")
                .because("the domain must stay technology-agnostic and must not know adapters or the runtime layer");

        rule.check(CLASSES);
    }

    @Test
    void inbound_layer_must_not_depend_on_outbound_adapter_or_runtime() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "sk.eventfindr.fsa.controller..",
                        "sk.eventfindr.fsa.mapper..",
                        "sk.eventfindr.fsa.security..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "sk.eventfindr.fsa.jpa..",
                        "sk.eventfindr.fsa")
                .because("REST inbound code must delegate to the domain and must not reach into JPA adapters or runtime composition");

        rule.check(CLASSES);
    }

    @Test
    void outbound_layer_must_not_depend_on_inbound_api_contract_or_runtime() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("sk.eventfindr.fsa.jpa..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "sk.eventfindr.fsa.controller..",
                        "sk.eventfindr.fsa.mapper..",
                        "sk.eventfindr.fsa.security..",
                        "sk.eventfindr.fsa.rest..",
                        "sk.eventfindr.fsa.domain.service..",
                        "sk.eventfindr.fsa")
                .because("JPA adapters must implement domain ports and must not know inbound code, DTO contracts, or runtime composition");

        rule.check(CLASSES);
    }

    @Test
    void runtime_package_is_the_only_place_that_may_depend_on_both_adapters() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "sk.eventfindr.fsa.domain..",
                        "sk.eventfindr.fsa.controller..",
                        "sk.eventfindr.fsa.mapper..",
                        "sk.eventfindr.fsa.security..",
                        "sk.eventfindr.fsa.jpa..",
                        "sk.eventfindr.fsa.rest..")
                .should().dependOnClassesThat()
                .resideInAPackage("sk.eventfindr.fsa")
                .because("runtime bean composition belongs only in the springboot module");

        rule.check(CLASSES);
    }

    @Test
    void controllers_must_be_explicit_rest_entry_points() {
        ArchRule rule = classes()
                .that().resideInAPackage("sk.eventfindr.fsa.controller..")
                .and().areTopLevelClasses()
                .should().beAnnotatedWith(RestController.class)
                .orShould().beAnnotatedWith(RestControllerAdvice.class)
                .because("the controller package must contain only REST entry points and centralized error handling");

        rule.check(CLASSES);
    }

    @Test
    void repository_ports_and_adapters_must_follow_hexagonal_naming_and_roles() {
        ArchRule domainPorts = classes()
                .that().resideInAPackage("sk.eventfindr.fsa.domain")
                .and().haveSimpleNameEndingWith("Repository")
                .should().beInterfaces()
                .because("domain repositories are ports and must be interfaces");

        ArchRule outboundAdapters = classes()
                .that().resideInAPackage("sk.eventfindr.fsa.jpa..")
                .and().haveSimpleNameEndingWith("RepositoryAdapter")
                .should().beAnnotatedWith(Repository.class)
                .andShould(implementDomainRepositoryPort())
                .because("outbound repository adapters must be Spring repository beans implementing domain ports");

        domainPorts.check(CLASSES);
        outboundAdapters.check(CLASSES);
    }

    @Test
    void spring_data_repositories_must_stay_internal_to_outbound_module() {
        ArchRule rule = classes()
                .that().resideInAPackage("sk.eventfindr.fsa.jpa..")
                .and().haveSimpleNameEndingWith("SpringDataRepository")
                .should().beInterfaces()
                .andShould().notBePublic()
                .because("Spring Data repositories are internal outbound adapter details and must not be used outside that adapter");

        rule.check(CLASSES);
    }

    @Test
    void runtime_root_should_only_contain_application_and_configuration_classes() {
        ArchRule rule = classes()
                .that().resideInAPackage("sk.eventfindr.fsa")
                .and().areTopLevelClasses()
                .should().beAnnotatedWith(org.springframework.context.annotation.Configuration.class)
                .orShould().beAnnotatedWith(SpringBootApplication.class)
                .because("the root runtime package must contain only bootstrapping and bean configuration classes");

        rule.check(CLASSES);
    }

    private static ArchCondition<JavaClass> implementDomainRepositoryPort() {
        return new ArchCondition<>("implement a domain repository port") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                Set<String> repositoryInterfaces = item.getAllRawInterfaces().stream()
                        .map(JavaClass::getFullName)
                        .filter(name -> name.startsWith("sk.eventfindr.fsa.domain."))
                        .filter(name -> name.endsWith("Repository"))
                        .collect(java.util.stream.Collectors.toSet());

                boolean satisfied = !repositoryInterfaces.isEmpty();
                String message = item.getName() + (satisfied
                        ? " implements domain port " + repositoryInterfaces
                        : " does not implement any domain repository port");
                events.add(new SimpleConditionEvent(item, satisfied, message));
            }
        };
    }
}
