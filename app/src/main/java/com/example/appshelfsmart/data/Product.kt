package com.example.appshelfsmart.data

import java.util.UUID

data class Product(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val barcode: String,
    val expirationDate: String,
    val weight: String = "",
    val lot: String = "",
    val origin: String = ""
)
