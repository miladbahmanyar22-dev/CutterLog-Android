package com.example.data.entity

/**
 * Data models for the Studio Invoice (صورتحساب استودیو) in CutterLog.
 * Based directly on CutterLog's existing entities and financial calculations.
 */
data class InvoiceItem(
    val rowNumber: Int,
    val projectId: Int,
    val projectName: String,
    val date: String,
    val totalPrice: Double,
    val paidAmount: Double,
    val remainingBalance: Double
)

data class InvoiceData(
    val invoiceNumber: String,
    val issueDate: String,
    val studioName: String,
    val brandTitle: String,
    val items: List<InvoiceItem>,
    val totalAmount: Double,
    val totalPaid: Double,
    val totalRemaining: Double,
    val accountStatus: String,
    val bankCard: String? = null,
    val bankOwner: String? = null,
    val footerNote: String? = null
)
