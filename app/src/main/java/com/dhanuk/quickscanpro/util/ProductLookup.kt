package com.dhanuk.quickscanpro.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class ProductInfo(
    val name: String,
    val brand: String,
    val imageUrl: String,
    val categories: List<String>,
    val nutriscoreGrade: String,
    val quantity: String
)

object ProductLookup {

    private const val API_BASE = "https://world.openfoodfacts.org/api/v0/product"

    suspend fun lookup(barcode: String): Result<ProductInfo> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$API_BASE/$barcode.json")
            val response = url.readText()
            val json = JSONObject(response)

            val status = json.optInt("status", 0)
            if (status != 1) {
                return@withContext Result.failure(Exception("Product not found"))
            }

            val product = json.getJSONObject("product")
            val categories = product.optString("categories", "").split(",").map { it.trim() }.filter { it.isNotEmpty() }

            val info = ProductInfo(
                name = product.optString("product_name", "Unknown Product"),
                brand = product.optString("brands", "Unknown Brand"),
                imageUrl = product.optString("image_front_url", ""),
                categories = categories.take(5),
                nutriscoreGrade = product.optString("nutriscore_grade", "").uppercase(),
                quantity = product.optString("quantity", "")
            )
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
