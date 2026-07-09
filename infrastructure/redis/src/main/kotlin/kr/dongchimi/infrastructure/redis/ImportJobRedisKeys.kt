package kr.dongchimi.infrastructure.redis

internal object ImportJobRedisKeys {
    fun snapshot(jobId: String) = "import:job:$jobId"

    fun events(jobId: String) = "import:job:$jobId:events"

    fun control(jobId: String) = "import:job:$jobId:control"

    fun cancel(jobId: String) = "import:job:$jobId:cancel"
}
