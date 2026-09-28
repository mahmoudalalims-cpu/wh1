package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.GoogleAuthSyncManager
import com.example.data.db.WarehouseDatabase
import com.example.data.model.DamageRecordEntity
import com.example.data.model.IssueOrderEntity
import com.example.data.model.ItemEntity
import com.example.data.model.OrderItem
import com.example.data.model.ReturnOrderEntity
import com.example.data.model.SampleOrderEntity
import com.example.data.model.StockMovementEntity
import com.example.data.repository.WarehouseRepository
import com.example.data.security.SecurityManager
import com.example.data.sync.FirebaseSyncManager
import com.example.data.sync.FirebaseSyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WarehouseViewModel(application: Application) : AndroidViewModel(application) {

    private val database: WarehouseDatabase = WarehouseDatabase.getDatabase(application, viewModelScope)
    private val repository: WarehouseRepository
    val securityManager: SecurityManager = SecurityManager(application)
    val settingsManager: com.example.data.security.WarehouseSettingsManager = com.example.data.security.WarehouseSettingsManager(application)
    val firebaseSyncManager: FirebaseSyncManager
    val googleAuthSyncManager: GoogleAuthSyncManager = GoogleAuthSyncManager(application)
    val themePreferencesManager: com.example.data.theme.ThemePreferencesManager = com.example.data.theme.ThemePreferencesManager(application)
    val driveBackupManager: com.example.data.drive.GoogleDriveBackupManager = com.example.data.drive.GoogleDriveBackupManager(application)

    val storekeeperName: StateFlow<String> = settingsManager.storekeeperName
    val defaultRepresentativeName: StateFlow<String> = settingsManager.defaultRepresentativeName

    fun updateStorekeeperName(newName: String) {
        settingsManager.updateStorekeeperName(newName)
        _uiMessage.value = "تم تثبيت اسم أمين المستودع بنجاح: ${settingsManager.storekeeperName.value}"
    }

    fun updateDefaultRepresentativeName(newName: String) {
        settingsManager.updateDefaultRepresentativeName(newName)
        _uiMessage.value = "تم تثبيت المندوب الافتراضي بنجاح: ${settingsManager.defaultRepresentativeName.value}"
    }

    // --- Search & Filter States ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _showOnlyLowStock = MutableStateFlow(false)
    val showOnlyLowStock: StateFlow<Boolean> = _showOnlyLowStock.asStateFlow()

    // Movements filter
    private val _movementTypeFilter = MutableStateFlow("الكل")
    val movementTypeFilter: StateFlow<String> = _movementTypeFilter.asStateFlow()

    // Base flows
    val allItems: StateFlow<List<ItemEntity>>
    val lowStockItems: StateFlow<List<ItemEntity>>
    val issueOrders: StateFlow<List<IssueOrderEntity>>
    val returnOrders: StateFlow<List<ReturnOrderEntity>>
    val sampleOrders: StateFlow<List<SampleOrderEntity>>
    val damageRecords: StateFlow<List<DamageRecordEntity>>
    val allMovements: StateFlow<List<StockMovementEntity>>
    val allRepresentatives: StateFlow<List<com.example.data.model.RepresentativeEntity>>

    // Filtered Items (Slice & Search)
    val filteredItems: StateFlow<List<ItemEntity>>

    // Filtered Movements
    val filteredMovements: StateFlow<List<StockMovementEntity>>

    // UI Message
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // Currently viewed issue voucher for print/export
    private val _viewedIssueVoucher = MutableStateFlow<IssueOrderEntity?>(null)
    val viewedIssueVoucher: StateFlow<IssueOrderEntity?> = _viewedIssueVoucher.asStateFlow()

    private val _viewedReturnVoucher = MutableStateFlow<ReturnOrderEntity?>(null)
    val viewedReturnVoucher: StateFlow<ReturnOrderEntity?> = _viewedReturnVoucher.asStateFlow()

    private val _viewedSampleVoucher = MutableStateFlow<SampleOrderEntity?>(null)
    val viewedSampleVoucher: StateFlow<SampleOrderEntity?> = _viewedSampleVoucher.asStateFlow()

    init {
        val db = database
        repository = WarehouseRepository(db.warehouseDao())
        firebaseSyncManager = FirebaseSyncManager(application, db.warehouseDao())

        allItems = repository.allItems.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        lowStockItems = repository.lowStockItems.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        issueOrders = repository.issueOrders.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        returnOrders = repository.returnOrders.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        sampleOrders = repository.sampleOrders.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        damageRecords = repository.damageRecords.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allMovements = repository.allMovements.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allRepresentatives = repository.allRepresentatives.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        filteredItems = combine(
            allItems,
            _searchQuery,
            _selectedCategory,
            _showOnlyLowStock
        ) { items, query, category, onlyLowStock ->
            items.filter { item ->
                val matchesQuery = query.isBlank() ||
                        item.name.contains(query, ignoreCase = true) ||
                        item.code.contains(query, ignoreCase = true) ||
                        item.category.contains(query, ignoreCase = true) ||
                        item.supplier.contains(query, ignoreCase = true)

                val matchesCategory = category == "الكل" || item.category == category
                val matchesLowStock = !onlyLowStock || item.isLowStock

                matchesQuery && matchesCategory && matchesLowStock
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        filteredMovements = combine(
            allMovements,
            _movementTypeFilter,
            _searchQuery
        ) { movements, filterType, query ->
            movements.filter { mov ->
                val matchesType = filterType == "الكل" || mov.movementType == filterType
                val matchesQuery = query.isBlank() ||
                        mov.itemName.contains(query, ignoreCase = true) ||
                        mov.itemCode.contains(query, ignoreCase = true) ||
                        mov.relatedOrderNumber.contains(query, ignoreCase = true) ||
                        mov.movementNumber.contains(query, ignoreCase = true)
                matchesType && matchesQuery
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        checkDailyBackupSchedule()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun toggleLowStockSlice(show: Boolean) {
        _showOnlyLowStock.value = show
    }

    fun setMovementTypeFilter(type: String) {
        _movementTypeFilter.value = type
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun showIssueVoucher(order: IssueOrderEntity) {
        _viewedIssueVoucher.value = order
    }

    fun dismissIssueVoucher() {
        _viewedIssueVoucher.value = null
    }

    fun showReturnVoucher(order: ReturnOrderEntity) {
        _viewedReturnVoucher.value = order
    }

    fun dismissReturnVoucher() {
        _viewedReturnVoucher.value = null
    }

    /**
     * Submit new Issue Order
     */
    fun submitIssueOrder(
        destination: String,
        recipientName: String,
        supervisorName: String,
        notes: String,
        items: List<OrderItem>,
        onSuccess: (IssueOrderEntity) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.processIssueOrder(
                destination = destination,
                recipientName = recipientName,
                supervisorName = supervisorName,
                notes = notes,
                items = items
            )
            result.onSuccess { order ->
                _uiMessage.value = "تم اعتماد أمر الصرف بنجاح: ${order.orderNumber}"
                _viewedIssueVoucher.value = order
                onSuccess(order)
                triggerAutoSyncIfEnabled("صرف")
            }.onFailure { err ->
                _uiMessage.value = "خطأ: ${err.message}"
            }
        }
    }

    /**
     * Submit Return Order
     */
    fun submitReturnOrder(
        relatedIssueOrderNumber: String?,
        items: List<OrderItem>,
        reason: String,
        supervisorName: String,
        notes: String,
        onSuccess: (ReturnOrderEntity) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.processReturnOrder(
                relatedIssueOrderNumber = relatedIssueOrderNumber,
                items = items,
                reason = reason,
                supervisorName = supervisorName,
                notes = notes
            )
            result.onSuccess { order ->
                _uiMessage.value = "تم اعتماد أمر الاسترجاع بنجاح: ${order.orderNumber}"
                _viewedReturnVoucher.value = order
                onSuccess(order)
                triggerAutoSyncIfEnabled("استرجاع")
            }.onFailure { err ->
                _uiMessage.value = "خطأ: ${err.message}"
            }
        }
    }

    /**
     * Submit Damage Record
     */
    fun submitDamageRecord(
        itemCode: String,
        cartons: Int,
        pieces: Int,
        reason: String,
        supervisorName: String,
        notes: String,
        photoUri: String? = null,
        onSuccess: (DamageRecordEntity) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.processDamageRecord(
                itemCode = itemCode,
                cartons = cartons,
                pieces = pieces,
                reason = reason,
                supervisorName = supervisorName,
                notes = notes,
                photoUri = photoUri
            )
            result.onSuccess { record ->
                _uiMessage.value = "تم تسجيل التالف بنجاح: ${record.damageNumber}"
                onSuccess(record)
                triggerAutoSyncIfEnabled("تالف")
            }.onFailure { err ->
                _uiMessage.value = "خطأ: ${err.message}"
            }
        }
    }

    /**
     * Add new inventory item
     */
    fun addNewItem(
        code: String,
        name: String,
        category: String,
        conversionFactor: Int,
        cartons: Int,
        pieces: Int,
        minStock: Int,
        supplier: String,
        price: Double,
        packagingUnit: String = "كرتون",
        baseUnit: String = "حبة"
    ) {
        viewModelScope.launch {
            val totalPieces = (cartons * conversionFactor) + pieces
            val item = ItemEntity(
                code = code.trim(),
                name = name.trim(),
                category = category.trim(),
                packagingUnit = packagingUnit.ifBlank { "كرتون" },
                baseUnit = baseUnit.ifBlank { "حبة" },
                conversionFactor = conversionFactor.coerceAtLeast(1),
                totalPieces = totalPieces,
                minStockCartons = minStock,
                supplier = supplier.trim(),
                pricePerCarton = price
            )
            repository.insertItem(item)
            _uiMessage.value = "تم إضافة الصنف بنجاح في المستودع"
        }
    }

    /**
     * Issue samples to sales representative (صرف عينات للمناديب)
     */
    fun issueSampleOrder(
        representativeName: String,
        representativeId: Long?,
        supervisorName: String,
        notes: String,
        items: List<OrderItem>,
        onSuccess: (SampleOrderEntity) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.issueSampleOrder(
                representativeName = representativeName,
                representativeId = representativeId,
                supervisorName = supervisorName,
                notes = notes,
                items = items
            )
            result.onSuccess { entity ->
                _uiMessage.value = "تم اعتماد أمر صرف العينات بنجاح برقم: ${entity.orderNumber}"
                _viewedSampleVoucher.value = entity
                onSuccess(entity)
            }.onFailure { err ->
                _uiMessage.value = "خطأ في أمر العينات: ${err.message}"
            }
        }
    }

    fun rollbackSampleOrder(
        orderNumber: String,
        reason: String = "إلغاء أمر العينات"
    ) {
        viewModelScope.launch {
            val result = repository.rollbackSampleOrder(orderNumber, reason)
            result.onSuccess {
                _uiMessage.value = "تم إلغاء أمر العينات $orderNumber وإرجاع الرصيد بنجاح"
            }.onFailure { err ->
                _uiMessage.value = "خطأ أثناء إلغاء أمر العينات: ${err.message}"
            }
        }
    }

    fun viewSampleVoucher(order: SampleOrderEntity) {
        _viewedSampleVoucher.value = order
    }

    fun dismissSampleVoucher() {
        _viewedSampleVoucher.value = null
    }

    /**
     * Edit existing inventory item
     */
    fun updateItem(item: ItemEntity) {
        viewModelScope.launch {
            repository.updateItem(item)
            _uiMessage.value = "تم تحديث بيانات الصنف بنجاح"
        }
    }

    /**
     * Edit inventory item protected with Admin PIN
     */
    fun updateItemProtected(
        item: ItemEntity,
        adminPin: String,
        onSuccess: () -> Unit
    ) {
        if (securityManager.isProtectionEnabled() && !securityManager.verifyPin(adminPin)) {
            _uiMessage.value = "خطأ: رمز الأمان (كلمة المرور) غير صحيح"
            return
        }
        viewModelScope.launch {
            repository.updateItem(item)
            _uiMessage.value = "تم تعديل بيانات الصنف واعتماد الرصيد بنجاح"
            onSuccess()
        }
    }

    /**
     * Delete an inventory item with Admin PIN verification
     */
    fun deleteItemProtected(
        itemCode: String,
        adminPin: String,
        onSuccess: () -> Unit
    ) {
        if (securityManager.isProtectionEnabled() && !securityManager.verifyPin(adminPin)) {
            _uiMessage.value = "خطأ: رمز الأمان غير صحيح، تم رفض الحذف"
            return
        }
        viewModelScope.launch {
            val result = repository.deleteItemSafely(itemCode, "المشرف المعتمد")
            result.onSuccess {
                _uiMessage.value = "تم حذف الصنف من دليل المستودع وتوثيق العملية في سجل التدقيق"
                onSuccess()
            }.onFailure { err ->
                _uiMessage.value = "تعذر حذف الصنف: ${err.message}"
            }
        }
    }

    /**
     * Cancel / Rollback an Issue Order with Admin PIN
     */
    fun cancelIssueOrderProtected(
        orderNumber: String,
        adminPin: String,
        onSuccess: () -> Unit
    ) {
        if (securityManager.isProtectionEnabled() && !securityManager.verifyPin(adminPin)) {
            _uiMessage.value = "خطأ: رمز الأمان غير صحيح! لا يمكن إلغاء أمر الصرف"
            return
        }
        viewModelScope.launch {
            val result = repository.rollbackIssueOrder(orderNumber, "مشرف المستودع")
            result.onSuccess {
                _uiMessage.value = "تم إلغاء أمر الصرف $orderNumber وإعادة الكميات للمستودع وتوثيق العملية"
                if (_viewedIssueVoucher.value?.orderNumber == orderNumber) {
                    _viewedIssueVoucher.value = null
                }
                onSuccess()
            }.onFailure { err ->
                _uiMessage.value = "تعذر إلغاء أمر الصرف: ${err.message}"
            }
        }
    }

    /**
     * Cancel / Rollback a Return Order with Admin PIN
     */
    fun cancelReturnOrderProtected(
        orderNumber: String,
        adminPin: String,
        onSuccess: () -> Unit
    ) {
        if (securityManager.isProtectionEnabled() && !securityManager.verifyPin(adminPin)) {
            _uiMessage.value = "خطأ: رمز الأمان غير صحيح! لا يمكن إلغاء أمر الاسترجاع"
            return
        }
        viewModelScope.launch {
            val result = repository.rollbackReturnOrder(orderNumber, "مشرف المستودع")
            result.onSuccess {
                _uiMessage.value = "تم إلغاء أمر الاسترجاع $orderNumber بنجاح وتحديث الرصيد"
                if (_viewedReturnVoucher.value?.orderNumber == orderNumber) {
                    _viewedReturnVoucher.value = null
                }
                onSuccess()
            }.onFailure { err ->
                _uiMessage.value = "تعذر إلغاء أمر الاسترجاع: ${err.message}"
            }
        }
    }

    /**
     * Cancel / Rollback a Damage Record with Admin PIN
     */
    fun cancelDamageRecordProtected(
        damageNumber: String,
        adminPin: String,
        onSuccess: () -> Unit
    ) {
        if (securityManager.isProtectionEnabled() && !securityManager.verifyPin(adminPin)) {
            _uiMessage.value = "خطأ: رمز الأمان غير صحيح! لا يمكن إلغاء محضر التالف"
            return
        }
        viewModelScope.launch {
            val result = repository.rollbackDamageRecord(damageNumber, "مشرف المستودع")
            result.onSuccess {
                _uiMessage.value = "تم إلغاء محضر التالف $damageNumber واسترداد الرصيد إلى المستودع"
                onSuccess()
            }.onFailure { err ->
                _uiMessage.value = "تعذر إلغاء محضر التالف: ${err.message}"
            }
        }
    }

    // --- Firebase Cloud Sync Operations ---
    val syncStatus: StateFlow<FirebaseSyncStatus> get() = firebaseSyncManager.syncStatus
    val lastSyncTime: StateFlow<Long?> get() = firebaseSyncManager.lastSyncTime

    /**
     * Smart Bidirectional Sync with Firebase (دمج وتحديث ذكي ثنائي الاتجاه)
     * Protects local data, uploads newer local transactions, and downloads newer cloud records.
     */
    fun syncWithFirebase(onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            val result = firebaseSyncManager.syncWithCloud()
            result.onSuccess { msg ->
                _uiMessage.value = msg
                googleAuthSyncManager.recordSuccessfulAutoSync()
                onComplete?.invoke(true, msg)
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: "فشلت المزامنة السحابية"
                _uiMessage.value = errorMsg
                onComplete?.invoke(false, errorMsg)
            }
        }
    }

    fun uploadAllToFirebase(onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            val result = firebaseSyncManager.uploadAllToCloudFromDatabase()
            result.onSuccess { msg ->
                _uiMessage.value = msg
                googleAuthSyncManager.recordSuccessfulAutoSync()
                onComplete?.invoke(true, msg)
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: "فشل الرفع السحابي"
                _uiMessage.value = errorMsg
                onComplete?.invoke(false, errorMsg)
            }
        }
    }

    fun downloadAllFromFirebase(onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            val result = firebaseSyncManager.downloadFromCloud()
            result.onSuccess { msg ->
                _uiMessage.value = msg
                googleAuthSyncManager.recordSuccessfulAutoSync()
                onComplete?.invoke(true, msg)
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: "فشل التنزيل من السحابة"
                _uiMessage.value = errorMsg
                onComplete?.invoke(false, errorMsg)
            }
        }
    }

    // --- Google Drive & Local Cloud Backup Triggers ---
    fun backupToGoogleDriveJson() {
        viewModelScope.launch {
            try {
                val file = driveBackupManager.createCompleteBackupJson(
                    items = allItems.value,
                    issues = issueOrders.value,
                    returns = returnOrders.value,
                    damages = damageRecords.value,
                    movements = allMovements.value,
                    representatives = allRepresentatives.value
                )
                driveBackupManager.launchSaveToDriveIntent(file, "application/json")
                googleAuthSyncManager.recordSuccessfulAutoSync()
                _uiMessage.value = "تم إنشاء النسخة الاحتياطية بنجاح وجاهزة للرفع إلى Google Drive"
            } catch (e: Exception) {
                _uiMessage.value = "خطأ في إنشاء النسخة: ${e.message}"
            }
        }
    }

    fun exportInventoryToGoogleDriveCsv() {
        viewModelScope.launch {
            try {
                val file = driveBackupManager.createInventoryCsvForDrive(allItems.value)
                driveBackupManager.launchSaveToDriveIntent(file, "text/csv")
                _uiMessage.value = "تم تجهيز جدول المخزون (Excel/Sheets) للحفظ في Drive"
            } catch (e: Exception) {
                _uiMessage.value = "خطأ في تصدير الجدول: ${e.message}"
            }
        }
    }

    // --- Representative CRUD Operations ---
    fun addRepresentative(
        name: String,
        phone: String,
        code: String,
        vehicleNumber: String,
        routeOrArea: String,
        notes: String,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _uiMessage.value = "يجب إدخال اسم المندوب"
                return@launch
            }
            val rep = com.example.data.model.RepresentativeEntity(
                name = name.trim(),
                phone = phone.trim(),
                code = if (code.isNotBlank()) code.trim() else "REP-%02d".format((allRepresentatives.value.size + 1)),
                vehicleNumber = vehicleNumber.trim(),
                routeOrArea = routeOrArea.trim(),
                notes = notes.trim()
            )
            repository.insertRepresentative(rep)
            _uiMessage.value = "تمت إضافة المندوب بنجاح: ${rep.name}"
            onSuccess?.invoke()
        }
    }

    fun updateRepresentative(
        rep: com.example.data.model.RepresentativeEntity,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.updateRepresentative(rep)
            result.onSuccess {
                _uiMessage.value = "تم تحديث بيانات المندوب بنجاح"
                onSuccess?.invoke()
            }.onFailure {
                _uiMessage.value = "خطأ في التحديث: ${it.message}"
            }
        }
    }

    fun deleteRepresentative(id: Long, repName: String) {
        viewModelScope.launch {
            val result = repository.deleteRepresentative(id)
            result.onSuccess {
                _uiMessage.value = "تم حذف المندوب: $repName"
            }.onFailure {
                _uiMessage.value = "خطأ في الحذف: ${it.message}"
            }
        }
    }

    // --- Automatic Synchronization & Daily Schedule Logic ---
    fun triggerAutoSyncIfEnabled(triggerName: String) {
        if (!googleAuthSyncManager.isAutoSyncOnTransactionEnabled()) return
        viewModelScope.launch {
            try {
                val syncResult = firebaseSyncManager.syncWithCloud()
                syncResult.onSuccess {
                    googleAuthSyncManager.recordSuccessfulAutoSync()
                }
            } catch (e: Exception) {
                // Background auto-sync fail shouldn't crash app
            }
        }
    }

    fun checkDailyBackupSchedule() {
        viewModelScope.launch {
            if (googleAuthSyncManager.shouldRunDailySync()) {
                triggerDailyAutoSyncNow()
            }
        }
    }

    fun triggerDailyAutoSyncNow(onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val syncResult = firebaseSyncManager.syncWithCloud()
                syncResult.onSuccess { msg ->
                    googleAuthSyncManager.recordSuccessfulAutoSync()
                    _uiMessage.value = "تمت المزامنة اليومية السحابية التلقائية بنجاح"
                    onComplete?.invoke(true, msg)
                }.onFailure { err ->
                    val errorMsg = err.localizedMessage ?: "تعذر إتمام المزامنة اليومية"
                    onComplete?.invoke(false, errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "حدث استثناء أثناء المزامنة اليومية"
                onComplete?.invoke(false, errorMsg)
            }
        }
    }
}
