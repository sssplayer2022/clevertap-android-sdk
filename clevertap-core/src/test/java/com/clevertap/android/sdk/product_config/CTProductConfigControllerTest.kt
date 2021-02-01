package com.clevertap.android.sdk.product_config

import com.clevertap.android.sdk.BaseAnalyticsManager
import com.clevertap.android.sdk.BaseCallbackManager
import com.clevertap.android.sdk.CoreMetaData
import com.clevertap.android.sdk.DeviceInfo
import com.clevertap.android.sdk.MockCallbackManager
import com.clevertap.android.sdk.MockDeviceInfo
import com.clevertap.android.shared.test.BaseTestCase
import org.junit.*
import org.junit.runner.*
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class CTProductConfigControllerTest : BaseTestCase() {

    private lateinit var mProductConfigController: CTProductConfigController
    private val guid = "1212121221"
    private lateinit var coreMetaData: CoreMetaData
    private lateinit var analyticsManager: BaseAnalyticsManager
    private lateinit var callbackManager: BaseCallbackManager
    private lateinit var productConfigSettings: ProductConfigSettings
    private lateinit var deviceInfo: DeviceInfo

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        coreMetaData = CoreMetaData()
        analyticsManager = mock(BaseAnalyticsManager::class.java)
        deviceInfo = MockDeviceInfo(application, cleverTapInstanceConfig, guid, coreMetaData)
        callbackManager = MockCallbackManager()
        productConfigSettings = mock(ProductConfigSettings::class.java)
        mProductConfigController = CTProductConfigController(
            application,
            guid,
            cleverTapInstanceConfig,
            analyticsManager,
            coreMetaData,
            callbackManager
        )
    }


    @Test
    fun testFetch_Valid_Guid_Window_Expired() {
        val windowInSeconds = TimeUnit.MINUTES.toSeconds(12)
        `when`(productConfigSettings.nextFetchIntervalInSeconds).thenReturn(windowInSeconds)
        val lastResponseTime = System.currentTimeMillis() - (windowInSeconds * 1000 * 2)
        `when`(productConfigSettings.lastFetchTimeStampInMillis).thenReturn(lastResponseTime)

        mProductConfigController.fetch()
        verify(analyticsManager).sendFetchEvent(any())
        Assert.assertTrue(coreMetaData.isProductConfigRequested)
    }

    @Test
    fun testFetch_Valid_Guid_Window_Not_Expired() {
        val windowInSeconds = TimeUnit.MINUTES.toSeconds(12)
        `when`(productConfigSettings.nextFetchIntervalInSeconds).thenReturn(windowInSeconds)
        val lastResponseTime = System.currentTimeMillis() - (2 * windowInSeconds * 1000)
        `when`(productConfigSettings.lastFetchTimeStampInMillis).thenReturn(lastResponseTime)

        mProductConfigController.fetch()
        verify(analyticsManager, never()).sendFetchEvent(any())
        Assert.assertFalse(coreMetaData.isProductConfigRequested)
    }
}