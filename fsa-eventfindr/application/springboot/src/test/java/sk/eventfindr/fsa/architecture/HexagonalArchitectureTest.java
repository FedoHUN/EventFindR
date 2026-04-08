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
                .because("doména má zostať technologicky agnostická a nesmie poznať adaptéry ani runtime vrstvu");

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
                .because("REST vstup má delegovať do domény, nie siahať do JPA adaptéra ani runtime skladania");

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
                .because("JPA adaptér má implementovať doménové porty, nie poznať inbound vrstvu, DTO kontrakt ani runtime");

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
                .because("runtime skladanie beanov patrí iba do springboot modulu");

        rule.check(CLASSES);
    }

    @Test
    void controllers_must_be_explicit_rest_entry_points() {
        ArchRule rule = classes()
                .that().resideInAPackage("sk.eventfindr.fsa.controller..")
                .and().areTopLevelClasses()
                .should().beAnnotatedWith(RestController.class)
                .orShould().beAnnotatedWith(RestControllerAdvice.class)
                .because("controller package má obsahovať iba REST vstupné body a centralizovaný error handling");

        rule.check(CLASSES);
    }

    @Test
    void repository_ports_and_adapters_must_follow_hexagonal_naming_and_roles() {
        ArchRule domainPorts = classes()
                .that().resideInAPackage("sk.eventfindr.fsa.domain")
                .and().haveSimpleNameEndingWith("Repository")
                .should().beInterfaces()
                .because("doménové repository sú porty a majú byť rozhrania");

        ArchRule outboundAdapters = classes()
                .that().resideInAPackage("sk.eventfindr.fsa.jpa..")
                .and().haveSimpleNameEndingWith("RepositoryAdapter")
                .should().beAnnotatedWith(Repository.class)
                .andShould(implementDomainRepositoryPort())
                .because("outbound repository adapter má byť Spring repository bean implementujúci doménový port");

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
                .because("Spring Data repozitáre sú interný detail outbound adaptéra a nemajú sa používať mimo neho");

        rule.check(CLASSES);
    }

    @Test
    void runtime_root_should_only_contain_application_and_configuration_classes() {
        ArchRule rule = classes()
                .that().resideInAPackage("sk.eventfindr.fsa")
                .should().beAnnotatedWith(org.springframework.context.annotation.Configuration.class)
                .orShould().beAnnotatedWith(SpringBootApplication.class)
                .because("root runtime package má obsahovať len bootstrapping a bean konfigurácie");

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
