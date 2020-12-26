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
@file:JvmName("ResponseTransformer")
@file:JvmMultifileClass

package com.skydoves.sandwich

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.skydoves.sandwich.coroutines.SuspensionFunction
import com.skydoves.sandwich.operators.ApiResponseOperator
import com.skydoves.sandwich.operators.ApiResponseSuspendOperator
import okhttp3.Headers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Requests asynchronously and executes the lambda that receives [ApiResponse] as a result.
 *
 * @param onResult An lambda that receives [ApiResponse] as a result.
 *
 * @return The original [Call].
 */
@JvmSynthetic
inline fun <T> Call<T>.request(
  crossinline onResult: (response: ApiResponse<T>) -> Unit
): Call<T> = apply {
  enqueue(getCallbackFromOnResult(onResult))
}

/**
 * combine a [DataSource] to the call for processing response data more handy.
 */
@JvmSynthetic
inline fun <T> Call<T>.combineDataSource(
  dataSource: DataSource<T>,
  crossinline onResult: (response: ApiResponse<T>) -> Unit
): DataSource<T> = dataSource.combine(this, getCallbackFromOnResult(onResult))

/**
 * Returns a response callback from an onResult lambda.
 *
 * @param onResult A lambda that would be executed when the request finished.
 *
 * @return A [Callback] will be executed.
 */
@PublishedApi
@JvmSynthetic
internal inline fun <T> getCallbackFromOnResult(
  crossinline onResult: (response: ApiResponse<T>) -> Unit
): Callback<T> {
  return object : Callback<T> {
    override fun onResponse(call: Call<T>, response: Response<T>) {
      onResult(ApiResponse.of { response })
    }

    override fun onFailure(call: Call<T>, throwable: Throwable) {
      onResult(ApiResponse.error(throwable))
    }
  }
}

/**
 * A function that would be executed for handling successful responses if the request succeeds.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Success] if the request succeeds.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
inline fun <T> ApiResponse<T>.onSuccess(
  crossinline onResult: ApiResponse.Success<T>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Success) {
    onResult(this)
  }
  return this
}

/**
 * A suspension function that would be executed for handling successful responses if the request succeeds.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Success] if the request succeeds.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
@SuspensionFunction
suspend inline fun <T> ApiResponse<T>.suspendOnSuccess(
  crossinline onResult: suspend ApiResponse.Success<T>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Success) {
    onResult(this)
  }
  return this
}

/**
 * A function that would be executed for handling error responses if the request failed or get an exception.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Failure] if the request failed or get an exception.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
inline fun <T> ApiResponse<T>.onFailure(
  crossinline onResult: ApiResponse.Failure<*>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Failure<*>) {
    onResult(this)
  }
  return this
}

/**
 * A suspension function that would be executed for handling error responses if the request failed or get an exception.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Failure] if the request failed or get an exception.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
@SuspensionFunction
suspend inline fun <T> ApiResponse<T>.suspendOnFailure(
  crossinline onResult: suspend ApiResponse.Failure<*>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Failure<*>) {
    onResult(this)
  }
  return this
}

/**
 * A scope function that would be executed for handling error responses if the request failed.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Failure.Exception] if the request failed.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
inline fun <T> ApiResponse<T>.onError(
  crossinline onResult: ApiResponse.Failure.Error<T>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Failure.Error) {
    onResult(this)
  }
  return this
}

/**
 * A suspension scope function that would be executed for handling error responses if the request failed.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Failure.Exception] if the request failed.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
@SuspensionFunction
suspend inline fun <T> ApiResponse<T>.suspendOnError(
  crossinline onResult: suspend ApiResponse.Failure.Error<T>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Failure.Error) {
    onResult(this)
  }
  return this
}

/**
 * A scope function that would be executed for handling exception responses if the request get an exception.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Failure.Exception] if the request get an exception.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
inline fun <T> ApiResponse<T>.onException(
  crossinline onResult: ApiResponse.Failure.Exception<T>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Failure.Exception) {
    onResult(this)
  }
  return this
}

/**
 * A suspension scope function that would be executed for handling exception responses if the request get an exception.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Failure.Exception] if the request get an exception.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
@SuspensionFunction
suspend inline fun <T> ApiResponse<T>.suspendOnException(
  crossinline onResult: suspend ApiResponse.Failure.Exception<T>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Failure.Exception) {
    onResult(this)
  }
  return this
}

/**
 * Returns a [LiveData] which contains successful data if the response is a [ApiResponse.Success].
 *
 * @return An observable [LiveData] which contains successful data.
 */
fun <T> ApiResponse<T>.toLiveData(): LiveData<T> {
  val liveData = MutableLiveData<T>()
  if (this is ApiResponse.Success) {
    liveData.postValue(data)
  }
  return liveData
}

/**
 * Maps [ApiResponse.Success] to a customized success response model.
 *
 * @param mapper A mapper interface for mapping [ApiResponse.Success] response as a custom [V] instance model.
 *
 * @return A mapped custom [V] error response model.
 */
fun <T, V> ApiResponse.Success<T>.map(mapper: ApiSuccessModelMapper<T, V>): V {
  return mapper.map(this)
}

/**
 * Maps [ApiResponse.Success] to a customized error response model with a receiver scope lambda.
 *
 * @param mapper A mapper interface for mapping [ApiResponse.Success] response as a custom [V] instance model.
 * @param onResult A receiver scope lambda of the mapped custom [V] success response model.
 *
 * @return A mapped custom [V] success response model.
 */
@JvmSynthetic
inline fun <T, V> ApiResponse.Success<T>.map(
  mapper: ApiSuccessModelMapper<T, V>,
  crossinline onResult: V.() -> Unit
) {
  onResult(mapper.map(this))
}

