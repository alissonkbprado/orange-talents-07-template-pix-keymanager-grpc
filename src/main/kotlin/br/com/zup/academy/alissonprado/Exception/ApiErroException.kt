package br.com.zup.academy.alissonprado.Exception

import io.micronaut.http.HttpStatus

class ApiErroException(httpStatus: HttpStatus, reason: String) : RuntimeException(reason) {
    private val httpStatus: HttpStatus
    val reason: String

    fun getHttpStatus(): HttpStatus {
        return httpStatus
    }

    init {
        this.httpStatus = httpStatus
        this.reason = reason
    }
}