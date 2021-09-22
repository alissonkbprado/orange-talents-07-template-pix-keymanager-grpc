package br.com.zup.academy.alissonprado.endpoint

import br.com.zup.academy.alissonprado.Exception.ChaveCadastradaException
import br.com.zup.academy.alissonprado.RegistraPixRequest
import br.com.zup.academy.alissonprado.RegistraPixResponse
import br.com.zup.academy.alissonprado.RegistraPixServiceGrpc
import br.com.zup.academy.alissonprado.httpClient.consultaCartaoItau.ConsultaContaItauCliente
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientException
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException

@Singleton
class RegistraPixEndpoint(
    val service: ResgistraPixService,
    val consultaContaClient: ConsultaContaItauCliente
) : RegistraPixServiceGrpc.RegistraPixServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun registraPix(request: RegistraPixRequest, responseObserver: StreamObserver<RegistraPixResponse>) {
        logger.info(
            "Request registraPix received: idClienteBanco: ${request.idClienteBanco.replaceAfter("-", "***")}"
        )

        try {
            // Chama função onde valida, consulta ERP do Itaú e persiste dados no Banco
            val chavePix = service.registra(request)

            val response = RegistraPixResponse.newBuilder().setIdPix(chavePix).build()

            responseObserver?.onNext(response)

        } catch (e: ConstraintViolationException) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription(e.localizedMessage)
                    .asRuntimeException()
            )
            return
        } catch (e: IllegalArgumentException) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("TipoConta ou TipoChave com valor inválido")
                    .asRuntimeException()
            )
            return
        } catch (e: ChaveCadastradaException) {
            responseObserver.onError(
                Status.ALREADY_EXISTS
                    .withDescription("Valor de chave informado já está registrado.")
                    .asRuntimeException()
            )
            return
        } catch (e: StatusRuntimeException) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription(e.message)
                    .asRuntimeException()
            )
            return
        } catch (e: HttpClientException) {
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription(e.message)
                    .asRuntimeException()
            )
            return
        }
        responseObserver?.onCompleted()
    }
}