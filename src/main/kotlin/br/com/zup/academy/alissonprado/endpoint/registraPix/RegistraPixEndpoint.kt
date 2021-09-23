package br.com.zup.academy.alissonprado.endpoint.registraPix

import br.com.zup.academy.alissonprado.RegistraPixRequest
import br.com.zup.academy.alissonprado.RegistraPixResponse
import br.com.zup.academy.alissonprado.RegistraPixServiceGrpc
import br.com.zup.academy.alissonprado.handler.ErrorAroundHandler
import io.grpc.stub.StreamObserver
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@ErrorAroundHandler
@Singleton
class RegistraPixEndpoint(
    val service: RegistraPixService
) : RegistraPixServiceGrpc.RegistraPixServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun registraPix(request: RegistraPixRequest, responseObserver: StreamObserver<RegistraPixResponse>) {
        logger.info(
            "Request registraPix received: idClienteBanco: ${request.idClienteBanco.replaceAfter("-", "***")}"
        )
        // Chama função onde valida, consulta ERP do Itaú e persiste dados no Banco
        val chavePix = service.registra(request)

        val response = RegistraPixResponse.newBuilder().setIdPix(chavePix).build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}