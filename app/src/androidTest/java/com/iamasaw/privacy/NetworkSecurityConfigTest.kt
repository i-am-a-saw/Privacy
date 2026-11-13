package com.iamasaw.privacy

import android.security.NetworkSecurityPolicy
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkSecurityConfigTest {
    @Test
    fun cleartextMatchesFlavor() {
        val packageName =
            InstrumentationRegistry.getInstrumentation().targetContext.packageName
        val policy = NetworkSecurityPolicy.getInstance()

        assertEquals(
            packageName.endsWith(".local"),
            policy.isCleartextTrafficPermitted("10.0.2.2"),
        )
        assertFalse(policy.isCleartextTrafficPermitted("example.com"))
    }
}
