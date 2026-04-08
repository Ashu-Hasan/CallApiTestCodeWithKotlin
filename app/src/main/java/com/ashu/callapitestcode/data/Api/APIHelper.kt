package com.ashu.callapitestcode.data.Api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response

object APIHelper {

    private val gson = Gson()

    fun getResponseData(
        TAG: String,
        response: Response<JsonObject>,
        ENABLE_TESTING: Boolean
    ): JSONObject? {

        var resultData: JSONObject? = null

        try {
            val httpCode = response.code()

            showTestLog(TAG, "Raw Response Code: $httpCode", ENABLE_TESTING)
            showTestLog(TAG, "Raw Response Body: ${response.body()}", ENABLE_TESTING)
            showTestLog(
                TAG,
                "Raw Error Body: ${response.errorBody()?.toString() ?: "null"}",
                ENABLE_TESTING
            )

            if (response.isSuccessful && response.body() != null) {

                resultData = JSONObject(response.body().toString())
                resultData.put("http_code", httpCode)

                showTestLog(TAG, "$TAG Json Response: $resultData", ENABLE_TESTING)
                return resultData

            } else if (response.errorBody() != null) {

                val errorJson = response.errorBody()!!.string()
                resultData = JSONObject(errorJson)
                resultData.put("http_code", httpCode)

                showTestLog(TAG, "$TAG errorJson: $resultData", ENABLE_TESTING)
                return resultData

            } else {

                val error = JSONObject().apply {
                    put("status", false)
                    put("message", "No response from server")
                    put("http_code", httpCode)
                }

                showTestLog(TAG, "$TAG errorJson: $error", ENABLE_TESTING)
                return error
            }

        } catch (e: Exception) {

            e.printStackTrace()

            return try {
                val error = JSONObject().apply {
                    put("status", false)
                    put("message", "Parse error: ${e.message}")
                    put("http_code", response.code())
                }

                showTestLog(TAG, "$TAG errorJson: $error", ENABLE_TESTING)
                error

            } catch (ignored: Exception) {
                null
            }
        }
    }

    // 🔁 Convert JSONObject → Model
    fun <T> convertJsonToModel(jsonObject: JSONObject, modelClass: Class<T>): T? {
        return try {
            gson.fromJson(jsonObject.toString(), modelClass)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }

    // 🔁 Convert JSONArray → List<Model>
    fun <T> convertJsonArrayToList(jsonArray: JSONArray, modelClass: Class<T>): ArrayList<T> {
        val listType = TypeToken.getParameterized(ArrayList::class.java, modelClass).type
        return gson.fromJson(jsonArray.toString(), listType)
    }

    // 🔁 Convert String → ArrayList<String>
    fun convertStringToArrayList(TAG: String, jsonString: String): ArrayList<String> {
        val arrayList = ArrayList<String>()

        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                arrayList.add(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "convertStringToArrayList error:- ${e.message}")
        }

        return arrayList
    }

    fun showTestLog(TAG: String, message: String, ENABLE_TESTING: Boolean) {
        if (ENABLE_TESTING) {
            Log.d(TAG, message)
        }
    }
}