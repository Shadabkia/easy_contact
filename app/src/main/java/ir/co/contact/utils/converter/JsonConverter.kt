package ir.co.contact.utils.converter

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JsonConverter(val json: Json = Json { ignoreUnknownKeys = true }) {

    inline fun <reified T> convertObjectToJsonString(obj: T): String = json.encodeToString(obj)

    inline fun <reified T> convertJsonStringToObject(jsonString: String, clazz: Class<T>): T =
        json.decodeFromString(jsonString)
}