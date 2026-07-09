package co.monterosa.sdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConverterTest {

    // -----------------------------------------------------------------------
    // toBestNumber — converts whole Doubles to Int, preserves fractional
    // -----------------------------------------------------------------------

    @Test
    fun `whole double becomes Int`() {
        assertEquals(42, 42.0.toBestNumber())
    }

    @Test
    fun `zero double becomes Int 0`() {
        assertEquals(0, 0.0.toBestNumber())
    }

    @Test
    fun `negative whole double becomes negative Int`() {
        assertEquals(-7, (-7.0).toBestNumber())
    }

    @Test
    fun `fractional double is preserved`() {
        assertEquals(3.14, 3.14.toBestNumber())
    }

    @Test
    fun `double at Int MAX_VALUE boundary becomes Int`() {
        assertEquals(Int.MAX_VALUE, Int.MAX_VALUE.toDouble().toBestNumber())
    }

    @Test
    fun `double beyond Int MAX_VALUE stays Double`() {
        val large = Int.MAX_VALUE.toDouble() + 1
        assertEquals(large, large.toBestNumber())
    }

    @Test
    fun `double at Int MIN_VALUE boundary becomes Int`() {
        assertEquals(Int.MIN_VALUE, Int.MIN_VALUE.toDouble().toBestNumber())
    }

    @Test
    fun `double below Int MIN_VALUE stays Double`() {
        val small = Int.MIN_VALUE.toDouble() - 1
        assertEquals(small, small.toBestNumber())
    }

    @Test
    fun `positive infinity stays Double`() {
        assertEquals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY.toBestNumber())
    }

    @Test
    fun `negative infinity stays Double`() {
        assertEquals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY.toBestNumber())
    }

    @Test
    fun `NaN stays Double`() {
        assertTrue((Double.NaN.toBestNumber() as Double).isNaN())
    }

    // -----------------------------------------------------------------------
    // normalize — recursive normalization of values from RN bridge
    // -----------------------------------------------------------------------

    @Test
    fun `normalize null returns null`() {
        assertNull(null.normalize())
    }

    @Test
    fun `normalize String returns same String`() {
        assertEquals("hello", "hello".normalize())
    }

    @Test
    fun `normalize empty String returns empty String`() {
        assertEquals("", "".normalize())
    }

    @Test
    fun `normalize Boolean true returns true`() {
        assertEquals(true, true.normalize())
    }

    @Test
    fun `normalize Boolean false returns false`() {
        assertEquals(false, false.normalize())
    }

    @Test
    fun `normalize Int returns same Int`() {
        assertEquals(42, (42 as Any?).normalize())
    }

    @Test
    fun `normalize whole Double converts to Int`() {
        assertEquals(5, (5.0 as Any?).normalize())
    }

    @Test
    fun `normalize fractional Double preserves Double`() {
        assertEquals(2.5, (2.5 as Any?).normalize())
    }

    @Test
    fun `normalize Float converts to best number`() {
        assertEquals(3, (3.0f as Any?).normalize())
    }

    @Test
    fun `normalize fractional Float preserves as Double`() {
        // 1.5f -> 1.5 (Double) -> stays 1.5
        val result = (1.5f as Any?).normalize()
        assertEquals(1.5, result)
    }

    @Test
    fun `normalize Long converts to best number`() {
        assertEquals(100, (100L as Any?).normalize())
    }

    @Test
    fun `normalize Long beyond Int range preserves as Double`() {
        val big = Int.MAX_VALUE.toLong() + 1
        val result = (big as Any?).normalize()
        assertEquals(big.toDouble(), result)
    }

    @Test
    fun `normalize Map recursively normalizes values`() {
        val input: Map<*, *> = mapOf("a" to 1.0, "b" to "str", "c" to true)
        val result = (input as Any?).normalize() as Map<*, *>
        assertEquals(1, result["a"])
        assertEquals("str", result["b"])
        assertEquals(true, result["c"])
    }

    @Test
    fun `normalize nested Map works recursively`() {
        val input: Map<*, *> = mapOf("outer" to mapOf("inner" to 2.0))
        val result = (input as Any?).normalize() as Map<*, *>
        val inner = result["outer"] as Map<*, *>
        assertEquals(2, inner["inner"])
    }

    @Test
    fun `normalize List normalizes each element`() {
        val input: List<*> = listOf(1.0, "two", true, null)
        val result = (input as Any?).normalize() as List<*>
        assertEquals(listOf(1, "two", true, null), result)
    }

    @Test
    fun `normalize Array normalizes each element`() {
        val input: Array<*> = arrayOf(1.0, "two")
        val result = (input as Any?).normalize() as List<*>
        assertEquals(listOf(1, "two"), result)
    }

    @Test
    fun `normalize unknown type converts to toString`() {
        val obj = Object()
        assertEquals(obj.toString(), (obj as Any?).normalize())
    }

    // -----------------------------------------------------------------------
    // toStringKeyMap — converts Map<*, *> to Map<String, Any?> with normalization
    // -----------------------------------------------------------------------

    @Test
    fun `toStringKeyMap preserves String keys`() {
        val input: Map<Any?, Any?> = mapOf("a" to 1, "b" to 2)
        val result = input.toStringKeyMap()
        assertEquals(2, result.size)
        assertTrue(result.containsKey("a"))
        assertTrue(result.containsKey("b"))
    }

    @Test
    fun `toStringKeyMap skips non-String keys`() {
        val input: Map<Any?, Any?> = mapOf("valid" to "yes", 123 to "no", null to "also-no")
        val result = input.toStringKeyMap()
        assertEquals(1, result.size)
        assertEquals("yes", result["valid"])
    }

    @Test
    fun `toStringKeyMap normalizes values`() {
        val input: Map<Any?, Any?> = mapOf("num" to 5.0, "str" to "hello")
        val result = input.toStringKeyMap()
        assertEquals(5, result["num"]) // 5.0 -> Int 5
        assertEquals("hello", result["str"])
    }

    @Test
    fun `toStringKeyMap handles empty map`() {
        val result = emptyMap<Any?, Any?>().toStringKeyMap()
        assertTrue(result.isEmpty())
    }

    // -----------------------------------------------------------------------
    // asMap — safe cast from Any? to Map<String, Any?>
    // -----------------------------------------------------------------------

    @Test
    fun `asMap returns map for Map input`() {
        val input: Any? = mapOf("key" to "value")
        val result = input.asMap()
        assertNotNull(result)
        assertEquals("value", result!!["key"])
    }

    @Test
    fun `asMap returns null for non-Map input`() {
        assertNull("not a map".asMap())
        assertNull(42.asMap())
        assertNull(null.asMap())
    }

    private fun assertNotNull(value: Any?) {
        assertTrue("Expected non-null value", value != null)
    }
}
