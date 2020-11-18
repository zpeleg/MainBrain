package ninja.ziv.mainbrain

import kotlin.random.Random

open class GameAction
open class GuiAction

class StartGame : GameAction()
class TryGuess(val guesses: List<Int>) : GameAction()

class ShowGuess(val guesses: List<Int>, val result: String, val finishGame: Boolean) : GuiAction()
class ClearScreen(val hint: String) : GuiAction()
class Error(val message: String) : GuiAction()

interface RandomGenerator {
    fun getRandomCollection(count: Int = 4, min: Int = 1, max: Int = 6): List<Int>
}

class RealRandomGenerator : RandomGenerator {
        override fun getRandomCollection(count: Int, min: Int, max: Int): List<Int> =
        (1..count).map { Random.nextInt(min, max) }
}

fun String.allIndexesOf(c: Char): List<Int> {
    val indexes = ArrayList<Int>()
    var index = indexOf(c)
    indexes.add(index)
    while (index >= 0) {
        indexes.add(index)
        index = indexOf(c, index + 1)
    }
    return indexes
}

class GameManager(private val randomGenerator: RandomGenerator) {
    enum class State {
        NotInitialized,
        Running,
        Finished
    }

    private var currentState = State.NotInitialized
    private lateinit var secret: List<Int>

    fun processAction(action: GameAction): GuiAction {
        when (currentState) {
            State.NotInitialized -> {
                return when (action) {
                    is StartGame -> {
                        currentState = State.Running
                        secret = randomGenerator.getRandomCollection()
                        ClearScreen(secret.joinToString())
                    }
                    else -> Error("Bad message")
                }
            }
            State.Running -> {
                when (action) {
                    is TryGuess -> {
                        val secretCopy = secret.toMutableList()
                        val guessesCopy = action.guesses.toMutableList()
                        val result = StringBuilder(4)
                        for (i in 0 until secretCopy.size) {
                            if (secretCopy[i] == guessesCopy[i]) {
                                result.append("X")
                                secretCopy[i] = -1
                                guessesCopy[i] = -1
                            }
                        }
                        for (i in 0 until secretCopy.size) {
                            if (guessesCopy[i] == -1) {
                                continue
                            }
                            val indexOf = secretCopy.indexOf(guessesCopy[i])
                            if (indexOf == -1) {
                                result.append('.')
                            } else {
                                result.append('O')
                                secretCopy[indexOf] = -1
                            }
                        }
                        val resultString =
                            result.toString().toCharArray().sorted().joinToString("")
                        val finished = resultString == "XXXX"
                        if (finished) {
                            currentState = State.Finished
                        }
                        return ShowGuess(action.guesses, resultString, finished)
                    }
                    else -> return Error("Bad message")
                }
            }
            State.Finished -> {
                return Error("Bad message")
            }
        }
    }
}