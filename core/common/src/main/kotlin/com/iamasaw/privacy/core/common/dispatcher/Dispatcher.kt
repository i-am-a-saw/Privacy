package com.iamasaw.privacy.core.common.dispatcher

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: MessengerDispatchers)

enum class MessengerDispatchers { IO, DEFAULT }