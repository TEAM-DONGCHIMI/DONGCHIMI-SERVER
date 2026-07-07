package kr.dongchimi.infrastructure.storage.testsupport

import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName

/**
 * 전체 테스트 스펙이 공유하는 싱글턴 LocalStack 컨테이너.
 * object의 클래스 초기화는 JVM에 의해 1회만 수행되므로 컨테이너 기동도 1회만 실행된다.
 */
object TestLocalStackContainer {
    val container: LocalStackContainer =
        LocalStackContainer(DockerImageName.parse("localstack/localstack:3.8"))
            .withServices(LocalStackContainer.Service.S3)

    init {
        container.start()
    }
}