/**
 * Maps [ApiResponse.Success] to a customized error response model with a suspension receiver scope lambda.
 *
 * @param mapper A mapper interface for mapping [ApiResponse.Success] response as a custom [V] instance model.
 * @param onResult A suspension receiver scope lambda of the mapped custom [V] success response model.
 *
 * @return A mapped custom [V] success response model.
 */
@JvmSynthetic
@SuspensionFunction
suspend inline fun <T, V> ApiResponse.Success<T>.suspendMap(
  mapper: ApiSuccessModelMapper<T, V>,
  crossinline onResult: suspend V.() -> Unit
) {
  onResult(mapper.map(this))
}

/**
 * Maps [ApiResponse.Failure.Error] to a customized error response model.
 *
 * @param mapper A mapper interface for mapping [ApiResponse.Failure.Error] response as a custom [V] instance model.
 *
 * @return A mapped custom [V] error response model.
 */
fun <T, V> ApiResponse.Failure.Error<T>.map(mapper: ApiErrorModelMapper<V>): V {
  return mapper.map(this)
}

/**
 * Maps [ApiResponse.Failure.Error] to a customized error response model with a receiver scope lambda.
 *
 * @param mapper A mapper interface for mapping [ApiResponse.Failure.Error] response as a custom [V] instance model.
 * @param onResult A receiver scope lambda of the mapped custom [V] error response model.
 *
 * @return A mapped custom [V] error response model.
 */
@JvmSynthetic
inline fun <T, V> ApiResponse.Failure.Error<T>.map(
  mapper: ApiErrorModelMapper<V>,
  crossinline onResult: V.() -> Unit
) {
  onResult(mapper.map(this))
}

/**
 * Maps [ApiResponse.Failure.Error] to a customized error response model with a suspension receiver scope lambda.
 *
 * @param mapper A mapper interface for mapping [ApiResponse.Failure.Error] response as a custom [V] instance model.
 * @param onResult A suspension receiver scope lambda of the mapped custom [V] error response model.
 *
 * @return A mapped custom [V] error response model.
 */
@JvmSynthetic
@SuspensionFunction
suspend inline fun <T, V> ApiResponse.Failure.Error<T>.suspendMap(
  mapper: ApiErrorModelMapper<V>,
  crossinline onResult: suspend V.() -> Unit
) {
  onResult(mapper.map(this))
}

/**
 * Merges multiple [ApiResponse]s as one [ApiResponse] depending on the policy, [ApiResponseMergePolicy].
 * The default policy is [ApiResponseMergePolicy.IGNORE_FAILURE].
 *
 * @param responses Responses for merging as one [ApiResponse].
 * @param mergePolicy A policy for merging response data depend on the success or not.
 *
 * @return [ApiResponse] that depends on the [ApiResponseMergePolicy].
 */
@JvmSynthetic
fun <T> ApiResponse<List<T>>.merge(
  vararg responses: ApiResponse<List<T>>,
  mergePolicy: ApiResponseMergePolicy = ApiResponseMergePolicy.IGNORE_FAILURE
): ApiResponse<List<T>> {
  val apiResponses = responses.toList().toMutableList()
  apiResponses.add(0, this)

  var apiResponse: ApiResponse.Success<List<T>> =
    ApiResponse.Success(Response.success(mutableListOf(), Headers.headersOf()))

  val data: MutableList<T> = mutableListOf()

  for (response in apiResponses) {
    if (response is ApiResponse.Success) {
      response.data?.let { data.addAll(it) }
      apiResponse = ApiResponse.Success(
        Response.success(data, response.headers)
      )
    } else if (mergePolicy === ApiResponseMergePolicy.PREFERRED_FAILURE) {
      return response
    }
  }

  return apiResponse
}

/**
 * Returns an error message from the [ApiResponse.Failure.Error] that consists of the status and error response.
 *
 * @return An error message from the [ApiResponse.Failure.Error].
 */
fun <T> ApiResponse.Failure.Error<T>.message(): String = toString()

/**
 * Returns an error message from the [ApiResponse.Failure.Exception] that consists of the localized message.
 *
 * @return An error message from the [ApiResponse.Failure.Exception].
 */
fun <T> ApiResponse.Failure.Exception<T>.message(): String = toString()

/**
 * Operates on an [ApiResponse] and return an [ApiResponse].
 * This allows you to handle success and error response instead of the [ApiResponse.onSuccess],
 * [ApiResponse.onError], [ApiResponse.onException] transformers.
 */
@JvmSynthetic
inline fun <reified T, V : ApiResponseOperator<T>> ApiResponse<T>.operator(
  apiResponseOperator: V
): ApiResponse<T> = apply {
  when (this) {
    is ApiResponse.Success -> apiResponseOperator.onSuccess(this)
    is ApiResponse.Failure.Error -> apiResponseOperator.onError(this)
    is ApiResponse.Failure.Exception -> apiResponseOperator.onException(this)
  }
}

/**
 * Operates on an [ApiResponse] and return an [ApiResponse] which should be handled in the suspension scope.
 * This allows you to handle success and error response instead of the [ApiResponse.suspendOnSuccess],
 * [ApiResponse.suspendOnError], [ApiResponse.suspendOnException] transformers.
 */
@JvmSynthetic
@SuspensionFunction
suspend inline fun <reified T, V : ApiResponseSuspendOperator<T>> ApiResponse<T>.suspendOperator(
  apiResponseOperator: V
): ApiResponse<T> = apply {
  when (this) {
    is ApiResponse.Success -> apiResponseOperator.onSuccess(this)
    is ApiResponse.Failure.Error -> apiResponseOperator.onError(this)
    is ApiResponse.Failure.Exception -> apiResponseOperator.onException(this)
  }
}
