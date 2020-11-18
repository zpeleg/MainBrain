package ninja.ziv.mainbrain

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class GuessModel(
    val guess1: Int,
    val guess2: Int,
    val guess3: Int,
    val guess4: Int,
    val result: String
)

data class UserGuess(val userGuess: List<Int>)

fun GuessModel.asList() = listOf(guess1, guess2, guess3, guess4)

class GuessesAdapter(private val guesses: List<GuessModel>) :
    RecyclerView.Adapter<GuessesAdapter.ViewHolder>() {
    inner class ViewHolder(listViewItem: View) : RecyclerView.ViewHolder(listViewItem) {
        val guessTextView: TextView = itemView.findViewById(R.id.theGuess)
        val resultTextView: TextView = itemView.findViewById(R.id.result)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuessesAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val guessView = inflater.inflate(R.layout.guess, parent, false)
        return ViewHolder(guessView)
    }

    override fun onBindViewHolder(holder: GuessesAdapter.ViewHolder, position: Int) {
        val guess = guesses[position]
        holder.guessTextView.text = guess.asList().joinToString()
        holder.resultTextView.text = guess.result
    }

    override fun getItemCount() = guesses.size
}

fun Button.textToInt() = this.text.toString().toIntOrNull() ?: throw Exception("Oh damn")
class ButtonManager(ctx: Activity) {
    private val buttons: Array<Button> = arrayOf(
        ctx.findViewById(R.id.button1),
        ctx.findViewById(R.id.button2),
        ctx.findViewById(R.id.button3),
        ctx.findViewById(R.id.button4)
    )

    fun initialize() {
        buttons.forEach { btn: Button ->
            btn.setOnClickListener {
                if (it is Button) {
                    val current = it.textToInt()
                    it.text = if (current == 6) "1" else (current + 1).toString()
                }
            }
        }
    }

    fun getCurrentGuess() = with(buttons) {
        UserGuess(
            listOf(
                this[0].textToInt(),
                this[1].textToInt(),
                this[2].textToInt(),
                this[3].textToInt()
            )
        )
    }

    fun lockButtons() {
        buttons.forEach { it.isEnabled = false }
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var buttonManager: ButtonManager
    private val guesses = ArrayList<GuessModel>()
    private val adapter = GuessesAdapter(guesses)
    private val manager = GameManager(RealRandomGenerator())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonManager = ButtonManager(this)
        buttonManager.initialize()
        val guessesView = findViewById<RecyclerView>(R.id.guesses)
        guessesView.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        guessesView.layoutManager = linearLayoutManager
        val hint = manager.processAction(StartGame())
//        if (hint is ClearScreen) {
//            Toast.makeText(applicationContext, hint.hint, Toast.LENGTH_SHORT)
//                .show()
//        }
    }

    fun tryCurrentGuess(view: View) {
        val element = buttonManager.getCurrentGuess()
        when (val result = manager.processAction(TryGuess(element.userGuess))) {
            is ShowGuess -> {
                val model = GuessModel(
                    result.guesses[0],
                    result.guesses[1],
                    result.guesses[2],
                    result.guesses[3],
                    result.result
                )
                guesses.add(0, model)
                adapter.notifyItemInserted(0)
                if (result.finishGame) {
                    Toast.makeText(applicationContext, "You win, great success", Toast.LENGTH_SHORT)
                        .show()
                    buttonManager.lockButtons()
                }
            }
            else -> {
                Log.e("THING", "very big error happened")
            }
        }
    }
}

