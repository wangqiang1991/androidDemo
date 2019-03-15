package com.hande.goochao

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.hande.goochao.utils.JsonUtils
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class JsonUtilsInstrumentedTest {
    @Test
    fun jsonTest() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        Assert.assertEquals("com.hande.goochao", appContext.packageName)

        val jsonObject = JSONObject("{\"a\":{\"a1\":\"a1\"}, \"b\":\"b\"}")
        println(jsonObject)
        println("a.a1=" + JsonUtils.getString(jsonObject, "a.a1", null))
        println("b=" + JsonUtils.getString(jsonObject, "b", null))
    }
}