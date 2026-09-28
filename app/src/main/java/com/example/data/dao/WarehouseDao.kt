package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DamageRecordEntity
import com.example.data.model.IssueOrderEntity
import com.example.data.model.ItemEntity
import com.example.data.model.ReturnOrderEntity
import com.example.data.model.SampleOrderEntity
import com.example.data.model.StockMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseDao {

    // --- Items ---
    @Query("SELECT * FROM items ORDER BY code ASC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items ORDER BY code ASC")
    suspend fun getAllItemsList(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE code = :code LIMIT 1")
    suspend fun getItemByCode(code: String): ItemEntity?

    @Query("SELECT lastUpdated FROM items WHERE code = :code LIMIT 1")
    suspend fun getItemLastUpdated(code: String): Long?

    @Query("SELECT * FROM items WHERE (totalPieces / conversionFactor) <= minStockCartons ORDER BY code ASC")
    fun getLowStockItems(): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ItemEntity>)

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Query("UPDATE items SET totalPieces = :newTotalPieces, lastUpdated = :now WHERE code = :code")
    suspend fun updateItemStockWithTime(code: String, newTotalPieces: Int, now: Long)

    suspend fun updateItemStock(code: String, newTotalPieces: Int) {
        updateItemStockWithTime(code, newTotalPieces, System.currentTimeMillis())
    }

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getItemCount(): Int

    @Query("DELETE FROM items WHERE code = :code")
    suspend fun deleteItem(code: String)

    // --- Issue Orders ---
    @Query("SELECT * FROM issue_orders ORDER BY timestamp DESC")
    fun getAllIssueOrders(): Flow<List<IssueOrderEntity>>

    @Query("SELECT * FROM issue_orders ORDER BY timestamp DESC")
    suspend fun getAllIssueOrdersList(): List<IssueOrderEntity>

    @Query("SELECT * FROM issue_orders WHERE orderNumber = :orderNumber LIMIT 1")
    suspend fun getIssueOrder(orderNumber: String): IssueOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssueOrder(order: IssueOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssueOrders(orders: List<IssueOrderEntity>)

    @Query("DELETE FROM issue_orders WHERE orderNumber = :orderNumber")
    suspend fun deleteIssueOrder(orderNumber: String)

    @Query("SELECT COUNT(*) FROM issue_orders")
    suspend fun getIssueOrdersCount(): Int

    // --- Return Orders ---
    @Query("SELECT * FROM return_orders ORDER BY timestamp DESC")
    fun getAllReturnOrders(): Flow<List<ReturnOrderEntity>>

    @Query("SELECT * FROM return_orders ORDER BY timestamp DESC")
    suspend fun getAllReturnOrdersList(): List<ReturnOrderEntity>

    @Query("SELECT * FROM return_orders WHERE orderNumber = :orderNumber LIMIT 1")
    suspend fun getReturnOrder(orderNumber: String): ReturnOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturnOrder(order: ReturnOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturnOrders(orders: List<ReturnOrderEntity>)

    @Query("DELETE FROM return_orders WHERE orderNumber = :orderNumber")
    suspend fun deleteReturnOrder(orderNumber: String)

    @Query("SELECT COUNT(*) FROM return_orders")
    suspend fun getReturnOrdersCount(): Int

    // --- Damage Records ---
    @Query("SELECT * FROM damage_records ORDER BY timestamp DESC")
    fun getAllDamageRecords(): Flow<List<DamageRecordEntity>>

    @Query("SELECT * FROM damage_records ORDER BY timestamp DESC")
    suspend fun getAllDamageRecordsList(): List<DamageRecordEntity>

    @Query("SELECT * FROM damage_records WHERE damageNumber = :damageNumber LIMIT 1")
    suspend fun getDamageRecord(damageNumber: String): DamageRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDamageRecord(record: DamageRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDamageRecords(records: List<DamageRecordEntity>)

    @Query("DELETE FROM damage_records WHERE damageNumber = :damageNumber")
    suspend fun deleteDamageRecord(damageNumber: String)

    @Query("SELECT COUNT(*) FROM damage_records")
    suspend fun getDamageRecordsCount(): Int

    // --- Unified Movements Ledger ---
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    suspend fun getAllMovementsList(): List<StockMovementEntity>

    @Query("SELECT * FROM stock_movements WHERE itemCode = :itemCode ORDER BY timestamp DESC")
    fun getMovementsByItem(itemCode: String): Flow<List<StockMovementEntity>>

    @Query("SELECT MAX(timestamp) FROM stock_movements WHERE itemCode = :itemCode")
    suspend fun getLatestMovementTimestampForItem(itemCode: String): Long?

    @Query("SELECT * FROM stock_movements WHERE movementNumber = :movementNumber LIMIT 1")
    suspend fun getMovement(movementNumber: String): StockMovementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovements(movements: List<StockMovementEntity>)

    @Query("SELECT COUNT(*) FROM stock_movements")
    suspend fun getMovementsCount(): Int

    // --- Representatives (المناديب) ---
    @Query("SELECT * FROM representatives ORDER BY name ASC")
    fun getAllRepresentatives(): Flow<List<com.example.data.model.RepresentativeEntity>>

    @Query("SELECT * FROM representatives ORDER BY name ASC")
    suspend fun getAllRepresentativesList(): List<com.example.data.model.RepresentativeEntity>

    @Query("SELECT * FROM representatives WHERE id = :id LIMIT 1")
    suspend fun getRepresentativeById(id: Long): com.example.data.model.RepresentativeEntity?

    @Query("SELECT * FROM representatives WHERE name = :name LIMIT 1")
    suspend fun getRepresentativeByName(name: String): com.example.data.model.RepresentativeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepresentative(rep: com.example.data.model.RepresentativeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepresentatives(reps: List<com.example.data.model.RepresentativeEntity>)

    @Update
    suspend fun updateRepresentative(rep: com.example.data.model.RepresentativeEntity)

    @Query("DELETE FROM representatives WHERE id = :id")
    suspend fun deleteRepresentative(id: Long)

    @Query("SELECT COUNT(*) FROM representatives")
    suspend fun getRepresentativesCount(): Int

    // --- Sample Orders (العينات المصروفة للمناديب) ---
    @Query("SELECT * FROM sample_orders ORDER BY timestamp DESC")
    fun getAllSampleOrders(): Flow<List<SampleOrderEntity>>

    @Query("SELECT * FROM sample_orders ORDER BY timestamp DESC")
    suspend fun getAllSampleOrdersList(): List<SampleOrderEntity>

    @Query("SELECT * FROM sample_orders WHERE orderNumber = :orderNumber LIMIT 1")
    suspend fun getSampleOrder(orderNumber: String): SampleOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSampleOrder(order: SampleOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSampleOrders(orders: List<SampleOrderEntity>)

    @Query("DELETE FROM sample_orders WHERE orderNumber = :orderNumber")
    suspend fun deleteSampleOrder(orderNumber: String)

    @Query("SELECT COUNT(*) FROM sample_orders")
    suspend fun getSampleOrdersCount(): Int
}
