package net.spexity.jooq

import org.jooq.codegen.GeneratorStrategy
import org.jooq.codegen.JavaGenerator
import org.jooq.codegen.JavaWriter
import org.jooq.meta.Definition


class QuarkusReflectingGenerator : JavaGenerator() {

    protected override fun printClassAnnotations(
        out: JavaWriter,
        definition: Definition,
        mode: GeneratorStrategy.Mode
    ) {
        when (mode) {
            GeneratorStrategy.Mode.POJO,
            GeneratorStrategy.Mode.RECORD,
            GeneratorStrategy.Mode.INTERFACE,
            GeneratorStrategy.Mode.DAO ->
                out.println("@io.quarkus.runtime.annotations.RegisterForReflection(registerFullHierarchy = true)")

            else -> {}
        }
        super.printClassAnnotations(out, definition, mode)
    }

}