package com.github.patou.gitmoji

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 对于Interceptor的intercept中可能出现的Throwable包裹成IOExceptionWrapper，转成网络请求失败，而不是应用崩溃
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
 * 将chain.proceed处理中发生的Throwable包装成IOExceptionWrapper
 */
class IOExceptionWrapper(message: String?, cause: Throwable?) : IOException(message, cause)