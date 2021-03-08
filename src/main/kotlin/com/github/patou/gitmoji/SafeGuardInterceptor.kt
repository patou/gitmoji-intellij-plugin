package com.github.patou.gitmoji

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * For the throwable wrapped in Interceptor's intercept, IOExceptionWrapper, transfer to network requests fail, not application crash
 * @author gclm
 */
class SafeGuardInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            return chain.proceed(chain.request())
        } catch (t: Throwable) {
            throw IOExceptionWrapper("SafeGuarded when requesting ${chain.request().url}", t)
        }
    }
}

/**
 * Packaging the throwable in chain.proceed processing into IOExceptionWrapper
 */
class IOExceptionWrapper(message: String?, cause: Throwable?) : IOException(message, cause)
