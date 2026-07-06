package kr.dongchimi.core.monitoring

interface ErrorNotifier {
    fun notify(context: ErrorContext)
}
