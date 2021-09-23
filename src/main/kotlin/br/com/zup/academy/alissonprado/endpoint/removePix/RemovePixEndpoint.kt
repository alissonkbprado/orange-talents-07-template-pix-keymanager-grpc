package br.com.zup.academy.alissonprado.endpoint.removePix

import br.com.zup.academy.alissonprado.RemovePixRequest
import br.com.zup.academy.alissonprado.RemovePixResponse
import br.com.zup.academy.alissonprado.RemovePixServiceGrpc
import br.com.zup.academy.alissonprado.handler.ErrorAroundHandler
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@ErrorAroundHandler
@Singleton
class RemovePixEndpoint(
   @Inject val service: RemovePixService
) : RemovePixServiceGrpc.RemovePixServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun removePix(request: RemovePixRequest, responseObserver: StreamObserver<RemovePixResponse>) {
        logger.info(
            "Request removePix received: clienteIdBanco: ${request.idClienteBanco.replaceAfter("-", "***")} " +
                    "idPix: ${request.idPix.replaceAfter("-", "***")}"
        )

        val removePixDto = request.toDto()

        // Chama função onde valida, consulta e remove registro do Banco
        val mensagem: String = service.remove(removePixDto)

        val response = RemovePixResponse.newBuilder().setMensagem(mensagem).build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}