package com.dropinocean.test

import net.bytebuddy.ByteBuddy
import net.bytebuddy.asm.Advice
import net.bytebuddy.matcher.ElementMatchers.isMethod
import java.lang.reflect.Method
import javax.net.ssl.HttpsURLConnection

class UnusedVarTest {
    fun configureHostnameVerifier() {
        HttpsURLConnection.setDefaultHostnameVerifier { h, _ ->
            h == "127.0.0.1" ||
                    h == "0:0:0:0:0:0:0:1" ||
                    h == "localhost"
        }
    }
}

data class ConnectionTarget(
    var hostname: String?,
    var port: Int?
)

class DestructVarTest {

    fun extractPort(connectionTargets: List<ConnectionTarget>): List<Int?> {
        return connectionTargets.stream().map { (_, port) -> port }.toList()
    }

}

class ReifiedMethodTest {

    inline fun <reified T> nameOf(): Result<String> = runCatching {
        T::class.simpleName!!
    }

}

fun byteBuddyTestRedefine() {
    val classesToRedefine =
        listOf(ConnectionTarget::class, UnusedVarTest::class, DestructVarTest::class, ReifiedMethodTest::class)
    classesToRedefine.forEach { clazz ->
        try {
            val newType = ByteBuddy()
                .redefine(clazz.java)
                .visit(
                    Advice.to(PlaygroundAdvice::class.java).on(isMethod())
                )
                .name("New${clazz.simpleName}")
                .make()
            print(newType.typeDescription)
        } catch (e: Exception) {
            println("Unable to redefine ${clazz.simpleName}")
            e.printStackTrace()
        }
    }
}

object PlaygroundAdvice {
    @JvmStatic
    @Advice.OnMethodEnter(inline = false)
    fun onMethodEnter(@Advice.Origin method: Method) {
        println(">>> Entering method: " + method)
    }
}

fun main() {
    byteBuddyTestRedefine()
}