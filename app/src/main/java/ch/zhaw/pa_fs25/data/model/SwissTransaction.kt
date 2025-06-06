package ch.zhaw.pa_fs25.data.model

data class SwissTransactionResponse(
    val transactions: TransactionsContainer
)

data class TransactionsContainer(
    val booked: List<SwissTransaction>
)

data class SwissTransaction(
    val transactionId: String,
    val bookingDate: String,
    val transactionAmount: Amount,
    val creditorName: String?,
    val remittanceInformationUnstructured: String?
) {
    val amount: Amount
        get() = transactionAmount

    data class Amount(
        val amount: String,
        val currency: String
    )
}
