package kr.dongchimi.bootstrap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["kr.dongchimi"])
@ConfigurationPropertiesScan("kr.dongchimi")
class DongchimiServerApplication

fun main(args: Array<String>) {
    runApplication<DongchimiServerApplication>(*args)
}
