package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ItemEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("مخزون آصرة العرب", appName)
  }

  @Test
  fun `verify carton and piece conversion logic`() {
    val item = ItemEntity(
      code = "216",
      name = "MEGA CHIPS SEA SALT",
      category = "شبس وتسالي",
      conversionFactor = 120,
      totalPieces = (85 * 120) + 14,
      minStockCartons = 20,
      supplier = "مؤسسة آصرة العرب"
    )

    assertEquals(85, item.currentCartons)
    assertEquals(14, item.remainingPieces)
    assertEquals(10214, item.totalPieces)
    assertEquals(false, item.isLowStock)
  }

  @Test
  fun `verify low stock alert logic`() {
    val item = ItemEntity(
      code = "218",
      name = "MEGA CHIPS YOGHURT & HERBS",
      category = "شبس وتسالي",
      conversionFactor = 120,
      totalPieces = (15 * 120) + 8, // 15 cartons < 20 minStockCartons
      minStockCartons = 20,
      supplier = "مؤسسة آصرة العرب"
    )

    assertEquals(15, item.currentCartons)
    assertEquals(8, item.remainingPieces)
    assertTrue(item.isLowStock)
  }

  @Test
  fun `verify firebase sync manager initialization safety`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.db.WarehouseDatabase.getDatabase(
      context,
      kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined)
    )
    val syncManager = com.example.data.sync.FirebaseSyncManager(context, db.warehouseDao())
    assertEquals(false, syncManager.isFirebaseInitialized())
  }
}

