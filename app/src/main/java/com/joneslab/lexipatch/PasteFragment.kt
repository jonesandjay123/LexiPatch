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

        // Pre-fill API Key logic if desired, or leave it for user to input if not hardcoded
        // In a real app, strict NO-NO to hardcode keys. 
        // We'll leave it invisible by default as per layout, but logic could toggle it.
        // For this MVP, we will assume user might put it in local.properties or just hardcode for demo if provided.
        // Since user didn't provide one, we show the field if we want, or just fail gracefully.
        // Let's make it visible so user can enter it as requirements say "structure it so I can add my key easily".
        etApiKey.visibility = View.VISIBLE

        btnGenerate.setOnClickListener {
            val text = etInput.text.toString()
            val apiKey = etApiKey.text.toString()
            if (text.isBlank()) {
                Toast.makeText(context, "Please paste some text", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (apiKey.isBlank()) {
                Toast.makeText(context, "Please enter Gemini API Key", Toast.LENGTH_SHORT).show()
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
                val result = callGeminiApi(text, apiKey)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnGenerate.isEnabled = true
                    if (result != null) {
                        // Launch Review Activity
                        val intent = Intent(requireContext(), ReviewActivity::class.java)
                        val gson = Gson()
                        intent.putExtra("items_json", gson.toJson(result))
                        startActivity(intent)
                    } else {
                        tvStatus.text = "Failed to generate vocabulary. Check API Key or Network."
                        tvStatus.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnGenerate.isEnabled = true
                    tvStatus.text = "Error: ${e.message}"
                    tvStatus.visibility = View.VISIBLE
                    e.printStackTrace()
                }
            }
        }
    }

    private fun callGeminiApi(inputText: String, apiKey: String): List<VocabItem>? {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true

        val prompt = "Extract up to 20 useful vocabulary words from the following text for an English learner. " +
                "Provide the English word, its Chinese meaning, and an optional abbreviation if common. " +
                "Strictly return ONLY a JSON object with this format: " +
                "{ \"items\": [ { \"english\": \"...\", \"abbr\": \"...\", \"chinese\": \"...\" } ] }. " +
                "Do not use markdown formatting. Text: $inputText"

        val jsonBody = """
            {
              "contents": [{
                "parts":[{
                  "text": "$prompt"
                }]
              }]
            }
        """.trimIndent()

        // Handle potential strict JSON issues with quotes in prompt? 
        // For MVP simplicity we assume simple user input. In production, use Gson to construct body.
        val safeJsonBody = Gson().toJson(mapOf(
            "contents" to listOf(mapOf(
                "parts" to listOf(mapOf("text" to prompt))
            ))
        ))

        try {
            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(safeJsonBody)
            writer.flush()
            writer.close()

            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                return parseGeminiResponse(response)
            } else {
                // Log error
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            conn.disconnect()
        }
    }

    private fun parseGeminiResponse(jsonResponse: String): List<VocabItem> {
        // Gemini returns a complex nested JSON. We need to extract the text from candidates[0].content.parts[0].text
        // Then parse that text as our expected JSON.
        try {
            val gson = Gson()
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            val rootMap: Map<String, Any> = gson.fromJson(jsonResponse, mapType)
            
            val candidates = rootMap["candidates"] as? List<Map<String, Any>>
            val content = candidates?.get(0)?.get("content") as? Map<String, Any>
            val parts = content?.get("parts") as? List<Map<String, Any>>
            var text = parts?.get(0)?.get("text") as? String ?: return emptyList()

            // Cleanup markdown code blocks if present
            text = text.replace("```json", "").replace("```", "").trim()

            val resultType = object : TypeToken<Map<String, List<VocabItem>>>() {}.type
            val resultMap: Map<String, List<VocabItem>> = gson.fromJson(text, resultType)
            
            return resultMap["items"] ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
}
