package ninja.ziv.mainbrain

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Test

class FakeRandomGenerator(val list: List<Int>) : RandomGenerator {
    override fun getRandomCollection(count: Int, min: Int, max: Int): List<Int> = list
}

class GameManagerTest {
    private fun runGame(
        secret: List<Int>,
        guessesAndExpectedResults: List<Pair<List<Int>, String>>
    ) {
        val random = FakeRandomGenerator(secret)
        val manager = GameManager(random)
        manager.processAction(StartGame())
        for ((guess, expectedResult) in guessesAndExpectedResults) {
            val res = manager.processAction(TryGuess(guess))
            res shouldBeInstanceOf ShowGuess::class
            res as ShowGuess
            res.guesses shouldBeEqualTo guess
            res.result shouldBeEqualTo expectedResult
        }
    }

    @Test
    fun gameflow1() {
        // This is a flow I noticed is bugged, add further test cases as necessary
        val secret = listOf(2, 4, 4, 1)
        val guessesAndExpectedResults = listOf(
            Pair(listOf(4, 1, 1, 1), "..OX"),
            Pair(listOf(4, 4, 1, 1), ".OXX"),
            Pair(listOf(2, 4, 1, 1), ".XXX"),
            Pair(listOf(2, 4, 4, 1), "XXXX")
        )

        runGame(secret, guessesAndExpectedResults)
    }

    @Test
    fun gameflow2() {
        val secret = listOf(4, 3, 2, 5)
        val guessesAndExpectedResults = listOf(
            Pair(listOf(1, 1, 1, 1), "...."),
            Pair(listOf(2, 1, 1, 1), "...O"),
            Pair(listOf(2, 2, 1, 1), "...O"),
            Pair(listOf(2, 2, 2, 1), "...X"),
            Pair(listOf(2, 2, 2, 2), "...X"),
            Pair(listOf(2, 3, 2, 2), "..XX"),
            Pair(listOf(3, 3, 2, 2), "..XX"),
            Pair(listOf(4, 3, 2, 2), ".XXX"),
            Pair(listOf(4, 4, 2, 2), "..XX"),
            Pair(listOf(4, 3, 3, 2), ".OXX"),
            Pair(listOf(4, 3, 4, 2), ".OXX"),
            Pair(listOf(4, 3, 5, 2), "OOXX"),
            Pair(listOf(4, 3, 2, 5), "XXXX")
        )
        runGame(secret, guessesAndExpectedResults)
    }
}