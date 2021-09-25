package br.com.zup.academy.alissonprado.endpoint.pesquisaPix

import br.com.zup.academy.alissonprado.PesquisaChavePixRequest
import br.com.zup.academy.alissonprado.PesquisaChavePixRequest.FiltroCase.*
import br.com.zup.academy.alissonprado.PesquisaChavePixResponse
import br.com.zup.academy.alissonprado.PesquisaChavePixServiceGrpc
import br.com.zup.academy.alissonprado.handler.ErrorAroundHandler
import br.com.zup.academy.alissonprado.httpClient.bcb.pesquisaChavePixBcb.PesquisaChavePixBcbClient
import br.com.zup.academy.alissonprado.httpClient.bcb.pesquisaChavePixBcb.filtro.Filtro
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import io.grpc.stub.StreamObserver
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@ErrorAroundHandler
@Singleton
class PesquisaPixEndpoint(
    private val validator: Validator,
    private val repository: ChavePixRepository,
    private val bcbClient: PesquisaChavePixBcbClient
) : PesquisaChavePixServiceGrpc.PesquisaChavePixServiceImplBase() {

    override fun pesquisaPix(
        request: PesquisaChavePixRequest,
        responseObserver: StreamObserver<PesquisaChavePixResponse>
    ) {
        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(repository, bcbClient)

        responseObserver.onNext(CarregaChavePixResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()
    }
}

private fun PesquisaChavePixRequest.toModel(validator: Validator): Filtro {

    val filtro = when(filtroCase) {
        PIXID -> pixId.let {
            Filtro.PorPixId(clienteId = it.idClienteBanco, pixId = it.idPix)
        }
        CHAVE -> {
            if (chave.isNullOrEmpty() || chave.length > 77)
                throw IllegalArgumentException("Chave Pix invalida")
            Filtro.PorChave(chave)
        }
        FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)

    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }



    return filtro
}
