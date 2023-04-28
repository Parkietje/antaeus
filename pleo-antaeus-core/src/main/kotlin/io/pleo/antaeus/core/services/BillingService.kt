package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock


class BillingService(
        private val paymentProvider: PaymentProvider,
        private val invoiceService: InvoiceService
) {

    private val invoiceLocks = ConcurrentHashMap<Int, ReentrantLock>()

    fun process(invoice: Invoice) {
        val lock = invoiceLocks.computeIfAbsent(invoice.id) { ReentrantLock() }
        try {
            lock.lock()
            if (invoice.status != InvoiceStatus.PAID) {
                val success = paymentProvider.charge(invoice)
                if (success) {
                    // Update the invoice in the database
                    invoiceService.update(invoice.id, InvoiceStatus.PAID)
                }
            }
        } finally {
            lock.unlock()
        }
    }
}
