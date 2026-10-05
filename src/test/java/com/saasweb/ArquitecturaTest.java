package com.saasweb;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Reglas de la reestructuración (ver el spec en docs/superpowers/specs):
 * cada sistema puede usar el núcleo, pero ningún sistema usa a otro y el
 * núcleo no conoce a ninguno. Si este test falla, algo se "enredó": lo
 * compartido va al núcleo, lo de negocio se duplica en cada sistema.
 */
class ArquitecturaTest {

    private static final String[] SISTEMAS = {"ecommerce", "pos", "restaurante"};

    private final JavaClasses clases = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.saasweb");

    @Test
    void ningunSistemaUsaAOtro() {
        for (String sistema : SISTEMAS) {
            for (String otro : SISTEMAS) {
                if (sistema.equals(otro)) continue;
                noClasses().that().resideInAPackage("com.saasweb." + sistema + "..")
                        .should().dependOnClassesThat().resideInAPackage("com.saasweb." + otro + "..")
                        .allowEmptyShould(true)
                        .because("los sistemas son independientes: " + sistema + " no puede usar " + otro)
                        .check(clases);
            }
        }
    }

    @Test
    void elNucleoNoConoceALosSistemas() {
        for (String sistema : SISTEMAS) {
            noClasses().that().resideInAPackage("com.saasweb.core..")
                    .should().dependOnClassesThat().resideInAPackage("com.saasweb." + sistema + "..")
                    .allowEmptyShould(true)
                    .because("el núcleo es compartido: no puede depender de " + sistema)
                    .check(clases);
        }
    }
}
