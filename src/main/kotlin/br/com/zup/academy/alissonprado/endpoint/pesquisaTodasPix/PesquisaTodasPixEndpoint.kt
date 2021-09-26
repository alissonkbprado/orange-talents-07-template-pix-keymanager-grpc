package br.com.zup.academy.alissonprado.endpoint.pesquisaTodasPix

import br.com.zup.academy.alissonprado.ListaChavesPixRequest
import br.com.zup.academy.alissonprado.ListaChavesPixResponse
import br.com.zup.academy.alissonprado.ListaChavesPixServiceGrpc
import br.com.zup.academy.alissonprado.handler.ErrorAroundHandler
import io.grpc.stub.StreamObserver
import jakarta.inject.Singleton

@ErrorAroundHandler
@Singleton
class PesquisaTodasPixEndpoint(
    private val listaChavesService: ListaChavesService
) : ListaChavesPixServiceGrpc.ListaChavesPixServiceImplBase() {

    override fun listaChavesPix(
        request: ListaChavesPixRequest,
        responseObserver: StreamObserver<ListaChavesPixResponse>,
    ) {

        val idClienteBanco = request.idClienteBanco

        val chaves = listaChavesService.pesquisa(idClienteBanco)

        responseObserver.onNext(
            ListaChavesPixResponse.newBuilder()
                .setIdClienteBanco(idClienteBanco)
                .addAllChaves(chaves)
                .build()
        )
        responseObserver.onCompleted()
    }

}

