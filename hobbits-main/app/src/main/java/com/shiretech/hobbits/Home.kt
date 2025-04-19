package com.shiretech.hobbits

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.*
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.widget.ScrollView
import org.json.JSONArray
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RetryPolicy

class Home : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var gridView: GridView
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var monthYearTextView: TextView
    private lateinit var displayTimeView: TextView
    private lateinit var dateAssignedTextView: TextView
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var requestQueue: RequestQueue
    private val messages = mutableListOf<ChatMessage>()

    private var currentMonth: Int = 0
    private var currentYear: Int = 0

    private var selectedDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)

        // Initialize all views first
        val textViewWelcome = findViewById<TextView>(R.id.TextViewWelcome)
        val textViewTime = findViewById<TextView>(R.id.TimeAssignedForHobby0)
        val chatButton = findViewById<ImageView>(R.id.ClickChat)
        val chatbotContainer = findViewById<View>(R.id.chatbotContainer)
        val mainContent = findViewById<ScrollView>(R.id.mainContent)
        chatRecyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        chatInput = findViewById<EditText>(R.id.chatInput)
        sendButton = findViewById<ImageButton>(R.id.sendButton)

        // Initialize Volley
        requestQueue = Volley.newRequestQueue(this)

        // Setup RecyclerView
        chatAdapter = ChatAdapter(messages)
        chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@Home)
            adapter = chatAdapter
        }

        // Chat button click listener
        chatButton.setOnClickListener {
            if (chatbotContainer.visibility == View.VISIBLE) {
                chatbotContainer.visibility = View.GONE
                mainContent.visibility = View.VISIBLE
            } else {
                chatbotContainer.visibility = View.VISIBLE
                mainContent.visibility = View.GONE
            }
        }

        // Send button click listener
        sendButton.setOnClickListener {
            val message = chatInput.text.toString().trim()
            if (message.isNotEmpty()) {
                // Add user message to chat
                messages.add(ChatMessage(message, true))
                chatAdapter.notifyItemInserted(messages.size - 1)
                chatInput.text.clear()

                // Scroll to bottom
                chatRecyclerView.scrollToPosition(messages.size - 1)

                // Send message to Deepseek API
                sendMessageToDeepseek(message)
            }
        }

        // Rest of your existing initialization code...
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            val usersRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)

            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.child("name").getValue(String::class.java)

                    if (user != null) {
                        val welcomeMessage = "Hello, $user"
                        textViewWelcome.text = welcomeMessage
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle the error
                }
            })
        }

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: ""

        val hobbyNameTextView = findViewById<TextView>(R.id.HobbyName0)

        database = FirebaseDatabase.getInstance().getReference("users").child(userId)
        val hobbiesRef = database.child("categories").child("hobbies").child("savedhobby0")

        // Fetch the hobby name from the database
        hobbiesRef.child("name").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val hobbyName = dataSnapshot.getValue(String::class.java)
                if (hobbyName != null) {
                    hobbyNameTextView.text = hobbyName
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Home", "Failed to fetch hobby name: ${databaseError.message}")
            }
        })

        val ButtonViewProgressButtonViewProgressHobby0 = findViewById<Button>(R.id.ButtonViewProgressButtonViewProgressHobby0)
        ButtonViewProgressButtonViewProgressHobby0.setOnClickListener {
            database = FirebaseDatabase.getInstance().getReference("users").child(userId)
            val hobbiesRef = database.child("categories").child("hobbies").child("savedhobby0")

            hobbiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Retrieve the hobby name from the database
                    val hobbyName = dataSnapshot.child("name").getValue(String::class.java)
                    if (hobbyName != null) {
                        // Retrieve the hobbits and bits data from the database
                        val hobbits = ArrayList<String>()
                        val hobbitBitsMap = HashMap<String, ArrayList<String>>()

                        for (hobbitSnapshot in dataSnapshot.child("hobbits").children) {
                            val hobbitName = hobbitSnapshot.child("name").getValue(String::class.java)
                            if (hobbitName != null) {
                                hobbits.add(hobbitName)

                                val bits = ArrayList<String>()
                                for (bitSnapshot in hobbitSnapshot.child("bits").children) {
                                    val bitText = bitSnapshot.getValue(String::class.java)
                                    if (bitText != null) {
                                        bits.add(bitText)
                                    }
                                }
                                hobbitBitsMap[hobbitName] = bits
                            }
                        }

                        // Start EditHobby activity with the intent and pass the hobbits and bits data
                        val intent = Intent(this@Home, Progress_List::class.java).apply {
                            putExtra("hobbyName", hobbyName)
                            putExtra("hobbits", hobbits)
                            putExtra("hobbitBitsMap", hobbitBitsMap)
                        }
                        startActivity(intent)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Progress_List", "Failed to fetch hobby data: ${databaseError.message}")
                }
            })
        }


        val UnselectedCategoriesImageClick = findViewById<ImageView>(R.id.ClickUnselectedCategories)
        UnselectedCategoriesImageClick.setOnClickListener {
            val intent = Intent(this, Categories::class.java)
            startActivity(intent)
        }
        val UnselectedUserImageClick = findViewById<ImageView>(R.id.ClickUnselectedUser)
        UnselectedUserImageClick.setOnClickListener {
            val intent = Intent(this, User_Profile::class.java)
            startActivity(intent)
        }

        gridView = findViewById(R.id.calendarGridView)
        monthYearTextView = findViewById(R.id.MonthandYear)

        val initialCalendar = Calendar.getInstance()
        currentMonth = initialCalendar.get(Calendar.MONTH)
        currentYear = initialCalendar.get(Calendar.YEAR)

        val currentDate = Calendar.getInstance().time

        calendarAdapter = CalendarAdapter(this, currentMonth, currentYear)
        gridView.adapter = calendarAdapter

        updateMonthAndYearTextView()


        val dateNext: ImageView = findViewById(R.id.DateNext)
        val dateBack: ImageView = findViewById(R.id.DateBack)

        dateNext.setOnClickListener {
            val nextCalendar = getNextMonthCalendar()
            currentMonth = nextCalendar.get(Calendar.MONTH)
            currentYear = nextCalendar.get(Calendar.YEAR)
            calendarAdapter.updateCalendar(currentMonth, currentYear)
            updateMonthAndYearTextView()
        }
        dateBack.setOnClickListener {
            val previousCalendar = getPreviousMonthCalendar()
            currentMonth = previousCalendar.get(Calendar.MONTH)
            currentYear = previousCalendar.get(Calendar.YEAR)
            calendarAdapter.updateCalendar(currentMonth, currentYear)
            updateMonthAndYearTextView()
        }

        // Get the current month and year
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // Create the adapter with the current month and year
        calendarAdapter = CalendarAdapter(this, currentMonth, currentYear)
        gridView.adapter = calendarAdapter


        //SCHEDULE


        displayTimeView = findViewById(R.id.TimeAssignedForHobby0)


        val sharedPreferences = getSharedPreferences("SchedulePrefs", MODE_PRIVATE)
        val savedHour = sharedPreferences.getInt("hour", 12)
        val savedMinute = sharedPreferences.getInt("minute", 0)
        val savedAMPM = sharedPreferences.getString("am_pm", "AM")

        val displayText = " ${String.format("%02d", savedHour)}:${String.format("%02d", savedMinute)} $savedAMPM"
        displayTimeView.text = displayText


        dateAssignedTextView = findViewById(R.id.DateAssignedForHobby0)

        // Retrieve the selected days from the intent
        val selectedDays = intent.getStringArrayExtra("selectedDays")

        // Update the TextView with the selected days
        if (selectedDays != null && selectedDays.isNotEmpty()) {
            val daysString = selectedDays.joinToString(" ")
            dateAssignedTextView.text = daysString
        }
    }

    private fun updateMonthAndYearTextView() {
        val monthName = DateFormatSymbols().months[currentMonth]
        val monthYearText = "$monthName, $currentYear"
        monthYearTextView.text = monthYearText
    }

    private fun getNextMonthCalendar(): Calendar {
        val nextCalendar = Calendar.getInstance()
        nextCalendar.set(Calendar.MONTH, currentMonth)
        nextCalendar.set(Calendar.YEAR, currentYear)
        nextCalendar.add(Calendar.MONTH, 1)
        return nextCalendar
    }

    private fun getPreviousMonthCalendar(): Calendar {
        val previousCalendar = Calendar.getInstance()
        previousCalendar.set(Calendar.MONTH, currentMonth)
        previousCalendar.set(Calendar.YEAR, currentYear)
        previousCalendar.add(Calendar.MONTH, -1)
        return previousCalendar
    }

    private fun sendMessageToDeepseek(message: String) {
        val url = "https://openrouter.ai/api/v1/chat/completions"
        val jsonBody = JSONObject().apply {
            put("model", "deepseek/deepseek-r1:free")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", message)
                })
            })
        }

        // Log the request
        Log.d("ChatAPI", "Sending request: ${jsonBody.toString()}")

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                try {
                    // Log the response
                    Log.d("ChatAPI", "Received response: ${response.toString()}")

                    // Show a loading message while processing
                    runOnUiThread {
                        messages.add(ChatMessage("Thinking...", false))
                        chatAdapter.notifyItemInserted(messages.size - 1)
                        chatRecyclerView.scrollToPosition(messages.size - 1)
                    }

                    // Parse the response
                    val choices = response.getJSONArray("choices")
                    Log.d("ChatAPI", "Choices array: ${choices.toString()}")

                    val firstChoice = choices.getJSONObject(0)
                    Log.d("ChatAPI", "First choice: ${firstChoice.toString()}")

                    val messageObj = firstChoice.getJSONObject("message")
                    Log.d("ChatAPI", "Message object: ${messageObj.toString()}")

                    val aiResponse = messageObj.getString("content")
                    Log.d("ChatAPI", "AI Response: $aiResponse")

                    // Update UI on the main thread
                    runOnUiThread {
                        // Remove the loading message
                        if (messages.isNotEmpty() && messages.last().message == "Thinking...") {
                            messages.removeAt(messages.size - 1)
                            chatAdapter.notifyItemRemoved(messages.size)
                        }

                        // Add AI response to chat
                        messages.add(ChatMessage(aiResponse, false))
                        chatAdapter.notifyItemInserted(messages.size - 1)
                        chatRecyclerView.scrollToPosition(messages.size - 1)
                    }
                } catch (e: Exception) {
                    Log.e("ChatAPI", "Error parsing response", e)
                    e.printStackTrace()
                    
                    runOnUiThread {
                        // Remove the loading message if it exists
                        if (messages.isNotEmpty() && messages.last().message == "Thinking...") {
                            messages.removeAt(messages.size - 1)
                            chatAdapter.notifyItemRemoved(messages.size)
                        }
                        messages.add(ChatMessage("Sorry, I couldn't process the response. Please try again.", false))
                        chatAdapter.notifyItemInserted(messages.size - 1)
                        chatRecyclerView.scrollToPosition(messages.size - 1)
                    }
                }
            },
            { error ->
                Log.e("ChatAPI", "Network error", error)
                error.printStackTrace()
                
                runOnUiThread {
                    // Remove the loading message if it exists
                    if (messages.isNotEmpty() && messages.last().message == "Thinking...") {
                        messages.removeAt(messages.size - 1)
                        chatAdapter.notifyItemRemoved(messages.size)
                    }
                    messages.add(ChatMessage("Sorry, I couldn't process your request. Please try again.", false))
                    chatAdapter.notifyItemInserted(messages.size - 1)
                    chatRecyclerView.scrollToPosition(messages.size - 1)
                }
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["HTTP-Referer"] = "https://github.com/ShireTech" // Replace with your app's domain
                headers["X-Title"] = "Hobbits App"
                headers["Authorization"] = "Bearer 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"
                headers["Content-Type"] = "application/json"
                return headers
            }

            override fun getRetryPolicy(): RetryPolicy {
                return DefaultRetryPolicy(
                    30000, // 30 seconds timeout
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            }
        }

        requestQueue.add(request)
    }
}

