package br.com.zupacademy.valteir.pix.cadastrar

import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE
import kotlin.reflect.KClass

@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [ValidPixKeyValidator::class])
annotation class ValidPixKey(
    val message: String = "chave Pix inválida",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = [],
)

@Singleton
class ValidPixKeyValidator: javax.validation.ConstraintValidator<ValidPixKey, NovaChavePix> {

    override fun isValid(value: NovaChavePix?, context: javax.validation.ConstraintValidatorContext): Boolean {

        // must be validated with @NotNull
        if (value?.tipoChave == null) {
            return true
        }

        val valid = value.tipoChave.valida(value.valor)
        if (!valid) {
            // https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#section-custom-property-paths
            context.disableDefaultConstraintViolation()
            context
                .buildConstraintViolationWithTemplate(context.defaultConstraintMessageTemplate + " (${value.tipoChave})") // or "chave Pix inválida (${value.tipo})"
                .addPropertyNode("chave").addConstraintViolation()
        }

        return valid
    }
}