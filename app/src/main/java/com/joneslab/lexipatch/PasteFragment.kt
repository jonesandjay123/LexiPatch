package com.joneslab.lexipatch

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class PasteFragment : Fragment() {

    private lateinit var etInput: EditText
    private lateinit var etApiKey: EditText
    private lateinit var btnGenerate: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_paste, container, false)
        etInput = view.findViewById(R.id.et_input)
        etApiKey = view.findViewById(R.id.et_api_key)
        btnGenerate = view.findViewById(R.id.btn_generate)
        progressBar = view.findViewById(R.id.progress_bar)
        tvStatus = view.findViewById(R.id.tv_status)

        // API Key is now handled securely via BuildConfig (read from local.properties)
        etApiKey.visibility = View.GONE

        btnGenerate.setOnClickListener {
            val text = etInput.text.toString()
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (text.isBlank()) {
                Toast.makeText(context, "Please paste some text", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (apiKey.isBlank()) {
                Toast.makeText(context, "API Key missing in local.properties (GEMINI_API_KEY=...)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            generateVocabulary(text, apiKey)
        }

        return view
    }

    private fun generateVocabulary(text: String, apiKey: String) {
        progressBar.visibility = View.VISIBLE
        tvStatus.visibility = View.GONE
        btnGenerate.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Using the official Google Gen AI Java SDK
                val client = com.google.genai.Client.builder()
                    .apiKey(apiKey)
                    .build()
                
                val prompt = "Extract up to 20 useful vocabulary words from the following text for an English learner. " +
                        "Provide the English word, its Chinese meaning, and an optional abbreviation if common. " +
                        "Strictly return ONLY a JSON object with this format: " +
                        "{ \"items\": [ { \"english\": \"...\", \"abbr\": \"...\", \"chinese\": \"...\" } ] }. " +
                        "Do not use markdown formatting. Text: $text"

                val response = client.models.generateContent(
                    "gemini-2.5-flash", // or gemini-1.5-flash which is stable
                    prompt,
                    null
                )
                
                // The SDK returns text from the response
                val responseText = response.text() ?: ""
                
                val vocabItems = parseGeminiResponse(responseText)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnGenerate.isEnabled = true
                    
                    if (vocabItems.isNotEmpty()) {
                        val intent = Intent(requireContext(), ReviewActivity::class.java)
                        val gson = Gson()
                        intent.putExtra("items_json", gson.toJson(vocabItems))
                        startActivity(intent)
                    } else {
                        tvStatus.text = "No vocabulary found or parsing failed."
                        tvStatus.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnGenerate.isEnabled = true
                    tvStatus.text = "Error: ${e.localizedMessage}"
                    tvStatus.visibility = View.VISIBLE
                    e.printStackTrace()
                }
            }
        }
    }

    private fun callGeminiApi(inputText: String, apiKey: String): List<VocabItem>? {
        // Deprecated: Replaced by Google Gen AI SDK
        return null
    }

    private fun parseGeminiResponse(responseText: String): List<VocabItem> {
        try {
            var text = responseText.trim()
            // Cleanup markdown code blocks if present
            if (text.startsWith("```")) {
                text = text.replace("```json", "").replace("```", "").trim()
            }

            val gson = Gson()
            val resultType = object : TypeToken<Map<String, List<VocabItem>>>() {}.type
            val resultMap: Map<String, List<VocabItem>> = gson.fromJson(text, resultType)
            
            return resultMap["items"] ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: try to see if it's a raw list? No, prompt asks for object.
            return emptyList()
        }
    }
}
