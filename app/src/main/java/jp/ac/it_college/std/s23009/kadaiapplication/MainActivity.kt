package jp.ac.it_college.std.s23009.kadaiapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import okhttp3.Request
import jp.ac.it_college.std.s23009.kadaiapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        // ログに記載するタグ用の文字列
        private const val DEBUG_TAG = "kadaiApplication"
        // アドバイスのURL
        private const val ADVICE_URL =
            "https://api.adviceslip.com/advice"

        private const val Name_URL =
            "https://api.genderize.io/?name="
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.apply {
            val manager = LinearLayoutManager(this@MainActivity)
            DividerItemDecoration(this@MainActivity, manager.orientation)
        }
        binding.btJugment.setOnClickListener {
            val name = binding.editText.text.toString()
            if (name.isNotEmpty()) {
                fetchGender(name)
            }
        }
        receiveAdvice()
    }



private fun fetchGender(name: String) {
    lifecycleScope.launch {
        try {
            val gender = getGenderFromApi(name)

            val genderInJapanese = when (gender) {
                "male" -> "男"
                "female" -> "女"
                else -> "不明"
            }
            binding.tvGenderView.text = genderInJapanese
        } catch (e: IOException) {
            binding.tvGenderView.text = "エラーが発生しました"
        }
    }
}

private suspend fun getGenderFromApi(name: String): String? {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val url = "$Name_URL$name"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val jsonResponse = JSONObject(response.body?.string() ?: "")
            jsonResponse.getString("gender")
        }
    }
}

    @UiThread
    private fun receiveAdvice() {
        lifecycleScope.launch {
            try {
                val advice = fetchAdvice()
                // アドバイスを取得してUIを更新する
                if (advice != null) {
                    binding.adviceText.text = advice
                }

            } catch (e: IOException) {
                // Handle the exception
                e.printStackTrace()
            }
        }
    }

    private suspend fun fetchAdvice(): String? {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(ADVICE_URL)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                // Parse the JSON response
                val jsonResponse = JSONObject(response.body?.string() ?: "")
                jsonResponse.getJSONObject("slip").getString("advice")
            }
        }
    }

    private suspend fun fetchName(urlString: String): String? {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(urlString)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val jsonResponse = JSONObject(response.body?.string() ?: "")
                jsonResponse.getString("gender")
            }
        }
    }
}
