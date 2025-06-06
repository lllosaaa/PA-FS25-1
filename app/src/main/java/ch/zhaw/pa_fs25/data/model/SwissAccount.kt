package ch.zhaw.pa_fs25.data.model

data class SwissAccount(
    val iban: String,
    val currency: String,
    val name: String?,
    val _links: SwissLinks
)

data class SwissLinks(
    val transactions: SwissHref
)

data class SwissHref(
    val href: String
)
