package ch.zhaw.pa_fs25.util

import ch.zhaw.pa_fs25.data.entity.Category

object TransactionsCategorizer {
    private val categoryKeywordMap: Map<String, List<String>> = mapOf(

        "Groceries" to listOf("coop", "migros", "aldi", "lidl", "denner", "volg", "spar", "migrolino", "manor", "avec", "supermarket", "peroni", "bäckerei", "conditorei", "bakery", "lebensmittel"),
        "Crypto/Exchange" to listOf("exchange to", "btc", "eur", "usd", "chf", "doge", "dot", "eth", "crypto", "wallet", "transaction", "balance migration", "transfer", "revolut", "payment from", "card top-up"),
        "Transportation" to listOf("sbb", "cff", "ffs", "tram", "bus", "train", "billett", "fahrkarte", "ticket", "easypark", "parkhaus", "park", "atm azienda", "autostrade", "milano serravalle", "funicolare", "tpg", "vbl", "zvv", "trainline", "uber", "bolt", "taxi"),
        "Dining Out" to listOf("restaurant", "ristorante", "cafe", "bar", "mcdonald", "burger", "sushi", "pizzeria", "tapas", "grill", "rice up", "food", "snack", "kebab", "coffee", "pub", "brewery", "gastro", "lounge"),
        "Health" to listOf("pharmacy", "apotheke", "doctor", "hospital", "spital", "clinic", "doterra", "dentist", "arztpraxis", "medbase", ),
        "Entertainment" to listOf("cinema", "kino", "movie", "netflix", "spotify", "disney+", "games", "concert", "eventim", "ticketcorner", "theater", "gardaland", "karting", "allianz cinema", "moon and stars", "naturklang", "tickets", "event"),
        "Education" to listOf("zhaw", "school", "university", "course", "tuition", "studies", "weiterbildung", "edu"),
        "Clothing" to listOf("h&m", "zara", "tally", "snipes", "jack & jones", "clothing", "shoes", "sneakers", "jeans", "shirt", "boutique", "under armour", "uniqlo", "adidas", "nike", "puma", "clothes", "fashion", "outfit", "dress"),
        "Utilities" to listOf("swisscom", "salt", "sunrise", "internet", "apple", "google", "microsoft", "spotify", "netflix", "abonnement", "tv", "subscription", "revolut", "top-up", "refill", "payment", "facebook","ups"),
        "Insurance" to listOf("axa", "zurich insurance", "mobiliar", "generali", "insurance", "policy", "healthcare", "versicherung", "premium", "prämie", "rentenversicherung", "pension", "life insurance", "accident insurance", "liability insurance"),
        "Travel" to listOf("hotel", "hostel", "bnb", "booking", "airbnb", "trip", "flight", "flug", "transfer", "cash at", "airport", "sightseeing", "zugreise", "expedia", "kayak", "getyourguide", "nomad", "rental", "carpi", "boat"),
        "Gifts" to listOf("gift", "present", "birthday", "donation", "unicef", "gofundme", "spende"),
        "Electronics" to listOf("amazon", "galaxus", "microspot", "digikey", "fust", "interdiscount", "media markt", "alternate", "whirlpool", "apple", "logitech", "microsoft", "asus"),
        "Miscellaneous" to emptyList()
    )


    fun categorizeTransaction(description: String): String {
        val lowerCaseDescription = description.lowercase()
        for ((category, keywords) in categoryKeywordMap) {
            if (keywords.any { lowerCaseDescription.contains(it) }) {
                return category
            }
        }
        return "Miscellaneous" // Default category if no match is found
    }

    fun categorizeTransactions(transactions: List<String>): Map<String, List<String>> {
        val categorizedTransactions = mutableMapOf<String, MutableList<String>>()

        for (transaction in transactions) {
            val category = categorizeTransaction(transaction)
            if (category !in categorizedTransactions) {
                categorizedTransactions[category] = mutableListOf()
            }
            categorizedTransactions[category]?.add(transaction)
        }

        return categorizedTransactions
    }

    fun detectCategoryId(description: String, categories: List<Category>, i: Int): Int {
        val categoryName = categorizeTransaction(description)
        val category = categories.find { it.name == categoryName }
        return category?.id ?: i // Return the default category ID if not found

    }
}
