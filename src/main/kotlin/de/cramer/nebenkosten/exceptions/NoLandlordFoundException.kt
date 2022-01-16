package de.cramer.nebenkosten.exceptions

class NoLandlordFoundException(message: String = "no landlord found") : ConflictException(message) {
    companion object {
        private const val serialVersionUID: Long = -8778970900539437741L
    }
}
