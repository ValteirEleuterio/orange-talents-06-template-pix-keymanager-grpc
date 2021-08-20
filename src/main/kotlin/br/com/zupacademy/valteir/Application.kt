package br.com.zupacademy.valteir

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zupacademy.valteir")
		.start()
}

