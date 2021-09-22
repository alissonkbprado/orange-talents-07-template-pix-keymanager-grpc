package br.com.zup.academy.alissonprado.handler

import br.com.zup.academy.alissonprado.Exception.ChaveCadastradaException
import br.com.zup.academy.alissonprado.Exception.CpfInconsistenteItauException
import br.com.zup.academy.alissonprado.Exception.IdNaoEncontradoItauException
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.http.client.exceptions.HttpClientException
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorAroundHandler::class)
class ErrorAroundHandlerInterceptor : MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {

        try {
            return context.proceed()
        } catch (ex: Exception) {

            val responseObserver = context.parameterValues[1] as StreamObserver<*>

            val status = when(ex) {
                is ConstraintViolationException -> Status.INVALID_ARGUMENT
                    .withCause(ex)
                    .withDescription(ex.message)
                is IllegalArgumentException -> Status.INVALID_ARGUMENT
                    .withCause(ex)
                    .withDescription("TipoConta ou TipoChave com valor inválido")
                is ChaveCadastradaException -> Status.ALREADY_EXISTS
                    .withCause(ex)
                    .withDescription("Valor de chave informado já está registrado")
                is IdNaoEncontradoItauException -> Status.FAILED_PRECONDITION
                    .withCause(ex)
                    .withDescription("Não encontrados dados do cartão com a instituição financeira")
                is CpfInconsistenteItauException -> Status.FAILED_PRECONDITION
                    .withCause(ex)
                    .withDescription("Valor de CPF da chave diferente do que está cadastrado na instituição financeira")
                is StatusRuntimeException -> Status.INVALID_ARGUMENT
                    .withCause(ex)
                    .withDescription(ex.message)
                is HttpClientException -> Status.INTERNAL
                    .withCause(ex)
                    .withDescription(ex.message)
                else -> Status.UNKNOWN
                    .withCause(ex)
                    .withDescription("Ops, um erro inesperado ocorreu")
            }

            responseObserver.onError(status.asRuntimeException())
        }

        return null
    }

}