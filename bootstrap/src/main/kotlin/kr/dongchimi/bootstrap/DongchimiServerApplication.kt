package kr.dongchimi.bootstrap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DongchimiServerApplication

fun main(args: Array<String>) {
    runApplication<DongchimiServerApplication>(*args)
}