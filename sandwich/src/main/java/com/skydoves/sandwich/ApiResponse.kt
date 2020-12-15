/*
 * Designed and developed by 2020 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package com.skydoves.sandwich

import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.Response

/**
 * ApiResponse is an interface for constructing standard responses from the retrofit call.
 */
sealed class ApiResponse<out T> {

  /**
   * API Success response class from OkHttp request call.
   * The [data] is a nullable generic type. (A response without data)
   *
   * @param response A response from OkHttp request call.
   *
   * @property statusCode [StatusCode] is Hypertext Transfer Protocol (HTTP) response status codes.
   * @property headers The header fields of a single HTTP message.
   * @property raw The raw response from the HTTP client.
   * @property data The de-serialized response body of a successful data.
   */
  data class Success<T>(val response: Response<T>) : ApiResponse<T>() {
    val statusCode: StatusCode = getStatusCodeFromResponse(response)
    val headers: Headers = response.headers()
    val raw: okhttp3.Response = response.raw()
    val data: T? = response.body()
    override fun toString() = "[ApiResponse.Success](data=$data)"
  }

  /**
   * API Failure response class from OkHttp request call.
   * There are two subtypes: [ApiResponse.Failure.Error] and [ApiResponse.Failure.Exception].
   */
  sealed class Failure<T> {
    /**
     * API response error case.
     * API communication conventions do not match or applications need to handle errors.
     * e.g., internal server error.
     *
     * @param response A response from OkHttp request call.
     *
     * @property statusCode [StatusCode] is Hypertext Transfer Protocol (HTTP) response status codes.
     * @property headers The header fields of a single HTTP message.
     * @property raw The raw response from the HTTP client.
     * @property errorBody The [ResponseBody] can be consumed only once.
     */
    data class Error<T>(val response: Response<T>) : ApiResponse<T>() {
      val statusCode: StatusCode = getStatusCodeFromResponse(response)
      val headers: Headers = response.headers()
      val raw: okhttp3.Response = response.raw()
      val errorBody: ResponseBody? = response.errorBody()
      override fun toString(): String = "[ApiResponse.Failure.Error-$statusCode](errorResponse=$response)"
    }

    /**
     * API request Exception case.
     * An unexpected exception occurs while creating requests or processing an response in the client side.
     * e.g., network connection error.
     *
     * @param exception An throwable exception.
     *
     * @property message The localized message from the exception.
     */
    data class Exception<T>(val exception: Throwable) : ApiResponse<T>() {
      val message: String? = exception.localizedMessage
      override fun toString(): String = "[ApiResponse.Failure.Exception](message=$message)"
    }
  }

  companion object {
    /**
     * [Failure] factory function. Only receives [Throwable] as an argument.
     *
     * @param ex A throwable.
     *
     * @return A [ApiResponse.Failure.Exception] based on the throwable.
     */
    fun <T> error(ex: Throwable) = Failure.Exception<T>(ex)

    /**
     * ApiResponse Factory.
     *
     * @param successCodeRange A success code range for determining the response is successful or failure.
     * @param [f] Create [ApiResponse] from [retrofit2.Response] returning from the block.
     * If [retrofit2.Response] has no errors, it creates [ApiResponse.Success].
     * If [retrofit2.Response] has errors, it creates [ApiResponse.Failure.Error].
     * If [retrofit2.Response] has occurred exceptions, it creates [ApiResponse.Failure.Exception].
     *
     * @return An [ApiResponse] model which holds information about the response.
     */
    @JvmSynthetic
    inline fun <T> of(
      successCodeRange: IntRange = SandwichInitializer.successCodeRange,
      crossinline f: () -> Response<T>
    ): ApiResponse<T> = try {
      val response = f()
      if (response.raw().code in successCodeRange) {
        Success(response)
      } else {
        Failure.Error(response)
      }
    } catch (ex: Exception) {
      Failure.Exception(ex)
    }

    /**
     * Returns a status code from the [Response].
     *
     * @param response A network callback response.
     *
     * @return A [StatusCode] from the network callback response.
     */
    fun <T> getStatusCodeFromResponse(response: Response<T>): StatusCode {
      return StatusCode.values().find { it.code == response.code() }
        ?: StatusCode.Unknown
    }
  }
}
