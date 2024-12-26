package com.coutuapi.cost

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("com.coutuapi.cost")
class CostApplication

fun main(args: Array<String>) {
	runApplication<CostApplication>(*args)
}
