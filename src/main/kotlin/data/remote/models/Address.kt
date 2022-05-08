package data.remote.models

data class Address (
    val country: String,
    val countryCode: String,
    val postcode: String,
    val road: String,
    val state: String,
    val stateDistrict: String,
    val village: String
)