package com.example.okhttpdemo

import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    // for moshi
    private val moshi = Moshi.Builder().build()
    private val gistJsonAdapter = moshi.adapter(Gist::class.java)

    // for cancelling call
    private val executor = Executors.newScheduledThreadPool(1)

    // timeout
//    private val client: OkHttpClient = OkHttpClient.Builder()
//        .connectTimeout(5, TimeUnit.SECONDS)
//        .writeTimeout(5, TimeUnit.SECONDS)
//        .readTimeout(5, TimeUnit.SECONDS)
//        .callTimeout(10, TimeUnit.SECONDS)
//        .build()

    // for handle authen
//    private val client = OkHttpClient.Builder()
//        .authenticator(object : Authenticator {
//            @Throws(IOException::class)
//            override fun authenticate(route: Route?, response: Response): Request? {
//                if (response.request.header("Authorization") != null) {
//                    return null // Give up, we've already attempted to authenticate.
//                }
//
//                println("Authenticating for response: $response")
//                println("Challenges: ${response.challenges()}")
//                val credential = Credentials.basic("jesse", "password1")
//                return response.request.newBuilder()
//                    .header("Authorization", credential)
//                    .build()
//            }
//        })
//        .build()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button_request.setOnClickListener {
            postingJson()
//            getAsynchronous()
//            accessHeader()
//            postingString()
//            postStreaming()
//            postingFromPaameter()
//            ParseJSONResponseWithMoshi()
//            cancellingCall()
//            timeout()
//            preCallConfig()
//            handleAuthen()
        }
    }

    fun postingJson() {
        val jsonObject = JSONObject()
        jsonObject.put("name", "morpheus")
        jsonObject.put("job", "leader")
        val body = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://reqres.in/api/users")
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body!!.string()
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    runOnUiThread {
                        textview_result.text = responseBody
                    }
                    println(responseBody)
                }
            }
        })
    }

    fun getAsynchronous() {
        val request = Request.Builder()
            .url("https://publicobject.com/helloworld.txt")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body!!.string()
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }
                    runOnUiThread {
                        textview_result.text = responseBody
                    }
                    println(responseBody)
                }
            }
        })
    }
    fun accessHeader() {
        val request = Request.Builder()
            .url("https://api.github.com/repos/square/okhttp/issues")
            .header("User-Agent", "OkHttp Headers.java")
            .addHeader("Accept", "application/json; q=0.5")
            .addHeader("Accept", "application/vnd.github.v3+json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    println("Server: ${response.header("Server")}")
                    println("Date: ${response.header("Date")}")
                    println("Vary: ${response.headers("Vary")}")
                }
            }
        })
    }

    fun postingString() {
        val postBody = """
        |Releases
        |--------
        |
        | * _1.0_ May 6, 2013
        | * _1.1_ June 15, 2013
        | * _1.2_ August 11, 2013
        |""".trimMargin()

        val request = Request.Builder()
            .url("https://api.github.com/markdown/raw")
            .post(postBody.toRequestBody(MEDIA_TYPE_MARKDOWN))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body!!.string()
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    runOnUiThread {
                        textview_result.text = Html.fromHtml(responseBody)
                    }
                    println(responseBody)
                }
            }
        })
    }

    fun postStreaming() {
        val requestBody = object : RequestBody() {
            override fun contentType() = MEDIA_TYPE_MARKDOWN

            override fun writeTo(sink: BufferedSink) {
                sink.writeUtf8("Numbers\n")
                sink.writeUtf8("-------\n")
                for (i in 2..997) {
                    sink.writeUtf8(String.format(" * $i = ${factor(i)}\n"))
                }
            }

            private fun factor(n: Int): String {
                for (i in 2 until n) {
                    val x = n / i
                    if (x * i == n) return "${factor(x)} × $i"
                }
                return n.toString()
            }
        }

        val request = Request.Builder()
            .url("https://api.github.com/markdown/raw")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body!!.string()
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    runOnUiThread {
                        textview_result.text = Html.fromHtml(responseBody)
                    }
                    println(responseBody)
                }
            }
        })
    }

    fun postingFromParameter() {
        val formBody = FormBody.Builder()
            .add("search", "Jurassic Park")
            .build()
        val request = Request.Builder()
            .url("https://en.wikipedia.org/w/index.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body!!.string()
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    runOnUiThread {
                        textview_result.text = Html.fromHtml(responseBody)
                    }
                    println(responseBody)
                }
            }
        })
    }
    fun ParseJSONResponseWithMoshi() {
        // Use the imgur image upload API as documented at https://api.imgur.com/endpoints/image
        val request = Request.Builder()
            .url("https://api.github.com/gists/c2a7c39532239ff261be")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body!!.source()
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val gist = gistJsonAdapter.fromJson(responseBody)

                    for ((key, value) in gist!!.files!!) {
                        println(key)
                        println(value.content)
                        runOnUiThread {
                            textview_result.text = value.content
                        }
                    }
                }
            }
        })
    }
    fun cancellingCall() {
        val request = Request.Builder()
            .url("https://httpbin.org/delay/2") // This URL is served with a 2 second delay.
            .build()

        val startNanos = System.nanoTime()
        val call = client.newCall(request)

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // Schedule a job to cancel the call in 1 second.
                executor.schedule({
                    System.out.printf("%.2f Canceling call.%n", (System.nanoTime() - startNanos) / 1e9f)
                    call.cancel()
                    System.out.printf("%.2f Canceled call.%n", (System.nanoTime() - startNanos) / 1e9f)
                }, 1, TimeUnit.SECONDS)

                System.out.printf("%.2f Executing call.%n", (System.nanoTime() - startNanos) / 1e9f)
                try {

                    System.out.printf("%.2f Call was expected to fail, but completed: %s%n",
                        (System.nanoTime() - startNanos) / 1e9f, response)

                } catch (e: IOException) {
                    System.out.printf("%.2f Call failed as expected: %s%n",
                        (System.nanoTime() - startNanos) / 1e9f, e)
                }
            }
        })
    }
    fun timeout() {
        val request = Request.Builder()
            .url("https://httpbin.org/delay/2") // This URL is served with a 2 second delay.
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    println("Response completed: $response")
                }
            }
        })
    }
    fun preCallConfig() {
        val request = Request.Builder()
            .url("https://httpbin.org/delay/2") // This URL is served with a 2 second delay.
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    // Copy to customize OkHttp for this request.
                    val client1 = client.newBuilder()
                        .readTimeout(500, TimeUnit.MILLISECONDS)
                        .build()
                    try {
                        client1.newCall(request).execute().use { response ->
                            println("Response 1 succeeded: $response")
                        }
                    } catch (e: IOException) {
                        println("Response 1 failed: $e")
                    }

                    // Copy to customize OkHttp for this request.
                    val client2 = client.newBuilder()
                        .readTimeout(3000, TimeUnit.MILLISECONDS)
                        .build()
                    try {
                        client2.newCall(request).execute().use { response ->
                            println("Response 2 succeeded: $response")
                        }
                    } catch (e: IOException) {
                        println("Response 2 failed: $e")
                    }

                }
            }
        })
    }
    fun handleAuthen() {
        val request = Request.Builder()
            .url("http://publicobject.com/secrets/hellosecret.txt")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    println(response.body!!.string())
                }
            }
        })
    }
    // for posting string
    // for post streaming
    companion object {
        val MEDIA_TYPE_MARKDOWN = "text/x-markdown; charset=utf-8".toMediaType()
    }
    // for moshi
    @JsonClass(generateAdapter = true)
    data class Gist(var files: Map<String, GistFile>?)

    @JsonClass(generateAdapter = true)
    data class GistFile(var content: String?)
}
