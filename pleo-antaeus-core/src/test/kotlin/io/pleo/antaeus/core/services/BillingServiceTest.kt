package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.concurrent.thread


class BillingServiceTest {

    // test invoice
    private val money = Money(BigDecimal.ONE,Currency.DKK)
    private var invoice = Invoice(1,1,money,InvoiceStatus.PENDING)

    // mock provider and add some processing delay
    private val provider = mockk<PaymentProvider> {
        every { charge(invoice) } answers {
            Thread.sleep(1000)
            true
        }
    }

    // mock invoice service
    private var invoiceService = mockk<InvoiceService> {
        every { update(1, InvoiceStatus.PAID) } returns 1
    }

    private val billingService = BillingService(
            paymentProvider = provider,
            invoiceService = invoiceService
    )

    @Test
    fun `will assert charge is only called once when using multiple threads`() {
        thread {
            billingService.process(invoice)
        }
        thread {
            billingService.process(invoice)
        }

        verify(exactly = 1) { provider.charge(invoice) }
    }
}