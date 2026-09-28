package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.WarehouseDao
import com.example.data.model.DamageRecordEntity
import com.example.data.model.IssueOrderEntity
import com.example.data.model.ItemEntity
import com.example.data.model.RepresentativeEntity
import com.example.data.model.ReturnOrderEntity
import com.example.data.model.StockMovementEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.data.model.SampleOrderEntity

@Database(
    entities = [
        ItemEntity::class,
        IssueOrderEntity::class,
        ReturnOrderEntity::class,
        DamageRecordEntity::class,
        StockMovementEntity::class,
        RepresentativeEntity::class,
        SampleOrderEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class WarehouseDatabase : RoomDatabase() {
    abstract fun warehouseDao(): WarehouseDao

    companion object {
        @Volatile
        private var INSTANCE: WarehouseDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE items ADD COLUMN lastUpdated INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `return_orders_new` (
                        `orderNumber` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `relatedIssueOrderNumber` TEXT,
                        `reason` TEXT NOT NULL,
                        `supervisorName` TEXT NOT NULL,
                        `itemsJson` TEXT NOT NULL,
                        `totalCartons` INTEGER NOT NULL,
                        `totalPieces` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        PRIMARY KEY(`orderNumber`)
                    )
                """.trimIndent())
                
                db.execSQL("""
                    INSERT INTO `return_orders_new` (`orderNumber`, `timestamp`, `relatedIssueOrderNumber`, `reason`, `supervisorName`, `itemsJson`, `totalCartons`, `totalPieces`, `notes`, `status`)
                    SELECT `orderNumber`, `timestamp`, `relatedIssueOrderNumber`, `reason`, `supervisorName`, 
                    '[{"itemCode":"' || `itemCode` || '","itemName":"' || `itemName` || '","cartons":' || `cartons` || ',"pieces":' || `pieces` || ',"conversionFactor":1,"price":0.0,"notes":""}]', 
                    `cartons`, `pieces`, `notes`, `status` FROM `return_orders`
                """.trimIndent())
                
                db.execSQL("DROP TABLE `return_orders`")
                db.execSQL("ALTER TABLE `return_orders_new` RENAME TO `return_orders`")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sample_orders` (
                        `orderNumber` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `representativeName` TEXT NOT NULL,
                        `representativeId` INTEGER,
                        `itemsJson` TEXT NOT NULL,
                        `totalCartons` INTEGER NOT NULL,
                        `totalPieces` INTEGER NOT NULL,
                        `supervisorName` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        PRIMARY KEY(`orderNumber`)
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): WarehouseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WarehouseDatabase::class.java,
                    "asrat_warehouse.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance

                instance
            }
        }

        /**
         * Seed all authentic items extracted from user's warehouse images
         */
        suspend fun populateInitialInventory(dao: WarehouseDao) {
            val initialItems = getSeedItemsList()
            dao.insertItems(initialItems)

            // Seed an initial opening stock movement for each item as proof of audit log
            initialItems.forEachIndexed { index, item ->
                val mov = StockMovementEntity(
                    movementNumber = "MOV-INIT-%04d".format(index + 1),
                    timestamp = System.currentTimeMillis() - (86400000L * 7) + (index * 1000L),
                    movementType = "توريد رصيد",
                    itemCode = item.code,
                    itemName = item.name,
                    cartons = item.currentCartons,
                    pieces = item.remainingPieces,
                    totalPiecesDelta = item.totalPieces,
                    stockAfterCartons = item.currentCartons,
                    stockAfterPieces = item.remainingPieces,
                    totalPiecesAfter = item.totalPieces,
                    relatedOrderNumber = "SUP-2026-INIT",
                    notes = "تسجيل الرصيد الافتتاحي لمستودع مؤسسة آصرة العرب",
                    operatorName = "إدارة المستودع"
                )
                dao.insertMovement(mov)
            }

            // Seed initial active representatives
            val initialReps = listOf(
                RepresentativeEntity(
                    name = "أحمد عبد الله الشمري",
                    phone = "0501234567",
                    code = "REP-01",
                    vehicleNumber = "أ ب ج 1234",
                    routeOrArea = "خط الرياض - الشمال (الهايبرات والمطاعم)",
                    notes = "مندوب التوزيع الرئيسي"
                ),
                RepresentativeEntity(
                    name = "خالد إبراهيم الدوسري",
                    phone = "0559876543",
                    code = "REP-02",
                    vehicleNumber = "د ر س 5678",
                    routeOrArea = "خط الرياض - الشرق والوسط (البقالات والتموينات)",
                    notes = "مندوب كبار العملاء"
                ),
                RepresentativeEntity(
                    name = "محمد سالم باوزير",
                    phone = "0543322110",
                    code = "REP-03",
                    vehicleNumber = "ص ع ق 9012",
                    routeOrArea = "خط الخرج والمحافظات المجاورة",
                    notes = "مندوب التوزيع الخارجي"
                )
            )
            dao.insertRepresentatives(initialReps)
        }

        /**
         * Ensures that all standard warehouse items from images exist in database
         */
        suspend fun ensureInitialItemsSeeded(dao: WarehouseDao) {
            val allSeedItems = getSeedItemsList()
            val existing = dao.getAllItemsList()
            val existingCodes = existing.map { it.code }.toSet()
            val missing = allSeedItems.filter { it.code !in existingCodes }
            if (missing.isNotEmpty()) {
                dao.insertItems(missing)
            }
        }

        private fun getSeedItemsList(): List<ItemEntity> {
            return listOf(
                // Item 213 (From Page 4 Unload Order)
                ItemEntity(
                    code = "213",
                    name = "MEGA CHIPS TOMATO KETCHUP 12X12X11GM شيبس كاتشب",
                    category = "شبس وتسالي",
                    conversionFactor = 144,
                    totalPieces = 144 * 80,
                    minStockCartons = 20,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 48.0
                ),
                ItemEntity(
                    code = "216",
                    name = "MEGA CHIPS SEA SALT 6X20X11 GM شيبس ملح البحر",
                    category = "شبس وتسالي",
                    conversionFactor = 120,
                    totalPieces = 120 * 85 + 14,
                    minStockCartons = 20,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 45.0
                ),
                ItemEntity(
                    code = "217",
                    name = "MEGA CHIPS CHEESE AND ONION 6X20X11GM شيبس جبن وبصل",
                    category = "شبس وتسالي",
                    conversionFactor = 120,
                    totalPieces = 120 * 62,
                    minStockCartons = 20,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 45.0
                ),
                ItemEntity(
                    code = "218",
                    name = "MEGA CHIPS YOGHURT & HERBS 6X20X11 GM شيبس لبنه واعشاب",
                    category = "شبس وتسالي",
                    conversionFactor = 120,
                    totalPieces = 120 * 15 + 8,
                    minStockCartons = 20,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 45.0
                ),
                ItemEntity(
                    code = "219",
                    name = "MEGA CHIPS TOMATO KETCHUP 6X20X11 GM شيبس الطماطم كاتشب",
                    category = "شبس وتسالي",
                    conversionFactor = 120,
                    totalPieces = 120 * 73,
                    minStockCartons = 20,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 45.0
                ),
                ItemEntity(
                    code = "220",
                    name = "MEGA CHIPS SEA SALT & VINEGAR 6X20X11 GM شيبس ملح وخل",
                    category = "شبس وتسالي",
                    conversionFactor = 120,
                    totalPieces = 120 * 12,
                    minStockCartons = 20,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 45.0
                ),
                ItemEntity(
                    code = "221",
                    name = "MEGA CHIPS CHILLI & SALSA 6X20X11 GM بطاطس تشلي حار",
                    category = "شبس وتسالي",
                    conversionFactor = 120,
                    totalPieces = 120 * 94,
                    minStockCartons = 20,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 45.0
                ),
                ItemEntity(
                    code = "222",
                    name = "MEGA CHIPS PAPRIKA 6X20X11 GM شيبس بابريكا",
                    category = "شبس وتسالي",
                    conversionFactor = 120,
                    totalPieces = 120 * 55,
                    minStockCartons = 20,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 45.0
                ),
                // Item 223 (From Page 4 Unload Order)
                ItemEntity(
                    code = "223",
                    name = "MEGA CHIPS CHEESE AND ONION 12X12X11GM شبس جبن وبصل",
                    category = "شبس وتسالي",
                    conversionFactor = 144,
                    totalPieces = 144 * 65,
                    minStockCartons = 20,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 48.0
                ),
                // Item 242 (From Page 4 Unload Order)
                ItemEntity(
                    code = "242",
                    name = "ميجا شبس عائلي ملح البحر 24*65جرام",
                    category = "شبس وتسالي",
                    conversionFactor = 24,
                    totalPieces = 24 * 90,
                    minStockCartons = 25,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 38.0
                ),
                ItemEntity(
                    code = "244",
                    name = "ميجا شبس عائلي لبنة واعشاب 65جرام*24",
                    category = "شبس وتسالي",
                    conversionFactor = 24,
                    totalPieces = 24 * 120,
                    minStockCartons = 25,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 38.0
                ),
                ItemEntity(
                    code = "245",
                    name = "ميجا شبس عائلي كاتشب 65جرام*24",
                    category = "شبس وتسالي",
                    conversionFactor = 24,
                    totalPieces = 24 * 105,
                    minStockCartons = 25,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 38.0
                ),
                ItemEntity(
                    code = "246",
                    name = "ميجا شبس عائلي ملح وخل 65جرام*24",
                    category = "شبس وتسالي",
                    conversionFactor = 24,
                    totalPieces = 24 * 40,
                    minStockCartons = 25,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 38.0
                ),
                ItemEntity(
                    code = "247",
                    name = "ميجا شبس عائلي حار 65جرام*24",
                    category = "شبس وتسالي",
                    conversionFactor = 24,
                    totalPieces = 24 * 18,
                    minStockCartons = 25,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 38.0
                ),
                ItemEntity(
                    code = "248",
                    name = "ميجا شبس عائلي بابريكا 65جرام*24",
                    category = "شبس وتسالي",
                    conversionFactor = 24,
                    totalPieces = 24 * 82,
                    minStockCartons = 25,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 38.0
                ),
                ItemEntity(
                    code = "249",
                    name = "بفك فوسكو بالجبن 3 شدة *24 جرام*18",
                    category = "فشار وبفك",
                    conversionFactor = 72,
                    totalPieces = 72 * 45,
                    minStockCartons = 20,
                    supplier = "مصنع الأغذية الوطنية",
                    pricePerCarton = 32.0
                ),
                ItemEntity(
                    code = "250",
                    name = "بفك فوسكو شطة حار 3 شدة *24 جرام*18",
                    category = "فشار وبفك",
                    conversionFactor = 72,
                    totalPieces = 72 * 14,
                    minStockCartons = 20,
                    supplier = "مصنع الأغذية الوطنية",
                    pricePerCarton = 32.0
                ),
                ItemEntity(
                    code = "251",
                    name = "فشار بالملح 4شدة *12*20 جرام",
                    category = "فشار وبفك",
                    conversionFactor = 48,
                    totalPieces = 48 * 65,
                    minStockCartons = 15,
                    supplier = "مصنع الأغذية الوطنية",
                    pricePerCarton = 28.0
                ),
                ItemEntity(
                    code = "252",
                    name = "فشار بالزبدة 4شدة *12*20 جرام",
                    category = "فشار وبفك",
                    conversionFactor = 48,
                    totalPieces = 48 * 52,
                    minStockCartons = 15,
                    supplier = "مصنع الأغذية الوطنية",
                    pricePerCarton = 28.0
                ),
                ItemEntity(
                    code = "253",
                    name = "فشار حلو ومالح 4شدة *12*20 جرام",
                    category = "فشار وبفك",
                    conversionFactor = 48,
                    totalPieces = 48 * 38,
                    minStockCartons = 15,
                    supplier = "مصنع الأغذية الوطنية",
                    pricePerCarton = 28.0
                ),
                ItemEntity(
                    code = "254",
                    name = "بفك يولو بالجبنة 24*60جرام",
                    category = "فشار وبفك",
                    conversionFactor = 24,
                    totalPieces = 24 * 115,
                    minStockCartons = 30,
                    supplier = "مصنع الأغذية الوطنية",
                    pricePerCarton = 30.0
                ),
                ItemEntity(
                    code = "255",
                    name = "بفك يولو بالكاتشب 24*60جرام",
                    category = "فشار وبفك",
                    conversionFactor = 24,
                    totalPieces = 24 * 96,
                    minStockCartons = 30,
                    supplier = "مصنع الأغذية الوطنية",
                    pricePerCarton = 30.0
                ),
                // Item 300 (From Page 4 Unload Order)
                ItemEntity(
                    code = "300",
                    name = "قشطة روابح",
                    category = "منتجات ألبان وقشطة",
                    conversionFactor = 24,
                    totalPieces = 24 * 110,
                    minStockCartons = 20,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 42.0
                ),
                ItemEntity(
                    code = "511",
                    name = "بسكويت ميني كاسات 100جم جيزمو فراولة شد 24",
                    category = "بسكويت وحلويات",
                    conversionFactor = 24,
                    totalPieces = 24 * 80,
                    minStockCartons = 20,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 52.0
                ),
                ItemEntity(
                    code = "512",
                    name = "بسكويت ميني كاسات 100جم جيزمو شوكولاتة شد 24",
                    category = "بسكويت وحلويات",
                    conversionFactor = 24,
                    totalPieces = 24 * 74,
                    minStockCartons = 20,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 52.0
                ),
                ItemEntity(
                    code = "513",
                    name = "بسكويت ميني 25جم شيكوميكو شوكولاتة شد 6*24",
                    category = "بسكويت وحلويات",
                    conversionFactor = 144,
                    totalPieces = 144 * 55,
                    minStockCartons = 20,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 60.0
                ),
                ItemEntity(
                    code = "514",
                    name = "بسكويت ميني 25جم شيكوميكو فراولة شد 6*24",
                    category = "بسكويت وحلويات",
                    conversionFactor = 144,
                    totalPieces = 144 * 60,
                    minStockCartons = 20,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 60.0
                ),
                ItemEntity(
                    code = "515",
                    name = "بسكويت ميني 300جم شيكو ميكو فراولة شد 12",
                    category = "بسكويت وحلويات",
                    conversionFactor = 12,
                    totalPieces = 12 * 90,
                    minStockCartons = 25,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 40.0
                ),
                ItemEntity(
                    code = "516",
                    name = "بسكويت ميني 300جم شيكو ميكو شوكولاتة شد 12",
                    category = "بسكويت وحلويات",
                    conversionFactor = 12,
                    totalPieces = 12 * 21,
                    minStockCartons = 25,
                    supplier = "مؤسسة آصرة العرب",
                    pricePerCarton = 40.0
                )
            )
        }
    }
}
