package br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb

import br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb.dto.RemovePixKeyRequest
import br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb.dto.RemovePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client

@Client("\${api_BCB}")
interface RemoveChavePixBcbClient {
    @Delete(
        value = "/api/v1/pix/keys/{key}",
        consumes = [MediaType.APPLICATION_XML],
        produces = [MediaType.APPLICATION_XML]
    )
    fun remove(
        @PathVariable key: String,
        @Body removePixKeyRequest: RemovePixKeyRequest
    ): HttpResponse<RemovePixKeyResponse>
}